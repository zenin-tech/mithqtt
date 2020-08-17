package io.j1st.power.storage.mongo.entity;

/**
 * Agent status
 */
public enum AgentStatus {

    ENABLE(1),       //正常
    DISABLED(2);     //停用

    private final int value;

    AgentStatus(int value) {
        this.value = value;
    }

    public static AgentStatus valueOf(int value) {
        for (AgentStatus t : values()) {
            if (t.value == value) {
                return t;
            }
        }
        return null;
    }

    public int value() {
        return value;
    }
}
