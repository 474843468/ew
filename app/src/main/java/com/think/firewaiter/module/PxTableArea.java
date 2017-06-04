package com.think.firewaiter.module;

// THIS CODE IS GENERATED BY greenDAO, EDIT ONLY INSIDE THE "KEEP"-SECTIONS

// KEEP INCLUDES - put your custom includes here
// KEEP INCLUDES END

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * 桌台区域
 */
public class PxTableArea implements java.io.Serializable {

    private Long id;
    /**
     * 虚拟删除 0：正常 1：删除 2：审核
     */
     @Expose
    private String delFlag;
    /**
     * 对应服务器id
     */
     @SerializedName("id") @Expose
    private String objectId;
    /**
     * 区域(0:大厅，1包厢)
     */
     @Expose
    private String type;
    /**
     * 名称
     */
     @Expose
    private String name;

    // KEEP FIELDS - put your custom fields here
    // KEEP FIELDS END

    public PxTableArea() {
    }

    public PxTableArea(Long id) {
        this.id = id;
    }

    public PxTableArea(Long id, String delFlag, String objectId, String type, String name) {
        this.id = id;
        this.delFlag = delFlag;
        this.objectId = objectId;
        this.type = type;
        this.name = name;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDelFlag() {
        return delFlag;
    }

    public void setDelFlag(String delFlag) {
        this.delFlag = delFlag;
    }

    public String getObjectId() {
        return objectId;
    }

    public void setObjectId(String objectId) {
        this.objectId = objectId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    // KEEP METHODS - put your custom methods here
    // KEEP METHODS END

}
