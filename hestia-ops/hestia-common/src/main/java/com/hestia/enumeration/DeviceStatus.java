package com.hestia.enumeration;

/**
 * 设备状态
 */
public enum DeviceStatus {

    /** 正常 */
    NORMAL(1, "正常"),
    /** 维修中 */
    REPAIRING(2, "维修中"),
    /** 停用 */
    DISABLED(3, "停用"),
    /** 报废 */
    SCRAPPED(4, "报废");

    private final int code;
    private final String desc;

    DeviceStatus(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public int getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    public static DeviceStatus of(Integer code) {
        if (code == null) {
            throw new IllegalArgumentException("设备状态不能为空");
        }
        for (DeviceStatus e : values()) {
            if (e.code == code) {
                return e;
            }
        }
        throw new IllegalArgumentException("非法的设备状态: " + code);
    }

    public static String descOf(Integer code) {
        if (code == null) {
            return "";
        }
        for (DeviceStatus e : values()) {
            if (e.code == code) {
                return e.desc;
            }
        }
        return "";
    }
}
