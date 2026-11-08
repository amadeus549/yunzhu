package com.hestia.enumeration;

/**
 * 工单优先级
 */
public enum WorkOrderPriority {

    /** 低 */
    LOW(1, "低"),
    /** 普通 */
    NORMAL(2, "普通"),
    /** 高 */
    HIGH(3, "高"),
    /** 紧急 */
    URGENT(4, "紧急");

    private final int code;
    private final String desc;

    WorkOrderPriority(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public int getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    public static WorkOrderPriority of(Integer code) {
        if (code == null) {
            throw new IllegalArgumentException("工单优先级不能为空");
        }
        for (WorkOrderPriority e : values()) {
            if (e.code == code) {
                return e;
            }
        }
        throw new IllegalArgumentException("非法的工单优先级: " + code);
    }

    public static String descOf(Integer code) {
        if (code == null) {
            return "";
        }
        for (WorkOrderPriority e : values()) {
            if (e.code == code) {
                return e.desc;
            }
        }
        return "";
    }
}
