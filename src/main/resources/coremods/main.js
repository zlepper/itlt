function initializeCoreMod() {
    var opcodes = Java.type("org.objectweb.asm.Opcodes");
    var InsnNode = Java.type("org.objectweb.asm.tree.InsnNode");

    // Author: Paint_Ninja
    // Reason: Restores the ability to change the Minecraft window title
    // Todo: Make a Forge PR for a "WindowTitleChangedEvent" or similar
    return {
        "CustomWindowTitle": {
            "target": {
                "type": "METHOD",
                "class": "net.minecraft.client.Minecraft",
                "methodName": "updateTitle",
                "methodDesc": "()V"
            },
            "transformer": function(methodNode) {
                methodNode.instructions.insert(new InsnNode(opcodes.RETURN));
                return methodNode;
            }
        }
    };
}