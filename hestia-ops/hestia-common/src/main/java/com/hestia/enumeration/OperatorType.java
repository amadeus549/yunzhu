package com.hestia.enumeration;

/**
 * 操作人类型
 */
public enum OperatorType {

    /** 员工 */
    STAFF(1, "员工"),
    /** 住户 */
    RESIDENT(2, "住户"),
    /** 系统 */
    SYSTEM(3, "系统");

    private final int code;
    private final String desc;

    OperatorType(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public int getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    public static OperatorType of(Integer code) {
        if (code == null) {
            throw new IllegalArgumentException("操作人类型不能为空");
        }
        for (OperatorType e : values()) {
            if (e.code == code) {
                return e;
            }
        }
        throw new IllegalArgumentException("非法的操作人类型: " + code);
    }

    public static String descOf(Integer code) {
        if (code == null) {
            return "";
        }
        for (OperatorType e : values()) {
            if (e.code == code) {
                return e.desc;
            }
        }
        return "";
    }
}
