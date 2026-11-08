package com.hestia.enumeration;

/**
 * 巡检任务状态
 */
public enum InspectionTaskStatus {

    /** 待执行 */
    PENDING(1, "待执行"),
    /** 已完成 */
    COMPLETED(2, "已完成"),
    /** 已逾期 */
    OVERDUE(3, "已逾期");

    private final int code;
    private final String desc;

    InspectionTaskStatus(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public int getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    public static InspectionTaskStatus of(Integer code) {
        if (code == null) {
            throw new IllegalArgumentException("巡检任务状态不能为空");
        }
        for (InspectionTaskStatus e : values()) {
            if (e.code == code) {
                return e;
            }
        }
        throw new IllegalArgumentException("非法的巡检任务状态: " + code);
    }

    public static String descOf(Integer code) {
        if (code == null) {
            return "";
        }
        for (InspectionTaskStatus e : values()) {
            if (e.code == code) {
                return e.desc;
            }
        }
        return "";
    }
}
