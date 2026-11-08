package com.hestia.enumeration;

/**
 * 员工角色
 */
public enum StaffRole {

    /** 管家 */
    HOUSEKEEPER(1, "管家"),
    /** 工程师 */
    ENGINEER(2, "工程师"),
    /** 主管 */
    SUPERVISOR(3, "主管"),
    /** 客服 */
    SERVICE(4, "客服");

    private final int code;
    private final String desc;

    StaffRole(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public int getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    public static StaffRole of(Integer code) {
        if (code == null) {
            throw new IllegalArgumentException("员工角色不能为空");
        }
        for (StaffRole e : values()) {
            if (e.code == code) {
                return e;
            }
        }
        throw new IllegalArgumentException("非法的员工角色: " + code);
    }

    public static String descOf(Integer code) {
        if (code == null) {
            return "";
        }
        for (StaffRole e : values()) {
            if (e.code == code) {
                return e.desc;
            }
        }
        return "";
    }
}
