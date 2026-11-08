package com.hestia.enumeration;

/**
 * 工单来源
 */
public enum WorkOrderSource {

    /** 住户自助 */
    RESIDENT(1, "住户自助"),
    /** 管家代报 */
    HOUSEKEEPER(2, "管家代报"),
    /** 巡检发现 */
    INSPECTION(3, "巡检发现"),
    /** 电话报修 */
    PHONE(4, "电话报修");

    private final int code;
    private final String desc;

    WorkOrderSource(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public int getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    public static WorkOrderSource of(Integer code) {
        if (code == null) {
            throw new IllegalArgumentException("工单来源不能为空");
        }
        for (WorkOrderSource e : values()) {
            if (e.code == code) {
                return e;
            }
        }
        throw new IllegalArgumentException("非法的工单来源: " + code);
    }

    public static String descOf(Integer code) {
        if (code == null) {
            return "";
        }
        for (WorkOrderSource e : values()) {
            if (e.code == code) {
                return e.desc;
            }
        }
        return "";
    }
}
