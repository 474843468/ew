package com.think.firewaiter.module;

import com.think.firewaiter.dao.DaoServiceUtil;
import com.think.firewaiter.dao.PxTableAreaDao;

/**
 * User: ylw
 * Date: 2017-02-14
 * Time: 14:31
 * FIXME
 */
public class TableAreaType {
  public static final String TYPE_HALL = "大厅";
  public static final String TYPE_PARLOF = "包厢";
  private int mNum;
  private String name;
  private String type;

  public String getType() {
    return type;
  }

  public TableAreaType() {
  }

  public TableAreaType(int num, String type) {
    mNum = num;
    this.type = type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public String getName() {
    if (type != null) {
      PxTableArea area = DaoServiceUtil.getTableAreaService()
          .queryBuilder()
          .where(PxTableAreaDao.Properties.Type.eq(type))
          .unique();
      return area == null ? "未知" : area.getName();
    }else{
      return "未知";
    }
  }

  public void setName(String name) {
    this.name = name;
  }

  public int getNum() {
    return mNum;
  }

  public void setNum(int num) {
    mNum = num;
  }
}