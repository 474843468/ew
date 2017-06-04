package com.think.firewaiter.dao;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;

import de.greenrobot.dao.AbstractDao;
import de.greenrobot.dao.Property;
import de.greenrobot.dao.internal.DaoConfig;

import com.think.firewaiter.module.PxProductCategory;

// THIS CODE IS GENERATED BY greenDAO, DO NOT EDIT.
/** 
 * DAO for table "ProductCategory".
*/
public class PxProductCategoryDao extends AbstractDao<PxProductCategory, Long> {

    public static final String TABLENAME = "ProductCategory";

    /**
     * Properties of entity PxProductCategory.<br/>
     * Can be used for QueryBuilder and for referencing column names.
    */
    public static class Properties {
        public final static Property Id = new Property(0, Long.class, "id", true, "_id");
        public final static Property ObjectId = new Property(1, String.class, "objectId", false, "OBJECT_ID");
        public final static Property OrderNo = new Property(2, Integer.class, "orderNo", false, "ORDER_NO");
        public final static Property Code = new Property(3, String.class, "code", false, "CODE");
        public final static Property Version = new Property(4, Integer.class, "version", false, "VERSION");
        public final static Property Name = new Property(5, String.class, "name", false, "NAME");
        public final static Property DelFlag = new Property(6, String.class, "delFlag", false, "DEL_FLAG");
        public final static Property ParentId = new Property(7, String.class, "parentId", false, "PARENT_ID");
        public final static Property Leaf = new Property(8, String.class, "leaf", false, "LEAF");
        public final static Property Type = new Property(9, String.class, "type", false, "TYPE");
        public final static Property Shelf = new Property(10, String.class, "shelf", false, "SHELF");
        public final static Property Visible = new Property(11, String.class, "visible", false, "VISIBLE");
    };

    private DaoSession daoSession;


    public PxProductCategoryDao(DaoConfig config) {
        super(config);
    }
    
    public PxProductCategoryDao(DaoConfig config, DaoSession daoSession) {
        super(config, daoSession);
        this.daoSession = daoSession;
    }

    /** Creates the underlying database table. */
    public static void createTable(SQLiteDatabase db, boolean ifNotExists) {
        String constraint = ifNotExists? "IF NOT EXISTS ": "";
        db.execSQL("CREATE TABLE " + constraint + "\"ProductCategory\" (" + //
                "\"_id\" INTEGER PRIMARY KEY ASC ," + // 0: id
                "\"OBJECT_ID\" TEXT," + // 1: objectId
                "\"ORDER_NO\" INTEGER," + // 2: orderNo
                "\"CODE\" TEXT," + // 3: code
                "\"VERSION\" INTEGER," + // 4: version
                "\"NAME\" TEXT," + // 5: name
                "\"DEL_FLAG\" TEXT," + // 6: delFlag
                "\"PARENT_ID\" TEXT," + // 7: parentId
                "\"LEAF\" TEXT," + // 8: leaf
                "\"TYPE\" TEXT," + // 9: type
                "\"SHELF\" TEXT," + // 10: shelf
                "\"VISIBLE\" TEXT);"); // 11: visible
    }

    /** Drops the underlying database table. */
    public static void dropTable(SQLiteDatabase db, boolean ifExists) {
        String sql = "DROP TABLE " + (ifExists ? "IF EXISTS " : "") + "\"ProductCategory\"";
        db.execSQL(sql);
    }

