package com.think.firewaiter.dao.data;

import android.database.sqlite.SQLiteDatabase;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.orhanobut.logger.Logger;
import com.think.firewaiter.common.App;
import com.think.firewaiter.config.Setting;
import com.think.firewaiter.dao.DaoServiceUtil;
import com.think.firewaiter.dao.OfficeDao;
import com.think.firewaiter.dao.PxFormatInfoDao;
import com.think.firewaiter.dao.PxMethodInfoDao;
import com.think.firewaiter.dao.PxOptReasonDao;
import com.think.firewaiter.dao.PxProductCategoryDao;
import com.think.firewaiter.dao.PxProductFormatRelDao;
import com.think.firewaiter.dao.PxProductInfoDao;
import com.think.firewaiter.dao.PxProductMethodRefDao;
import com.think.firewaiter.dao.PxProductRemarksDao;
import com.think.firewaiter.dao.PxPromotioDetailsDao;
import com.think.firewaiter.dao.PxPromotioInfoDao;
import com.think.firewaiter.dao.PxTableAreaDao;
import com.think.firewaiter.dao.PxTableInfoDao;
import com.think.firewaiter.dao.UserDao;
import com.think.firewaiter.module.Office;
import com.think.firewaiter.module.PxFormatInfo;
import com.think.firewaiter.module.PxMethodInfo;
import com.think.firewaiter.module.PxOptReason;
import com.think.firewaiter.module.PxProductCategory;
import com.think.firewaiter.module.PxProductFormatRel;
import com.think.firewaiter.module.PxProductInfo;
import com.think.firewaiter.module.PxProductMethodRef;
import com.think.firewaiter.module.PxProductRemarks;
import com.think.firewaiter.module.PxPromotioDetails;
import com.think.firewaiter.module.PxPromotioInfo;
import com.think.firewaiter.module.PxTableArea;
import com.think.firewaiter.module.PxTableInfo;
import com.think.firewaiter.module.User;
import com.think.firewaiter.network.DataSync;
import com.think.firewaiter.network.RestClient;
import com.think.firewaiter.utils.SPUtils;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by zjq on 2016/4/6.
 */
public class DataLoader {

  //商品list
  private static List<PxProductInfo> mProductInfoList = new ArrayList<PxProductInfo>();
  //商品和规格关系list
  private static List<PxProductFormatRel> mProdFormatRelList = new ArrayList<PxProductFormatRel>();
  //商品和做法关系list
  private static List<PxProductMethodRef> mProdMethodRelList = new ArrayList<PxProductMethodRef>();
  //促销计划详情
  private static List<PxPromotioDetails> sPromDetailsList = new ArrayList<>();
  //单例
  private static DataLoader sDataLoader;

  //@formatter:on
  public static DataLoader getInstance() {
    if (sDataLoader == null) {
      sDataLoader = new DataLoader();
    }
    //商品list
    mProductInfoList = new ArrayList<PxProductInfo>();
    //商品和规格关系list
    mProdFormatRelList = new ArrayList<PxProductFormatRel>();
    //商品和做法关系list
    mProdMethodRelList = new ArrayList<PxProductMethodRef>();
    //促销计划详情
    sPromDetailsList = new ArrayList<>();
    return sDataLoader;
  }

  /**
   * 储存数据
   */
  public void saveData(List<DataSync> dataList) {
    if (dataList == null || dataList.size() == 0) return;
    SQLiteDatabase db = DaoServiceUtil.getOfficeDao().getDatabase();
    db.beginTransaction();
    try {
      for (DataSync dataSync : dataList) {
        Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create();
        String s = gson.toJson(dataSync);
        Logger.json("Data:" + s);
        operateData(dataSync);
      }
      //连接商品和分类
      linkProInfoAndProCate();
      //连接商品和规格信息
      linkProdAndFormat();
      //连接商品和做法信息
      linkProdAndMethod();
      //修改sp，是否初始化
      SPUtils.put(App.getContext(), Setting.IS_INSTALL, "1");
      db.setTransactionSuccessful();
    } catch (Exception e) {
      Logger.e(e.toString());
    } finally {
      db.endTransaction();
    }
  }

