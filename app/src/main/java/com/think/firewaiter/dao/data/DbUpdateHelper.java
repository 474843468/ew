package com.think.firewaiter.dao.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import com.think.firewaiter.dao.DaoMaster;

/**
 * Created by dorado on 2016/7/26.
 */
public class DbUpdateHelper extends DaoMaster.OpenHelper {
  public DbUpdateHelper(Context context, String name, SQLiteDatabase.CursorFactory factory) {
    super(context, name, factory);
  }

  @Override public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    if (1 >= oldVersion && 2 <= newVersion) {
      upgrade1To2(db);
    }
    if (2 >= oldVersion && 3 <= newVersion) {
      upgrade2To3(db);
    }
    if (3 >= oldVersion && 4 <= newVersion) {
      upgrade3To4(db);
    }
    if (4 >= oldVersion && 5 <= newVersion) {
      upgrade4To5(db);
    }
    if (5 >= oldVersion && 6 <= newVersion) {
      upgrade5To6(db);
    }
  }

  private void upgrade1To2(SQLiteDatabase db) {
    /**
     * User添加退菜权限
     */
    db.execSQL("ALTER TABLE 'User' ADD 'CAN_RETREAT' TEXT;");
    /**
     * 添加操作原因表
     */
    db.execSQL("CREATE TABLE " + "IF NOT EXISTS " + "\"OptReason\" (" +
        "\"_id\" INTEGER PRIMARY KEY ASC ," +
        "\"OBJECT_ID\" TEXT," +
        "\"NAME\" TEXT," +
        "\"TYPE\" TEXT," +
        "\"DEL_FLAG\" TEXT);");
  }

  private void upgrade2To3(SQLiteDatabase db) {
    /**
     * 商品添加剩余数量
     */
    db.execSQL("ALTER TABLE 'ProductInfo' ADD 'OVER_PLUS' REAL;");

    /**
     * 商品添加分类  type
     */
    db.execSQL("ALTER TABLE 'ProductInfo' ADD 'TYPE' TEXT DEFAULT '0';");

    /**
     * 商品分类添加分类 type
     */
    db.execSQL("ALTER TABLE 'ProductCategory' ADD 'TYPE' TEXT DEFAULT '0';");
  }

  private void upgrade3To4(SQLiteDatabase db) {
    /**
     * 规格添加余量
     */
    db.execSQL("ALTER TABLE 'ProductFormatRel' ADD 'STOCK' REAL;");

    /**
     * 规格添加状态
     */
    db.execSQL("ALTER TABLE 'ProductFormatRel' ADD 'STATUS' TEXT;");
  }

  private void upgrade4To5(SQLiteDatabase db) {
    /**
     * 商品添加上架状态
     */
    db.execSQL("ALTER TABLE 'ProductInfo' ADD 'SHELF' TEXT DEFAULT '0';");

    /**
     * 商品添加是否在微信点餐页面显示
     */
    db.execSQL("ALTER TABLE 'ProductInfo' ADD 'VISIBLE' TEXT DEFAULT '0';");

    /**
     * 分类添加上架状态
     */
    db.execSQL("ALTER TABLE 'ProductCategory' ADD 'SHELF' TEXT DEFAULT '0';");

    /**
     * 分类添加是否在微信点餐页面显示
     */
    db.execSQL("ALTER TABLE 'ProductCategory' ADD 'VISIBLE' TEXT DEFAULT '0';");

    /**
     * 添加商品备注表
     */
    db.execSQL("CREATE TABLE " + "IF NOT EXISTS " + "\"ProductRemarks\" (" +
        "\"_id\" INTEGER PRIMARY KEY ASC ," +
        "\"OBJECT_ID\" TEXT," +
        "\"DEL_FLAG\" TEXT," +
        "\"REMARKS\" TEXT);");
    /**
     * 购物车添加字段
     */
    db.execSQL("ALTER TABLE 'ShoppingCart' ADD 'REMARKS' TEXT;");
  }

  private void upgrade5To6(SQLiteDatabase db) {

    /**
     * 添加桌台区域
     */
    db.execSQL("CREATE TABLE " + "IF NOT EXISTS " + "\"PxTableArea\" (" +
        "\"_id\" INTEGER PRIMARY KEY ASC ," +
        "\"OBJECT_ID\" TEXT," +
        "\"NAME\" TEXT," +
        "\"TYPE\"  TEXT," +
        "\"DEL_FLAG\" TEXT );");

    /**
     * 添加促销计划
     */
    db.execSQL("CREATE TABLE " + "IF NOT EXISTS " + "\"PromotioInfo\" (" +
        "\"_id\" INTEGER PRIMARY KEY ASC ," +
        "\"OBJECT_ID\" TEXT," +
        "\"NAME\" TEXT," +
        "\"CODE\"  TEXT," +
        "\"TYPE\"  TEXT," +
        "\"WEEKLY\"  TEXT," +
        "\"START_TIME\"  TEXT," +
        "\"END_TIME\"  TEXT," +
        "\"START_DATE\"  INTEGER," +
        "\"END_DATE\"  INTEGER," +
        "\"DEL_FLAG\" TEXT );");

    /**
     * 添加促销计划详情
     */
    db.execSQL("CREATE TABLE " + "IF NOT EXISTS " + "\"PromotioDetails\" (" +
        "\"_id\" INTEGER PRIMARY KEY ASC ," +
        "\"OBJECT_ID\" TEXT," +
        "\"PROMOTIONAL_PRICE\" INTEGER," +
        "\"PX_FORMAT_ID\"  INTEGER," +
        "\"PX_PRODUCT_INFO_ID\"  INTEGER," +
        "\"PX_PROMOTIO_INFO_ID\"  INTEGER," +
        "\"DEL_FLAG\" TEXT );");
  }
}
