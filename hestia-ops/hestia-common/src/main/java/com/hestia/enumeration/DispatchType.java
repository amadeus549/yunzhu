package com.hestia.enumeration;

/**
 * 派单方式
 */
public enum DispatchType {

    /** 手动派单 */
    MANUAL(1, "手动派单"),
    /** 智能派单 */
    AUTO(2, "智能派单");

    private final int code;
    private final String desc;

    DispatchType(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public int getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    public static DispatchType of(Integer code) {
        if (code == null) {
            throw new IllegalArgumentException("派单方式不能为空");
        }
        for (DispatchType e : values()) {
            if (e.code == code) {
                return e;
            }
        }
        throw new IllegalArgumentException("非法的派单方式: " + code);
    }

    public static String descOf(Integer code) {
        if (code == null) {
            return "";
        }
        for (DispatchType e : values()) {
            if (e.code == code) {
                return e.desc;
            }
        }
        return "";
    }
}