  /**
   * 具体操作
   */
  private void operateData(DataSync dataSync) {
    String simpleClassName = dataSync.getSimpleClassName();
    switch (simpleClassName) {
      case "PxProductCategory"://商品分类
        operatePxProductCategory(dataSync);
        break;
      case "PxProductInfo"://商品信息
        operatePxProductInfo(dataSync);
        break;
      case "PxTableInfo"://桌台
        operatePxTableInfo(dataSync);
        break;
      case "User"://用户
        operateUser(dataSync);
        break;
      case "Office"://公司
        operateOffice(dataSync);
        break;
      case "PxFormatInfo"://规格
        operateFormatInfo(dataSync);
        break;
      case "PxProductFormatRel"://商品规格引用关系
        operateProductFormatRel(dataSync);
        break;
      case "PxMethodInfo"://做法
        operateMethodInfo(dataSync);
        break;
      case "PxProductMethodRef"://商品做法引用关系
        operateProductMethodRel(dataSync);
        break;
      case "PxOptReason"://操作原因
        operateOptReason(dataSync);
        break;
      case "PxProductRemarks"://商品备注
        operateProdRemarks(dataSync);
        break;
      case "PxTableArea"://桌台区域
        operateTableArea(dataSync);
        break;
      case "PxPromotioInfo"://促销信息
        operatePromotionInfo(dataSync);
        break;
      case "PxPromotioDetails"://促销详情
        operatePromotionDetails(dataSync);
        break;
    }
  }

  /**
   * 连接商品和分类
   */
  private void linkProInfoAndProCate() {
    for (PxProductInfo productInfo : mProductInfoList) {
      for (PxProductCategory category : DaoServiceUtil.getProductCategoryDao().loadAll()) {
        //该商品所属分类id
        String proCateId = productInfo.getCategory().getObjectId();
        //分类id
        String cateId = category.getObjectId();
        //如果两个id相等，则表示此商品属于该分类
        if (!proCateId.equals(cateId)) continue;
        productInfo.setDbCategory(category);
        DaoServiceUtil.getProductInfoService().update(productInfo);
      }
    }
  }

  /**
   * 连接商品和规格
   */
  private void linkProdAndFormat() {
    for (PxProductFormatRel rel : mProdFormatRelList) {
      String prodObjId = rel.getProduct().getObjectId();
      String formatObjId = rel.getFormat().getObjectId();
      PxProductInfo productInfo = DaoServiceUtil.getProductInfoService()
          .queryBuilder()
          .where(PxProductInfoDao.Properties.ObjectId.eq(prodObjId))
          .unique();
      PxFormatInfo formatInfo = DaoServiceUtil.getFormatInfoService()
          .queryBuilder()
          .where(PxFormatInfoDao.Properties.ObjectId.eq(formatObjId))
          .unique();
      rel.setDbProduct(productInfo);
      rel.setDbFormat(formatInfo);
      DaoServiceUtil.getProductFormatRelServiceService().update(rel);
    }
  }

  /**
   * 连接商品和做法
   */
  private void linkProdAndMethod() {
    for (PxProductMethodRef rel : mProdMethodRelList) {
      String prodObjId = rel.getProduct().getObjectId();
      String methodObjId = rel.getMethod().getObjectId();
      PxProductInfo productInfo = DaoServiceUtil.getProductInfoService()
          .queryBuilder()
          .where(PxProductInfoDao.Properties.ObjectId.eq(prodObjId))
          .unique();
      PxMethodInfo methodInfo = DaoServiceUtil.getMethodInfoService()
          .queryBuilder()
          .where(PxMethodInfoDao.Properties.ObjectId.eq(methodObjId))
          .unique();
      rel.setDbProduct(productInfo);
      rel.setDbMethod(methodInfo);
      DaoServiceUtil.getProductMethodRelService().update(rel);
    }
  }

  /**
   * 公用的转化方法
   */
  public String getDataJson(DataSync dataSync) {
    //objData不能向下转型
    Object objData = dataSync.getData();
    Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create();
    String dataJson = gson.toJson(objData);
    return dataJson;
  }

  /**
   * 用户转化方法
   */
  public String[] getDataJsonByUser(DataSync dataSync) {
    //objData不能向下转型
    Object objData = dataSync.getData();
    Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create();
    String dataJson = gson.toJson(objData);
    String companyCode = dataSync.getCompanyCode();
    String[] strings = new String[2];
    strings[0] = dataJson;
    strings[1] = companyCode;
    return strings;
  }

