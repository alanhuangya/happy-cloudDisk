package com.alan.entity.enums;

/**
 * 用户状态枚举, 0:禁用, 1:启用
 */
public enum UserStatusEnum {
    DISABLE(0, "禁用"),
    ENABLE(1, "启用");

    /**
     * 用户状态
     */
    private Integer status;

    /**
     * 描述，用于前端展示
     */
    private String desc;

    UserStatusEnum(Integer status, String desc) {
        this.status = status;
        this.desc = desc;
    }

    public static UserStatusEnum getByStatus(Integer status) {
        // 遍历枚举值
        for (UserStatusEnum item : UserStatusEnum.values()) {
            if (item.getStatus().equals(status)) {
                return item;
            }
        }
        return null;
    }

    public Integer getStatus() {
        return status;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

}
