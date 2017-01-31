/*
 * Copyright 2010-2017 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jetbrains.kotlin.codegen.optimization.captured

import org.jetbrains.kotlin.builtins.PrimitiveType
import org.jetbrains.kotlin.codegen.optimization.common.OptimizationBasicInterpreter
import org.jetbrains.kotlin.codegen.optimization.common.StrictBasicValue
import org.jetbrains.kotlin.codegen.optimization.common.removeEmptyCatchBlocks
import org.jetbrains.kotlin.codegen.optimization.fixStack.peek
import org.jetbrains.kotlin.codegen.optimization.fixStack.top
import org.jetbrains.kotlin.codegen.optimization.transformer.MethodTransformer
import org.jetbrains.kotlin.resolve.jvm.AsmTypes
import org.jetbrains.org.objectweb.asm.Opcodes
import org.jetbrains.org.objectweb.asm.Type
import org.jetbrains.org.objectweb.asm.tree.*
import org.jetbrains.org.objectweb.asm.tree.analysis.BasicValue
import org.jetbrains.org.objectweb.asm.tree.analysis.Frame

class CapturedVarsOptimizationMethodTransformer : MethodTransformer() {
    override fun transform(internalClassName: String, methodNode: MethodNode) {
        Transformer(internalClassName, methodNode).run()
    }

    class RefValue(val newInsn: TypeInsnNode, type: Type, val valueType: Type) : StrictBasicValue(type) {
        var hazard: Hazard? = null
        val hasHazard get() = hazard != null

        var initCallInsn: MethodInsnNode? = null
        var localVar: LocalVariableNode? = null
        var localVarIndex = -1
        val astoreInsns: MutableCollection<VarInsnNode> = LinkedHashSet()
        val aloadInsns: MutableCollection<VarInsnNode> = LinkedHashSet()
        val stackInsns: MutableCollection<AbstractInsnNode> = LinkedHashSet()
        val getFieldInsns: MutableCollection<FieldInsnNode> = LinkedHashSet()
        val putFieldInsns: MutableCollection<FieldInsnNode> = LinkedHashSet()

        fun canRewrite(): Boolean {
            return !hasHazard &&
                   initCallInsn != null &&
                   localVar != null &&
                   localVarIndex >= 0
        }
    }

    enum class Hazard {
        UNEXPECTED_USAGE,
        MERGED,
        UNEXPECTED_STACK_OP,
        LOCAL_VAR_CONFLICT,
        ESCAPES_TO_LOCAL,
        STRANGE_INITIALIZATION
    }

    class Transformer(private val internalClassName: String, private val methodNode: MethodNode) {
        private val refValues = ArrayList<RefValue>()
        private val refValuesByNewInsn = LinkedHashMap<TypeInsnNode, RefValue>()
        private val insns = methodNode.instructions.toArray()
        private lateinit var frames: Array<out Frame<BasicValue>?>

        val hasRewritableRefValues: Boolean
            get() = refValues.isNotEmpty()

        fun run() {
            createRefValues()
            if (!hasRewritableRefValues) return

            analyze()
            if (!hasRewritableRefValues) return

            rewrite()
        }

        private fun AbstractInsnNode.getIndex() = methodNode.instructions.indexOf(this)

        private fun createRefValues() {
            for (insn in insns) {
                if (insn.opcode == Opcodes.NEW && insn is TypeInsnNode) {
                    val type = Type.getObjectType(insn.desc)
                    if (AsmTypes.isSharedVarType(type)) {
                        val valueType = REF_TYPE_TO_ELEMENT_TYPE[type.internalName] ?: continue
                        val refValue = RefValue(insn, type, valueType)
                        refValues.add(refValue)
                        refValuesByNewInsn[insn] = refValue
                    }
                }
            }
        }

        private fun analyze() {
            frames = MethodTransformer.analyze(internalClassName, methodNode, RefTrackingInterpreter())
            trackPops()
            assignLocalVars()

            refValues.removeAll { !it.canRewrite() }
            if (!hasRewritableRefValues) return
        }

        inner class RefTrackingInterpreter : OptimizationBasicInterpreter() {
            override fun merge(v: BasicValue, w: BasicValue): BasicValue =
                    when {
                        v is RefValue && w is RefValue -> {
                            if (v === w)
                                v
                            else {
                                assert(v.newInsn != w.newInsn) { "Two different RefValue instances with same newInsn" }
                                v.hazard = Hazard.MERGED
                                w.hazard = Hazard.MERGED
                                StrictBasicValue.REFERENCE_VALUE
                            }
                        }
                        v is RefValue -> {
                            v.hazard = Hazard.MERGED
                            StrictBasicValue.REFERENCE_VALUE
                        }
                        w is RefValue -> {
                            w.hazard = Hazard.MERGED
                            StrictBasicValue.REFERENCE_VALUE
                        }
                        else -> super.merge(v, w)
                    }

            override fun newOperation(insn: AbstractInsnNode): BasicValue? =
                    refValuesByNewInsn[insn] ?: super.newOperation(insn)

            override fun copyOperation(insn: AbstractInsnNode, value: BasicValue): BasicValue? {
                if (value is RefValue) {
                    if (processRefValueUsage(value, insn, 0)) {
                        return value
                    }
                    value.hazard = Hazard.UNEXPECTED_USAGE
                }
                return super.copyOperation(insn, value)
            }

            override fun unaryOperation(insn: AbstractInsnNode, value: BasicValue): BasicValue? {
                checkRefValuesUsages(insn, listOf(value))
                return super.unaryOperation(insn, value)
            }

            override fun binaryOperation(insn: AbstractInsnNode, value1: BasicValue, value2: BasicValue): BasicValue? {
                checkRefValuesUsages(insn, listOf(value1, value2))
                return super.binaryOperation(insn, value1, value2)
            }

            override fun ternaryOperation(insn: AbstractInsnNode, value1: BasicValue, value2: BasicValue, value3: BasicValue): BasicValue? {
                checkRefValuesUsages(insn, listOf(value1, value2, value3))
                return super.ternaryOperation(insn, value1, value2, value3)
            }

            override fun naryOperation(insn: AbstractInsnNode, values: List<BasicValue>): BasicValue? {
                checkRefValuesUsages(insn, values)
                return super.naryOperation(insn, values)
            }

            private fun checkRefValuesUsages(insn: AbstractInsnNode, values: List<BasicValue>) {
                values.forEachIndexed { pos, value ->
                    if (value is RefValue && !processRefValueUsage(value, insn, pos)) {
                        value.hazard = Hazard.UNEXPECTED_USAGE
                    }
                }
            }

            private fun processRefValueUsage(value: RefValue, insn: AbstractInsnNode, position: Int) =
                    when (insn.opcode) {
                        Opcodes.ALOAD -> {
                            value.aloadInsns.add(insn as VarInsnNode)
                            true
                        }
                        Opcodes.ASTORE -> {
                            value.astoreInsns.add(insn as VarInsnNode)
                            true
                        }
                        Opcodes.GETFIELD -> {
                            if (position == 0 && insn is FieldInsnNode && insn.name == REF_ELEMENT_FIELD) {
                                value.getFieldInsns.add(insn)
                                true
                            }
                            else
                                false
                        }
                        Opcodes.PUTFIELD ->
                            if (position == 0 && insn is FieldInsnNode && insn.name == REF_ELEMENT_FIELD) {
                                value.putFieldInsns.add(insn)
                                true
                            }
                            else {
                                false
                            }
                        Opcodes.INVOKESPECIAL ->
                            if (position == 0 && insn is MethodInsnNode && insn.name == INIT_METHOD_NAME) {
                                val anotherCallInsn = value.initCallInsn != null && value.initCallInsn != insn
                                value.initCallInsn = insn
                                !anotherCallInsn
                            }
                            else
                                false
                        Opcodes.DUP -> {
                            value.stackInsns.add(insn)
                            true
                        }
                        else -> false
                    }

        }

        private fun trackPops() {
            for (i in insns.indices) {
                val frame = frames[i] ?: continue
                val insn = insns[i]

                when (insn.opcode) {
                    Opcodes.POP -> {
                        val top = frame.top()
                        if (top is RefValue) {
                            top.stackInsns.add(insn)
                        }
                    }
                    Opcodes.POP2 -> {
                        val top = frame.top()
                        if (top?.size == 1) {
                            if (top is RefValue) top.hazard = Hazard.UNEXPECTED_STACK_OP
                            val next = frame.peek(1)
                            if (next is RefValue) next.hazard = Hazard.UNEXPECTED_STACK_OP
                        }
                    }
                }
            }
        }

        private fun assignLocalVars() {
            for (localVar in methodNode.localVariables) {
                if (!localVar.desc.startsWith("L")) continue
                val type = Type.getType(localVar.desc)
                if (!AsmTypes.isSharedVarType(type)) continue

                val startFrame = frames[localVar.start.getIndex()] ?: continue
                val refValue = startFrame.getLocal(localVar.index) as? RefValue ?: continue
                if (refValue.hasHazard) continue

                if (refValue.localVar == null) {
                    refValue.localVar = localVar
                }
                else {
                    refValue.hazard = Hazard.LOCAL_VAR_CONFLICT
                }
            }

            for (refValue in refValues) {
                if (refValue.hasHazard) continue
                val localVar = refValue.localVar ?: continue

                val localVarIndex = localVar.index
                if (refValue.aloadInsns.any { it.`var` != localVarIndex } || refValue.astoreInsns.any { it.`var` != localVarIndex }) {
                    refValue.hazard = Hazard.ESCAPES_TO_LOCAL
                    continue
                }
                refValue.localVarIndex = localVarIndex

                val startIndex = localVar.start.getIndex()
                val initFieldInsns = refValue.putFieldInsns.filter { it.getIndex() < startIndex }
                if (initFieldInsns.size != 1) {
                    refValue.hazard = Hazard.STRANGE_INITIALIZATION
                    continue
                }
            }
        }

        private fun rewrite() {
            for (refValue in refValues) {
                if (!refValue.canRewrite()) continue
                if (refValue.valueType.size != 1) continue // TODO support Long and Double: need a different local variable
                rewriteRefValue(refValue)
            }

            methodNode.removeEmptyCatchBlocks()
        }

        private fun rewriteRefValue(refValue: RefValue) {
            methodNode.instructions.run {
                refValue.localVar!!.let {
                    assert(it.signature == null) { "Non-null signature for local var $it" }
                    it.desc = refValue.valueType.descriptor
                }

                remove(refValue.newInsn)
                remove(refValue.initCallInsn!!)
                refValue.stackInsns.forEach { remove(it) }
                refValue.aloadInsns.forEach { remove(it) }
                refValue.astoreInsns.forEach { remove(it) }

                refValue.getFieldInsns.forEach {
                    insert(it, VarInsnNode(refValue.valueType.getOpcode(Opcodes.ILOAD), refValue.localVarIndex))
                    remove(it)
                }

                refValue.putFieldInsns.forEach {
                    insert(it, VarInsnNode(refValue.valueType.getOpcode(Opcodes.ISTORE), refValue.localVarIndex))
                    remove(it)
                }
            }
        }
    }
}

internal const val REF_ELEMENT_FIELD = "element"
internal const val INIT_METHOD_NAME = "<init>"

internal val REF_TYPE_TO_ELEMENT_TYPE = HashMap<String, Type>().apply {
    put(AsmTypes.OBJECT_REF_TYPE.internalName, AsmTypes.OBJECT_TYPE)
    PrimitiveType.values().forEach {
        put(AsmTypes.sharedTypeForPrimitive(it).internalName, AsmTypes.valueTypeForPrimitive(it));
    }
}