  /**
   * 商品分类
   */
  private void operatePxProductCategory(DataSync dataSync) {
    int opt = dataSync.getOpt();
    String dataJson = getDataJson(dataSync);
    PxProductCategory category =
        RestClient.getGsonWithBuilder().fromJson(dataJson, PxProductCategory.class);
    switch (opt) {
      case DataSync.INSERT:
        PxProductCategory unique = DaoServiceUtil.getProductCategoryService()
            .queryBuilder()
            .where(PxProductCategoryDao.Properties.ObjectId.eq(category.getObjectId()))
            .unique();
        if (unique == null) {
          category.setDelFlag("0");
          DaoServiceUtil.getProductCategoryService().saveOrUpdate(category);
        }
        break;
      case DataSync.UPDATE:
        PxProductCategory exist = DaoServiceUtil.getProductCategoryService()
            .queryBuilder()
            .where(PxProductCategoryDao.Properties.ObjectId.eq(category.getObjectId()))
            .unique();
        if (exist != null) {
          exist.setType(category.getType());
          exist.setName(category.getName());
          exist.setCode(category.getCode());
          exist.setOrderNo(category.getOrderNo());
          exist.setVersion(category.getVersion());
          exist.setLeaf(category.getLeaf());
          exist.setShelf(category.getShelf());
          exist.setVisible(category.getVisible());
          exist.setDelFlag("0");
          DaoServiceUtil.getProductCategoryService().update(exist);
        } else {
          category.setDelFlag("0");
          DaoServiceUtil.getProductCategoryService().saveOrUpdate(category);
        }
        break;
      case DataSync.DELETE:
        PxProductCategory del = DaoServiceUtil.getProductCategoryService()
            .queryBuilder()
            .where(PxProductCategoryDao.Properties.ObjectId.eq(category.getObjectId()))
            .unique();
        if (del != null) {
          del.setDelFlag("1");
          DaoServiceUtil.getProductCategoryService().update(del);
        }
        break;
    }
  }

  /**
   * 商品信息
   */
  private void operatePxProductInfo(DataSync dataSync) {
    int opt = dataSync.getOpt();
    String dataJson = getDataJson(dataSync);
    PxProductInfo productInfo =
        RestClient.getGsonWithBuilder().fromJson(dataJson, PxProductInfo.class);
    productInfo.setIsCustom(false);
    switch (opt) {
      case DataSync.INSERT:
        PxProductInfo unique = DaoServiceUtil.getProductInfoService()
            .queryBuilder()
            .where(PxProductInfoDao.Properties.ObjectId.eq(productInfo.getObjectId()))
            .unique();
        if (unique == null) {
          productInfo.setDelFlag("0");
          mProductInfoList.add(productInfo);
          DaoServiceUtil.getProductInfoService().save(productInfo);
        }
        break;
      case DataSync.UPDATE:
        PxProductInfo exist = DaoServiceUtil.getProductInfoService()
            .queryBuilder()
            .where(PxProductInfoDao.Properties.ObjectId.eq(productInfo.getObjectId()))
            .unique();
        if (exist != null) {
          exist.setType(productInfo.getType());
          exist.setName(productInfo.getName());
          exist.setPy(productInfo.getPy());
          exist.setCode(productInfo.getCode());
          exist.setPrice(productInfo.getPrice());
          exist.setVipPrice(productInfo.getVipPrice());
          exist.setUnit(productInfo.getUnit());
          exist.setMultipleUnit(productInfo.getMultipleUnit());
          exist.setOrderUnit(productInfo.getOrderUnit());
          exist.setIsDiscount(productInfo.getIsDiscount());
          exist.setIsGift(productInfo.getIsGift());
          exist.setChangePrice(productInfo.getChangePrice());
          exist.setStatus(productInfo.getStatus());
          exist.setShelf(productInfo.getShelf());
          exist.setVisible(productInfo.getVisible());
          exist.setDelFlag("0");
          DaoServiceUtil.getProductInfoService().update(exist);
          //设置分类,添加到list，更新关联
          exist.setCategory(productInfo.getCategory());
          mProductInfoList.add(exist);
        } else {
          productInfo.setDelFlag("0");
          mProductInfoList.add(productInfo);
          DaoServiceUtil.getProductInfoService().save(productInfo);
        }
        break;
      case DataSync.DELETE:
        PxProductInfo del = DaoServiceUtil.getProductInfoService()
            .queryBuilder()
            .where(PxProductInfoDao.Properties.ObjectId.eq(productInfo.getObjectId()))
            .unique();
        if (del != null) {
          del.setDelFlag("1");
          DaoServiceUtil.getProductInfoService().update(del);
        }
        break;
    }
  }

