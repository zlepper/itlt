function initializeCoreMod() {
    var opcodes = Java.type("org.objectweb.asm.Opcodes");
    var InsnNode = Java.type("org.objectweb.asm.tree.InsnNode");

    return {
        "CustomWindowTitle": {
            "target": {
                "type": "METHOD",
                "class": "net.minecraft.client.Minecraft",
                "methodName": "func_230150_b_",
                "methodDesc": "()V"
            },
            "transformer": function(methodNode){
                methodNode.instructions.insert(new InsnNode(opcodes.RETURN));
                return methodNode;
            }
        }
    };
}