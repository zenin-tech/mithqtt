package io.j1st.power.storage.mongo.entity;

/**
 * Agent status
 */
public enum AgentStatus {

    INIT(1),                  //初始化
    IN_DEVELOPER_ORDER(2),    //初始化
    TO_OPERATOR(3),           //分配到operator
    TO_INSTALLER(4),          //分配到installerd
    INSTALL_ING(5),           //安装中
    INSTALL_SUCCESS(6),       //安装完成
    TEST(7),                  //测试中（）
    COMPLETE(8),              //验收完成
    DISABLED(10);             //停用

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