  /**
   * 桌台
   */
  private void operatePxTableInfo(DataSync dataSync) {
    int opt = dataSync.getOpt();
    String dataJson = getDataJson(dataSync);
    PxTableInfo tableInfo = RestClient.getGsonWithBuilder().fromJson(dataJson, PxTableInfo.class);
    switch (opt) {
      case DataSync.INSERT:
        PxTableInfo unique = DaoServiceUtil.getTableInfoService()
            .queryBuilder()
            .where(PxTableInfoDao.Properties.ObjectId.eq(tableInfo.getObjectId()))
            .unique();
        if (unique == null) {
          tableInfo.setDelFlag("0");
          DaoServiceUtil.getTableInfoService().saveOrUpdate(tableInfo);
        }
        break;
      case DataSync.UPDATE:
        PxTableInfo exist = DaoServiceUtil.getTableInfoService()
            .queryBuilder()
            .where(PxTableInfoDao.Properties.ObjectId.eq(tableInfo.getObjectId()))
            .unique();
        if (exist != null) {
          exist.setCode(tableInfo.getCode());
          exist.setName(tableInfo.getName());
          exist.setType(tableInfo.getType());
          exist.setPeopleNum(tableInfo.getPeopleNum());
          exist.setSortNo(tableInfo.getSortNo());
          exist.setDelFlag("0");
          DaoServiceUtil.getTableInfoService().update(exist);
        } else {
          tableInfo.setDelFlag("0");
          DaoServiceUtil.getTableInfoService().saveOrUpdate(tableInfo);
        }
        break;
      case DataSync.DELETE:
        PxTableInfo del = DaoServiceUtil.getTableInfoService()
            .queryBuilder()
            .where(PxTableInfoDao.Properties.ObjectId.eq(tableInfo.getObjectId()))
            .unique();
        if (del != null) {
          del.setDelFlag("1");
          DaoServiceUtil.getTableInfoService().update(del);
        }
        break;
    }
  }

  /**
   * 用户
   */
  private void operateUser(DataSync dataSync) {
    int opt = dataSync.getOpt();
    String[] dataJsonByUser = getDataJsonByUser(dataSync);
    User user = RestClient.getGsonWithBuilder().fromJson(dataJsonByUser[0], User.class);
    user.setCompanyCode(dataJsonByUser[1]);
    switch (opt) {
      case DataSync.INSERT:
        User unique = DaoServiceUtil.getUserService()
            .queryBuilder()
            .where(UserDao.Properties.ObjectId.eq(user.getObjectId()))
            .unique();
        if (unique == null) {
          user.setDelFlag("0");
          DaoServiceUtil.getUserService().saveOrUpdate(user);
        }
        break;
      case DataSync.UPDATE:
        User exist = DaoServiceUtil.getUserService()
            .queryBuilder()
            .where(UserDao.Properties.ObjectId.eq(user.getObjectId()))
            .unique();
        if (exist != null) {
          exist.setLoginName(user.getLoginName());
          exist.setPassword(user.getPassword());
          exist.setNo(user.getNo());
          exist.setName(user.getName());
          exist.setEmail(user.getEmail());
          exist.setPhone(user.getPhone());
          exist.setMobile(user.getMobile());
          exist.setUserType(user.getUserType());
          exist.setLoginIp(user.getLoginIp());
          exist.setLoginDate(user.getLoginDate());
          exist.setLoginFlag(user.getLoginFlag());
          exist.setPhoto(user.getPhoto());
          exist.setOldLoginName(user.getOldLoginName());
          exist.setNewPassword(user.getNewPassword());
          exist.setOldLoginIp(user.getOldLoginIp());
          exist.setOldLoginDate(user.getOldLoginDate());
          exist.setCompanyCode(user.getCompanyCode());
          exist.setMaxTail(user.getMaxTail());
          exist.setInitPassword(user.getInitPassword());
          exist.setImUserName(user.getImUserName());
          exist.setDelFlag("0");
          exist.setCanRetreat(user.getCanRetreat());
          DaoServiceUtil.getUserService().update(exist);
          //如果SP里的用户修改了密码，重置
          if (SPUtils.get(App.getContext(), Setting.SP_USERNAME, "").equals(user.getLoginName())) {
            if (!SPUtils.get(App.getContext(), Setting.SP_PWD, "").equals(user.getPassword())) {
              SPUtils.put(App.getContext(), Setting.REMEMBER_PWD, false);
              SPUtils.put(App.getContext(), Setting.SP_USERNAME, "");
              SPUtils.put(App.getContext(), Setting.SP_PWD, "");
            }
          }
        } else {
          user.setDelFlag("0");
          DaoServiceUtil.getUserService().saveOrUpdate(user);
        }
        break;
      case DataSync.DELETE:
        User del = DaoServiceUtil.getUserService()
            .queryBuilder()
            .where(UserDao.Properties.ObjectId.eq(user.getObjectId()))
            .unique();
        if (del != null) {
          del.setDelFlag("1");
          DaoServiceUtil.getUserService().update(del);
        }
        break;
    }
  }

