package dk.zlepper.itlt.client.helpers;

public final class Message {

    public enum Type {
        Wants, Needs
    }

    public enum Subject {
        Java, Memory
    }

    public enum Desire {
        SixtyFourBit, More, Less, Newer, Older
    }

    public enum Content {
        NeedsJava64bit(Type.Needs, Desire.SixtyFourBit, Subject.Java),
        WantsJava64bit(Type.Wants, Desire.SixtyFourBit, Subject.Java),
        NeedsMoreMemory(Type.Needs, Desire.More, Subject.Memory),
        WantsMoreMemory(Type.Wants, Desire.More, Subject.Memory),
        NeedsLessMemory(Type.Needs, Desire.Less, Subject.Memory),
        WantsLessMemory(Type.Wants, Desire.Less, Subject.Memory),
        NeedsNewerJava(Type.Needs, Desire.Newer, Subject.Java),
        WantsNewerJava(Type.Wants, Desire.Newer, Subject.Java),
        NeedsOlderJava(Type.Needs, Desire.Older, Subject.Java),
        WantsOlderJava(Type.Wants, Desire.Older, Subject.Java);

        public Type msgType;
        public Desire msgDesire;
        public Subject msgSubject;
        Content(final Type msgType, final Desire msgDesire, final Subject msgSubject) {
            this.msgType = msgType;
            this.msgDesire = msgDesire;
            this.msgSubject = msgSubject;
        }
    }
}