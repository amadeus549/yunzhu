package com.hestia.enumeration;

/**
 * 派单状态
 */
public enum DispatchStatus {

    /** 待接单 */
    PENDING(1, "待接单"),
    /** 已接单 */
    ACCEPTED(2, "已接单"),
    /** 已拒单 */
    REJECTED(3, "已拒单"),
    /** 已取消 */
    CANCELLED(4, "已取消");

    private final int code;
    private final String desc;

    DispatchStatus(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public int getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    public static DispatchStatus of(Integer code) {
        if (code == null) {
            throw new IllegalArgumentException("派单状态不能为空");
        }
        for (DispatchStatus e : values()) {
            if (e.code == code) {
                return e;
            }
        }
        throw new IllegalArgumentException("非法的派单状态: " + code);
    }

    public static String descOf(Integer code) {
        if (code == null) {
            return "";
        }
        for (DispatchStatus e : values()) {
            if (e.code == code) {
                return e.desc;
            }
        }
        return "";
    }
}
