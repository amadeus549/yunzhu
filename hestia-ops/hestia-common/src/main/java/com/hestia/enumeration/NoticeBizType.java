package com.hestia.enumeration;

/**
 * 通知业务类型
 */
public enum NoticeBizType {

    /** 派单通知 */
    DISPATCH(1, "派单通知"),
    /** 超时预警 */
    SLA_WARNING(2, "超时预警"),
    /** 验收提醒 */
    ACCEPTANCE(3, "验收提醒"),
    /** 系统通知 */
    SYSTEM(4, "系统通知");

    private final int code;
    private final String desc;

    NoticeBizType(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public int getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    public static NoticeBizType of(Integer code) {
        if (code == null) {
            throw new IllegalArgumentException("通知业务类型不能为空");
        }
        for (NoticeBizType e : values()) {
            if (e.code == code) {
                return e;
            }
        }
        throw new IllegalArgumentException("非法的通知业务类型: " + code);
    }

    public static String descOf(Integer code) {
        if (code == null) {
            return "";
        }
        for (NoticeBizType e : values()) {
            if (e.code == code) {
                return e.desc;
            }
        }
        return "";
    }
}