  /**
   * 公司
   */
  private void operateOffice(DataSync dataSync) {
    int opt = dataSync.getOpt();
    String dataJson = getDataJson(dataSync);
    Office office = RestClient.getGsonWithBuilder().fromJson(dataJson, Office.class);
    switch (opt) {
      case DataSync.INSERT:
        Office unique = DaoServiceUtil.getOfficeService()
            .queryBuilder()
            .where(OfficeDao.Properties.ObjectId.eq(office.getObjectId()))
            .unique();
        if (unique == null) {
          DaoServiceUtil.getOfficeService().saveOrUpdate(office);
        }
        break;
      case DataSync.UPDATE:
        Office exist = DaoServiceUtil.getOfficeService()
            .queryBuilder()
            .where(OfficeDao.Properties.ObjectId.eq(office.getObjectId()))
            .unique();
        if (exist != null) {
          exist.setCode(office.getCode());
          exist.setType(office.getType());
          exist.setGrade(office.getGrade());
          exist.setAddress(office.getAddress());
          exist.setZipCode(office.getZipCode());
          exist.setMaster(office.getMaster());
          exist.setPhone(office.getPhone());
          exist.setFax(office.getFax());
          exist.setEmail(office.getEmail());
          exist.setUseable(office.getUseable());
          exist.setLogo(office.getLogo());
          DaoServiceUtil.getOfficeService().update(exist);
        }
        break;
      case DataSync.DELETE:

        break;
    }
  }

  /**
   * 规格信息
   */
  private void operateFormatInfo(DataSync dataSync) {
    int opt = dataSync.getOpt();
    String dataJson = getDataJson(dataSync);
    PxFormatInfo formatInfo =
        RestClient.getGsonWithBuilder().fromJson(dataJson, PxFormatInfo.class);
    switch (opt) {
      case DataSync.INSERT:
        PxFormatInfo unique = DaoServiceUtil.getFormatInfoService()
            .queryBuilder()
            .where(PxFormatInfoDao.Properties.ObjectId.eq(formatInfo.getObjectId()))
            .unique();
        if (unique == null) {
          formatInfo.setDelFlag("0");
          DaoServiceUtil.getFormatInfoService().save(formatInfo);
        }
        break;
      case DataSync.UPDATE:
        PxFormatInfo exist = DaoServiceUtil.getFormatInfoService()
            .queryBuilder()
            .where(PxFormatInfoDao.Properties.ObjectId.eq(formatInfo.getObjectId()))
            .unique();
        if (exist != null) {
          exist.setName(formatInfo.getName());
          exist.setDelFlag("0");
          DaoServiceUtil.getFormatInfoService().update(exist);
        } else {
          formatInfo.setDelFlag("0");
          DaoServiceUtil.getFormatInfoService().save(formatInfo);
        }
        break;
      case DataSync.DELETE:
        PxFormatInfo existDel = DaoServiceUtil.getFormatInfoService()
            .queryBuilder()
            .where(PxFormatInfoDao.Properties.ObjectId.eq(formatInfo.getObjectId()))
            .unique();
        //假删除
        if (existDel != null) {
          existDel.setDelFlag("1");
          DaoServiceUtil.getFormatInfoService().update(existDel);
        }
        break;
    }
  }

  /**
   * 商品规格引用关系
   */
  //@formatter:on
  private void operateProductFormatRel(DataSync dataSync) {
    int opt = dataSync.getOpt();
    String dataJson = getDataJson(dataSync);
    PxProductFormatRel pxProductFormatRel =
        RestClient.getGsonWithBuilder().fromJson(dataJson, PxProductFormatRel.class);
    switch (opt) {
      case DataSync.INSERT:
        PxProductFormatRel unique = DaoServiceUtil.getProductFormatRelServiceService()
            .queryBuilder()
            .where(PxProductFormatRelDao.Properties.ObjectId.eq(pxProductFormatRel.getObjectId()))
            .unique();
        if (unique == null) {
          pxProductFormatRel.setDelFlag("0");
          DaoServiceUtil.getProductFormatRelServiceService().save(pxProductFormatRel);
          mProdFormatRelList.add(pxProductFormatRel);
        }
        break;
      case DataSync.UPDATE:
        PxProductFormatRel exist = DaoServiceUtil.getProductFormatRelServiceService()
            .queryBuilder()
            .where(PxProductFormatRelDao.Properties.ObjectId.eq(pxProductFormatRel.getObjectId()))
            .unique();
        if (exist != null) {

        } else {
          pxProductFormatRel.setDelFlag("0");
          DaoServiceUtil.getProductFormatRelServiceService().save(pxProductFormatRel);
          mProdFormatRelList.add(pxProductFormatRel);
        }
        break;
      case DataSync.DELETE:
        PxProductFormatRel del = DaoServiceUtil.getProductFormatRelServiceService()
            .queryBuilder()
            .where(PxProductFormatRelDao.Properties.ObjectId.eq(pxProductFormatRel.getObjectId()))
            .unique();
        //假删除
        if (del != null) {
          del.setDelFlag("1");
          DaoServiceUtil.getProductFormatRelServiceService().update(del);
        }
        break;
    }
  }

