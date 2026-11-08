package com.hestia.enumeration;

/**
 * 巡检结果
 */
public enum InspectionResult {

    /** 正常 */
    NORMAL(1, "正常"),
    /** 异常 */
    ABNORMAL(2, "异常"),
    /** 未执行 */
    NOT_EXECUTED(3, "未执行");

    private final int code;
    private final String desc;

    InspectionResult(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public int getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    public static InspectionResult of(Integer code) {
        if (code == null) {
            throw new IllegalArgumentException("巡检结果不能为空");
        }
        for (InspectionResult e : values()) {
            if (e.code == code) {
                return e;
            }
        }
        throw new IllegalArgumentException("非法的巡检结果: " + code);
    }

    public static String descOf(Integer code) {
        if (code == null) {
            return "";
        }
        for (InspectionResult e : values()) {
            if (e.code == code) {
                return e.desc;
            }
        }
        return "";
    }
}
