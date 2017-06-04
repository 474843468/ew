package com.think.firewaiter.module;

import com.think.firewaiter.dao.DaoSession;
import de.greenrobot.dao.DaoException;

import com.think.firewaiter.dao.PxMethodInfoDao;
import com.think.firewaiter.dao.PxProductInfoDao;
import com.think.firewaiter.dao.PxProductMethodRefDao;

// THIS CODE IS GENERATED BY greenDAO, EDIT ONLY INSIDE THE "KEEP"-SECTIONS

// KEEP INCLUDES - put your custom includes here
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
// KEEP INCLUDES END
/**
 * Entity mapped to table "PxProductMethodRef".
 */
public class PxProductMethodRef implements java.io.Serializable {

    private Long id;
    /**
     * 对应服务器id
     */
     @SerializedName("id") @Expose
    private String objectId;
    /**
     * 虚拟删除 0：正常 1：删除 2：审核
     */
     @Expose
    private String delFlag;
    private Long pxMethodInfoId;
    private Long pxProductInfoId;

    /** Used to resolve relations */
    private transient DaoSession daoSession;

    /** Used for active entity operations. */
    private transient PxProductMethodRefDao myDao;

    private PxMethodInfo dbMethod;
    private Long dbMethod__resolvedKey;

    private PxProductInfo dbProduct;
    private Long dbProduct__resolvedKey;


    // KEEP FIELDS - put your custom fields here
    @Expose private PxMethodInfo method;    // 做法编号
    @Expose private PxProductInfo product;    // 商品编号
    // KEEP FIELDS END

    public PxProductMethodRef() {
    }

    public PxProductMethodRef(Long id) {
        this.id = id;
    }

    public PxProductMethodRef(Long id, String objectId, String delFlag, Long pxMethodInfoId, Long pxProductInfoId) {
        this.id = id;
        this.objectId = objectId;
        this.delFlag = delFlag;
        this.pxMethodInfoId = pxMethodInfoId;
        this.pxProductInfoId = pxProductInfoId;
    }

    /** called by internal mechanisms, do not call yourself. */
    public void __setDaoSession(DaoSession daoSession) {
        this.daoSession = daoSession;
        myDao = daoSession != null ? daoSession.getPxProductMethodRefDao() : null;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getObjectId() {
        return objectId;
    }

    public void setObjectId(String objectId) {
        this.objectId = objectId;
    }

    public String getDelFlag() {
        return delFlag;
    }

    public void setDelFlag(String delFlag) {
        this.delFlag = delFlag;
    }

    public Long getPxMethodInfoId() {
        return pxMethodInfoId;
    }

    public void setPxMethodInfoId(Long pxMethodInfoId) {
        this.pxMethodInfoId = pxMethodInfoId;
    }

    public Long getPxProductInfoId() {
        return pxProductInfoId;
    }

    public void setPxProductInfoId(Long pxProductInfoId) {
        this.pxProductInfoId = pxProductInfoId;
    }

    /** To-one relationship, resolved on first access. */
    public PxMethodInfo getDbMethod() {
        Long __key = this.pxMethodInfoId;
        if (dbMethod__resolvedKey == null || !dbMethod__resolvedKey.equals(__key)) {
            if (daoSession == null) {
                throw new DaoException("Entity is detached from DAO context");
            }
            PxMethodInfoDao targetDao = daoSession.getPxMethodInfoDao();
            PxMethodInfo dbMethodNew = targetDao.load(__key);
            synchronized (this) {
                dbMethod = dbMethodNew;
            	dbMethod__resolvedKey = __key;
            }
        }
        return dbMethod;
    }

    public void setDbMethod(PxMethodInfo dbMethod) {
        synchronized (this) {
            this.dbMethod = dbMethod;
            pxMethodInfoId = dbMethod == null ? null : dbMethod.getId();
            dbMethod__resolvedKey = pxMethodInfoId;
        }
    }

    /** To-one relationship, resolved on first access. */
    public PxProductInfo getDbProduct() {
        Long __key = this.pxProductInfoId;
        if (dbProduct__resolvedKey == null || !dbProduct__resolvedKey.equals(__key)) {
            if (daoSession == null) {
                throw new DaoException("Entity is detached from DAO context");
            }
            PxProductInfoDao targetDao = daoSession.getPxProductInfoDao();
            PxProductInfo dbProductNew = targetDao.load(__key);
            synchronized (this) {
                dbProduct = dbProductNew;
            	dbProduct__resolvedKey = __key;
            }
        }
        return dbProduct;
    }

    public void setDbProduct(PxProductInfo dbProduct) {
        synchronized (this) {
            this.dbProduct = dbProduct;
            pxProductInfoId = dbProduct == null ? null : dbProduct.getId();
            dbProduct__resolvedKey = pxProductInfoId;
        }
    }

    /** Convenient call for {@link AbstractDao#delete(Object)}. Entity must attached to an entity context. */
    public void delete() {
        if (myDao == null) {
            throw new DaoException("Entity is detached from DAO context");
        }    
        myDao.delete(this);
    }

    /** Convenient call for {@link AbstractDao#update(Object)}. Entity must attached to an entity context. */
    public void update() {
        if (myDao == null) {
            throw new DaoException("Entity is detached from DAO context");
        }    
        myDao.update(this);
    }

    /** Convenient call for {@link AbstractDao#refresh(Object)}. Entity must attached to an entity context. */
    public void refresh() {
        if (myDao == null) {
            throw new DaoException("Entity is detached from DAO context");
        }    
        myDao.refresh(this);
    }

    // KEEP METHODS - put your custom methods here

    public PxMethodInfo getMethod() {
        return method;
    }

    public void setMethod(PxMethodInfo method) {
        this.method = method;
    }

    public PxProductInfo getProduct() {
        return product;
    }

    public void setProduct(PxProductInfo product) {
        this.product = product;
    }
    // KEEP METHODS END

}