  /**
   * 做法信息
   */
  private void operateMethodInfo(DataSync dataSync) {
    int opt = dataSync.getOpt();
    String dataJson = getDataJson(dataSync);
    PxMethodInfo methodInfo =
        RestClient.getGsonWithBuilder().fromJson(dataJson, PxMethodInfo.class);
    switch (opt) {
      case DataSync.INSERT:
        PxMethodInfo unique = DaoServiceUtil.getMethodInfoService()
            .queryBuilder()
            .where(PxMethodInfoDao.Properties.ObjectId.eq(methodInfo.getObjectId()))
            .unique();
        if (unique == null) {
          methodInfo.setDelFlag("0");
          DaoServiceUtil.getMethodInfoService().save(methodInfo);
        }
        break;
      case DataSync.UPDATE:
        PxMethodInfo exist = DaoServiceUtil.getMethodInfoService()
            .queryBuilder()
            .where(PxMethodInfoDao.Properties.ObjectId.eq(methodInfo.getObjectId()))
            .unique();
        if (exist != null) {
          exist.setName(methodInfo.getName());
          DaoServiceUtil.getMethodInfoService().update(exist);
        } else {
          methodInfo.setDelFlag("0");
          DaoServiceUtil.getMethodInfoService().save(methodInfo);
        }
        break;
      case DataSync.DELETE:
        PxMethodInfo existDel = DaoServiceUtil.getMethodInfoService()
            .queryBuilder()
            .where(PxMethodInfoDao.Properties.ObjectId.eq(methodInfo.getObjectId()))
            .unique();
        //假删除
        if (existDel != null) {
          existDel.setDelFlag("1");
          DaoServiceUtil.getMethodInfoService().update(existDel);
        }
        break;
    }
  }

  /**
   * 商品和做法引用关系
   */
  private void operateProductMethodRel(DataSync dataSync) {
    int opt = dataSync.getOpt();
    String dataJson = getDataJson(dataSync);
    PxProductMethodRef pxProductMethodRef =
        RestClient.getGsonWithBuilder().fromJson(dataJson, PxProductMethodRef.class);
    switch (opt) {
      case DataSync.INSERT:
        PxProductMethodRef unique = DaoServiceUtil.getProductMethodRelService()
            .queryBuilder()
            .where(PxProductMethodRefDao.Properties.ObjectId.eq(pxProductMethodRef.getObjectId()))
            .unique();
        if (unique == null) {
          pxProductMethodRef.setDelFlag("0");
          DaoServiceUtil.getProductMethodRelService().save(pxProductMethodRef);
          mProdMethodRelList.add(pxProductMethodRef);
        }
        break;
      case DataSync.UPDATE:
        PxProductMethodRef exist = DaoServiceUtil.getProductMethodRelService()
            .queryBuilder()
            .where(PxProductMethodRefDao.Properties.ObjectId.eq(pxProductMethodRef.getObjectId()))
            .unique();
        if (exist != null) {

        } else {
          pxProductMethodRef.setDelFlag("0");
          DaoServiceUtil.getProductMethodRelService().save(pxProductMethodRef);
          mProdMethodRelList.add(pxProductMethodRef);
        }
        break;
      case DataSync.DELETE:
        PxProductMethodRef del = DaoServiceUtil.getProductMethodRelService()
            .queryBuilder()
            .where(PxProductMethodRefDao.Properties.ObjectId.eq(pxProductMethodRef.getObjectId()))
            .unique();
        //假删除
        if (del != null) {
          del.setDelFlag("1");
          DaoServiceUtil.getProductMethodRelService().update(del);
        }
        break;
    }
  }