    /** @inheritdoc */
    @Override
    protected void bindValues(SQLiteStatement stmt, PxProductCategory entity) {
        stmt.clearBindings();
 
        Long id = entity.getId();
        if (id != null) {
            stmt.bindLong(1, id);
        }
 
        String objectId = entity.getObjectId();
        if (objectId != null) {
            stmt.bindString(2, objectId);
        }
 
        Integer orderNo = entity.getOrderNo();
        if (orderNo != null) {
            stmt.bindLong(3, orderNo);
        }
 
        String code = entity.getCode();
        if (code != null) {
            stmt.bindString(4, code);
        }
 
        Integer version = entity.getVersion();
        if (version != null) {
            stmt.bindLong(5, version);
        }
 
        String name = entity.getName();
        if (name != null) {
            stmt.bindString(6, name);
        }
 
        String delFlag = entity.getDelFlag();
        if (delFlag != null) {
            stmt.bindString(7, delFlag);
        }
 
        String parentId = entity.getParentId();
        if (parentId != null) {
            stmt.bindString(8, parentId);
        }
 
        String leaf = entity.getLeaf();
        if (leaf != null) {
            stmt.bindString(9, leaf);
        }
 
        String type = entity.getType();
        if (type != null) {
            stmt.bindString(10, type);
        }
 
        String shelf = entity.getShelf();
        if (shelf != null) {
            stmt.bindString(11, shelf);
        }
 
        String visible = entity.getVisible();
        if (visible != null) {
            stmt.bindString(12, visible);
        }
    }

    @Override
    protected void attachEntity(PxProductCategory entity) {
        super.attachEntity(entity);
        entity.__setDaoSession(daoSession);
    }

    /** @inheritdoc */
    @Override
    public Long readKey(Cursor cursor, int offset) {
        return cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0);
    }    

    /** @inheritdoc */
    @Override
    public PxProductCategory readEntity(Cursor cursor, int offset) {
        PxProductCategory entity = new PxProductCategory( //
            cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0), // id
            cursor.isNull(offset + 1) ? null : cursor.getString(offset + 1), // objectId
            cursor.isNull(offset + 2) ? null : cursor.getInt(offset + 2), // orderNo
            cursor.isNull(offset + 3) ? null : cursor.getString(offset + 3), // code
            cursor.isNull(offset + 4) ? null : cursor.getInt(offset + 4), // version
            cursor.isNull(offset + 5) ? null : cursor.getString(offset + 5), // name
            cursor.isNull(offset + 6) ? null : cursor.getString(offset + 6), // delFlag
            cursor.isNull(offset + 7) ? null : cursor.getString(offset + 7), // parentId
            cursor.isNull(offset + 8) ? null : cursor.getString(offset + 8), // leaf
            cursor.isNull(offset + 9) ? null : cursor.getString(offset + 9), // type
            cursor.isNull(offset + 10) ? null : cursor.getString(offset + 10), // shelf
            cursor.isNull(offset + 11) ? null : cursor.getString(offset + 11) // visible
        );
        return entity;
    }
     
    /** @inheritdoc */
    @Override
    public void readEntity(Cursor cursor, PxProductCategory entity, int offset) {
        entity.setId(cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0));
        entity.setObjectId(cursor.isNull(offset + 1) ? null : cursor.getString(offset + 1));
        entity.setOrderNo(cursor.isNull(offset + 2) ? null : cursor.getInt(offset + 2));
        entity.setCode(cursor.isNull(offset + 3) ? null : cursor.getString(offset + 3));
        entity.setVersion(cursor.isNull(offset + 4) ? null : cursor.getInt(offset + 4));
        entity.setName(cursor.isNull(offset + 5) ? null : cursor.getString(offset + 5));
        entity.setDelFlag(cursor.isNull(offset + 6) ? null : cursor.getString(offset + 6));
        entity.setParentId(cursor.isNull(offset + 7) ? null : cursor.getString(offset + 7));
        entity.setLeaf(cursor.isNull(offset + 8) ? null : cursor.getString(offset + 8));
        entity.setType(cursor.isNull(offset + 9) ? null : cursor.getString(offset + 9));
        entity.setShelf(cursor.isNull(offset + 10) ? null : cursor.getString(offset + 10));
        entity.setVisible(cursor.isNull(offset + 11) ? null : cursor.getString(offset + 11));
     }
    
    /** @inheritdoc */
    @Override
    protected Long updateKeyAfterInsert(PxProductCategory entity, long rowId) {
        entity.setId(rowId);
        return rowId;
    }
    
    /** @inheritdoc */
    @Override
    public Long getKey(PxProductCategory entity) {
        if(entity != null) {
            return entity.getId();
        } else {
            return null;
        }
    }

    /** @inheritdoc */
    @Override    
    protected boolean isEntityUpdateable() {
        return true;
    }
    
}
