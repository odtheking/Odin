package me.odinmain.lwjgl.plugin

import net.minecraft.launchwrapper.IClassTransformer
import org.objectweb.asm.*
import org.objectweb.asm.tree.*

class LWJGLClassTransformer : IClassTransformer {
    override fun transform(name: String, transformedName: String, basicClass: ByteArray): ByteArray {
        if (name == "org.lwjgl.nanovg.NanoVGGLConfig") {
            val reader = ClassReader(basicClass)
            val node = ClassNode()
            reader.accept(node, ClassReader.EXPAND_FRAMES)

            for (method in node.methods) {
                if (method.name == "configGL") {
                    val list = InsnList()

                    list.add(VarInsnNode(Opcodes.LLOAD, 0))
                    list.add(TypeInsnNode(Opcodes.NEW, "me/odinmain/lwjgl/LWJGLFunctionProvider"))
                    list.add(InsnNode(Opcodes.DUP))
                    list.add(
                        MethodInsnNode(
                            Opcodes.INVOKESPECIAL,
                            "me/odinmain/lwjgl/LWJGLFunctionProvider",
                            "<init>",
                            "()V",
                            false
                        )
                    )
                    list.add(
                        MethodInsnNode(
                            Opcodes.INVOKESTATIC,
                            "org/lwjgl/nanovg/NanoVGGLConfig",
                            "config",
                            "(JLorg/lwjgl/system/FunctionProvider;)V",
                            false
                        )
                    )
                    list.add(InsnNode(Opcodes.RETURN))

                    method.instructions.clear()
                    method.instructions.insert(list)
                }
            }
            val cw = ClassWriter(ClassWriter.COMPUTE_FRAMES)
            node.accept(cw)
            return cw.toByteArray()
        }
        return basicClass
    }
}