  /**
   * 操作原因
   */
  private void operateOptReason(DataSync dataSync) {
    int opt = dataSync.getOpt();
    String dataJson = getDataJson(dataSync);
    PxOptReason optReason = RestClient.getGsonWithBuilder().fromJson(dataJson, PxOptReason.class);
    switch (opt) {
      case DataSync.INSERT:
        PxOptReason reason = DaoServiceUtil.getOptReasonService()
            .queryBuilder()
            .where(PxOptReasonDao.Properties.ObjectId.eq(optReason.getObjectId()))
            .unique();
        if (reason == null) {
          optReason.setDelFlag("0");
          DaoServiceUtil.getOptReasonService().saveOrUpdate(optReason);
        }
        break;
      case DataSync.UPDATE:
        PxOptReason exist = DaoServiceUtil.getOptReasonService()
            .queryBuilder()
            .where(PxOptReasonDao.Properties.ObjectId.eq(optReason.getObjectId()))
            .unique();
        if (exist != null) {
          exist.setName(optReason.getName());
          exist.setType(optReason.getType());
          DaoServiceUtil.getOptReasonService().saveOrUpdate(exist);
        } else {
          optReason.setDelFlag("0");
          DaoServiceUtil.getOptReasonService().saveOrUpdate(optReason);
        }
        break;
      case DataSync.DELETE:
        PxOptReason del = DaoServiceUtil.getOptReasonService()
            .queryBuilder()
            .where(PxOptReasonDao.Properties.ObjectId.eq(optReason.getObjectId()))
            .unique();
        if (del != null) {
          del.setDelFlag("1");
          DaoServiceUtil.getOptReasonService().saveOrUpdate(del);
        }
        break;
    }
  }

  /**
   * 商品备注
   */
  private void operateProdRemarks(DataSync dataSync) {
    int opt = dataSync.getOpt();
    String dataJson = getDataJson(dataSync);
    PxProductRemarks remarks =
        RestClient.getGsonWithBuilder().fromJson(dataJson, PxProductRemarks.class);
    PxProductRemarks productRemarks = DaoServiceUtil.getProdRemarksService()
        .queryBuilder()
        .where(PxProductRemarksDao.Properties.ObjectId.eq(remarks.getObjectId()))
        .unique();
    switch (opt) {
      case DataSync.INSERT:
        if (productRemarks == null) {
          remarks.setDelFlag("0");
          DaoServiceUtil.getProdRemarksService().saveOrUpdate(remarks);
        }
        break;
      case DataSync.UPDATE:
        if (productRemarks == null) {
          remarks.setDelFlag("0");
          DaoServiceUtil.getProdRemarksService().saveOrUpdate(remarks);
        } else {
          productRemarks.setRemarks(remarks.getRemarks());
          DaoServiceUtil.getProdRemarksService().saveOrUpdate(productRemarks);
        }
        break;
      case DataSync.DELETE:
        if (productRemarks != null) {
          productRemarks.setDelFlag("1");
          DaoServiceUtil.getProdRemarksService().saveOrUpdate(productRemarks);
        }
        break;
    }
  }
  /**
   * 桌台区域
   */
  private void operateTableArea(DataSync dataSync) {
    int opt = dataSync.getOpt();
    String dataJson = getDataJson(dataSync);
    PxTableArea tableArea = RestClient.getGsonWithBuilder().fromJson(dataJson, PxTableArea.class);
    PxTableArea dbTableArea = DaoServiceUtil.getTableAreaService()
        .queryBuilder()
        .where(PxTableAreaDao.Properties.ObjectId.eq(tableArea.getObjectId()))
        .unique();
    switch (opt) {
      case DataSync.INSERT:
        if (dbTableArea == null){
          tableArea.setDelFlag("0");
          DaoServiceUtil.getTableAreaService().saveOrUpdate(tableArea);
        }
        break;
      case DataSync.UPDATE:
        if (dbTableArea != null){
          dbTableArea.setDelFlag("0");
          dbTableArea.setType(tableArea.getType());
          dbTableArea.setName(tableArea.getName());
          DaoServiceUtil.getTableAreaService().update(dbTableArea);
        }else{
          tableArea.setDelFlag("0");
          DaoServiceUtil.getTableAreaService().saveOrUpdate(tableArea);
        }

        break;
      case DataSync.DELETE:
        if (dbTableArea != null){
          dbTableArea.setDelFlag("1");
          DaoServiceUtil.getTableAreaService().update(dbTableArea);
        }
        break;
    }
  }
  /**
   * 促销信息
   */
  private void operatePromotionInfo(DataSync dataSync) {
    int opt = dataSync.getOpt();
    String dataJson = getDataJson(dataSync);
    Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation()
        .setDateFormat("yyyy-MM-dd")
        .create();
    PxPromotioInfo pxPromotioInfo = gson.fromJson(dataJson, PxPromotioInfo.class);
    switch (opt) {
      case DataSync.INSERT:
        PxPromotioInfo promotio = DaoServiceUtil.getPromotioService()
            .queryBuilder()
            .where(PxPromotioInfoDao.Properties.ObjectId.eq(pxPromotioInfo.getObjectId()))
            .unique();
        if (promotio == null) {
          pxPromotioInfo.setDelFlag("0");
          DaoServiceUtil.getPromotioService().saveOrUpdate(pxPromotioInfo);
        }
        break;
      case DataSync.UPDATE:
        PxPromotioInfo exist = DaoServiceUtil.getPromotioService()
            .queryBuilder()
            .where(PxPromotioInfoDao.Properties.ObjectId.eq(pxPromotioInfo.getObjectId()))
            .unique();
        if (exist != null) {
          exist.setDelFlag("0");
          exist.setName(pxPromotioInfo.getName());
          exist.setCode(pxPromotioInfo.getCode());
          exist.setEndTime(pxPromotioInfo.getEndTime());
          exist.setStartTime(pxPromotioInfo.getStartTime());
          exist.setType(pxPromotioInfo.getType());
          exist.setWeekly(pxPromotioInfo.getWeekly());
          exist.setStartDate(pxPromotioInfo.getStartDate());
          exist.setEndDate(pxPromotioInfo.getEndDate());
          DaoServiceUtil.getPromotioService().update(exist);
        } else {
          pxPromotioInfo.setDelFlag("0");
          DaoServiceUtil.getPromotioService().saveOrUpdate(pxPromotioInfo);
        }
        break;
      case DataSync.DELETE:
        PxPromotioInfo del = DaoServiceUtil.getPromotioService()
            .queryBuilder()
            .where(PxPromotioInfoDao.Properties.ObjectId.eq(pxPromotioInfo.getObjectId()))
            .unique();
        if (del != null) {
          del.setDelFlag("1");
          DaoServiceUtil.getPromotioService().update(del);
        }
        break;
    }
  }

