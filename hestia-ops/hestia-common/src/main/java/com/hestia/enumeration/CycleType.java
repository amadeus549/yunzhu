package com.hestia.enumeration;

/**
 * 巡检周期类型
 */
public enum CycleType {

    /** 每日 */
    DAILY(1, "每日"),
    /** 每周 */
    WEEKLY(2, "每周"),
    /** 每月 */
    MONTHLY(3, "每月"),
    /** 自定义 */
    CUSTOM(4, "自定义");

    private final int code;
    private final String desc;

    CycleType(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public int getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    public static CycleType of(Integer code) {
        if (code == null) {
            throw new IllegalArgumentException("巡检周期类型不能为空");
        }
        for (CycleType e : values()) {
            if (e.code == code) {
                return e;
            }
        }
        throw new IllegalArgumentException("非法的巡检周期类型: " + code);
    }

    public static String descOf(Integer code) {
        if (code == null) {
            return "";
        }
        for (CycleType e : values()) {
            if (e.code == code) {
                return e.desc;
            }
        }
        return "";
    }
}