  /**
   * 促销详情
   */
  private void operatePromotionDetails(DataSync dataSync) {
    int opt = dataSync.getOpt();
    String dataJson = getDataJson(dataSync);
    PxPromotioDetails pxPromotioDetails =
        RestClient.getGsonWithBuilder().fromJson(dataJson, PxPromotioDetails.class);
    switch (opt) {
      case DataSync.INSERT:
        PxPromotioDetails insert = DaoServiceUtil.getPromotioDetailsService()
            .queryBuilder()
            .where(PxPromotioDetailsDao.Properties.ObjectId.eq(pxPromotioDetails.getObjectId()))
            .unique();
        if (insert == null) {
          pxPromotioDetails.setDelFlag("0");
          sPromDetailsList.add(pxPromotioDetails);
          DaoServiceUtil.getPromotioDetailsService().saveOrUpdate(pxPromotioDetails);
        }
        break;
      case DataSync.UPDATE:
        PxPromotioDetails exist = DaoServiceUtil.getPromotioDetailsService()
            .queryBuilder()
            .where(PxPromotioDetailsDao.Properties.ObjectId.eq(pxPromotioDetails.getObjectId()))
            .unique();
        if (exist != null) {
          exist.setDelFlag("0");
          exist.setPromotionalPrice(pxPromotioDetails.getPromotionalPrice());
          DaoServiceUtil.getPromotioDetailsService().saveOrUpdate(exist);
          //
          exist.setPromotio(pxPromotioDetails.getPromotio());
          exist.setProduct(pxPromotioDetails.getProduct());
          exist.setFormat(pxPromotioDetails.getFormat());
          sPromDetailsList.add(exist);
        } else {
          pxPromotioDetails.setDelFlag("0");
          sPromDetailsList.add(pxPromotioDetails);
          DaoServiceUtil.getPromotioDetailsService().saveOrUpdate(pxPromotioDetails);
        }
        break;
      case DataSync.DELETE:
        PxPromotioDetails del = DaoServiceUtil.getPromotioDetailsService()
            .queryBuilder()
            .where(PxPromotioDetailsDao.Properties.ObjectId.eq(pxPromotioDetails.getObjectId()))
            .unique();
        if (del != null) {
          del.setDelFlag("1");
          DaoServiceUtil.getPromotioDetailsService().saveOrUpdate(del);
        }
        break;
    }
  }
}