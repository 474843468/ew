package com.think.firewaiter.dao;

/**
 * Created by zjq on 2016/3/31.
 * 用于获取各种DbService
 */
public class DaoServiceUtil {

  //桌台
  private static TableInfoService sTableInfoService;
  //分类
  private static ProductCategoryService sProductCategoryService;
  //商品
  private static ProductInfoService sProductInfoService;
  //用户
  private static UserService sUserService;
  //公司
  private static OfficeService sOfficeService;
  //规格
  private static FormatInfoService sFormatInfoService;
  //做法
  private static MethodInfoService sMethodInfoService;
  //商品规格关联
  private static ProductFormatRelService sProductFormatRelServiceService;
  //商品做法关联
  private static ProductMethodRelService sProductMethodRelService;
  //购物车
  private static ShoppingCartService sShoppingCartService;
  //服务桌台
  private static UserTableRelService sUserTableRelService;
  //操作原因
  private static OptReasonService sOptReasonService;
  //商品备注
  private static ProdRemarksService sProdRemarksService;
  private static PxTableAreaService sPxTableAreaService;
  private static PxPromotioInfoService sPxPromotioInfoService;
  private static PxPromotioDetailsService sPromotionDetailsService;

  /**
   * TableInfo
   */
  public static PxTableInfoDao getTableInfoDao() {
    return DbCore.getDaoSession().getPxTableInfoDao();
  }

  public static TableInfoService getTableInfoService() {
    if (sTableInfoService == null) {
      sTableInfoService = new TableInfoService(getTableInfoDao());
    }
    return sTableInfoService;
  }

  /**
   * ProductCategory
   */
  public static PxProductCategoryDao getProductCategoryDao() {
    return DbCore.getDaoSession().getPxProductCategoryDao();
  }

  public static ProductCategoryService getProductCategoryService() {
    if (sProductCategoryService == null) {
      sProductCategoryService = new ProductCategoryService(getProductCategoryDao());
    }
    return sProductCategoryService;
  }

  /**
   * ProductInfo
   */
  public static PxProductInfoDao getProductInfoDao() {
    return DbCore.getDaoSession().getPxProductInfoDao();
  }

  public static ProductInfoService getProductInfoService() {
    if (sProductInfoService == null) {
      sProductInfoService = new ProductInfoService(getProductInfoDao());
    }
    return sProductInfoService;
  }

  /**
   * User
   */
  public static UserDao getUserDao() {
    return DbCore.getDaoSession().getUserDao();
  }

  public static UserService getUserService() {
    if (sUserService == null) {
      sUserService = new UserService(getUserDao());
    }
    return sUserService;
  }

  /**
   * Office
   */
  public static OfficeDao getOfficeDao() {
    return DbCore.getDaoSession().getOfficeDao();
  }

  public static OfficeService getOfficeService() {
    if (sOfficeService == null) {
      sOfficeService = new OfficeService(getOfficeDao());
    }
    return sOfficeService;
  }

  /**
   * FormatInfo
   */
  public static PxFormatInfoDao getFormatInfoDao() {
    return DbCore.getDaoSession().getPxFormatInfoDao();
  }

  public static FormatInfoService getFormatInfoService() {
    if (sFormatInfoService == null) {
      sFormatInfoService = new FormatInfoService(getFormatInfoDao());
    }
    return sFormatInfoService;
  }

  /**
   * ProductFormatRel
   */
  public static PxProductFormatRelDao getProductFormatRelDao() {
    return DbCore.getDaoSession().getPxProductFormatRelDao();
  }

  public static ProductFormatRelService getProductFormatRelServiceService() {
    if (sProductFormatRelServiceService == null) {
      sProductFormatRelServiceService = new ProductFormatRelService(getProductFormatRelDao());
    }
    return sProductFormatRelServiceService;
  }

  /**
   * MethodIno
   */
  public static PxMethodInfoDao getMethodInfoDao() {
    return DbCore.getDaoSession().getPxMethodInfoDao();
  }

  public static MethodInfoService getMethodInfoService() {
    if (sMethodInfoService == null) {
      sMethodInfoService = new MethodInfoService(getMethodInfoDao());
    }
    return sMethodInfoService;
  }

  /**
   * ProductMethodRel
   */
  public static PxProductMethodRefDao getProductMethodRelDao() {
    return DbCore.getDaoSession().getPxProductMethodRefDao();
  }

  public static ProductMethodRelService getProductMethodRelService() {
    if (sProductMethodRelService == null) {
      sProductMethodRelService = new ProductMethodRelService(getProductMethodRelDao());
    }
    return sProductMethodRelService;
  }

  /**
   * ShoppingCart
   */
  public static ShoppingCartDao getShoppingCartDao() {
    return DbCore.getDaoSession().getShoppingCartDao();
  }

  public static ShoppingCartService getShoppingCartService() {
    if (sShoppingCartService == null) {
      sShoppingCartService = new ShoppingCartService(getShoppingCartDao());
    }
    return sShoppingCartService;
  }

  /**
   * ServeTable
   */
  public static UserTableRelDao getUserTableRelDao() {
    return DbCore.getDaoSession().getUserTableRelDao();
  }

  public static UserTableRelService getUserTableRelService() {
    if (sUserTableRelService == null) {
      sUserTableRelService = new UserTableRelService(getUserTableRelDao());
    }
    return sUserTableRelService;
  }

  /**
   * 操作原因
   */
  public static PxOptReasonDao getOptReasonDao() {
    return DbCore.getDaoSession().getPxOptReasonDao();
  }

  public static OptReasonService getOptReasonService() {
    if (sOptReasonService == null) {
      sOptReasonService = new OptReasonService(getOptReasonDao());
    }
    return sOptReasonService;
  }

  /**
   * ProdRemarks
   */
  public static PxProductRemarksDao getProductRemarksDao() {
    return DbCore.getDaoSession().getPxProductRemarksDao();
  }

  public static ProdRemarksService getProdRemarksService() {
    if (sProdRemarksService == null) {
      sProdRemarksService = new ProdRemarksService(getProductRemarksDao());
    }
    return sProdRemarksService;
  }

  /**
   * PxTableArea
   */
  public static PxTableAreaDao getTableAreaDao() {
    return DbCore.getDaoSession().getPxTableAreaDao();
  }

  public static PxTableAreaService getTableAreaService() {
    if (sPxTableAreaService == null) {
      sPxTableAreaService = new PxTableAreaService(getTableAreaDao());
    }
    return sPxTableAreaService;
  }

  /**
   * PxPromotioInfo
   */
  public static PxPromotioInfoDao getPromotioDao() {
    return DbCore.getDaoSession().getPxPromotioInfoDao();
  }

  public static PxPromotioInfoService getPromotioService() {
    if (sPxPromotioInfoService == null) {
      sPxPromotioInfoService = new PxPromotioInfoService(getPromotioDao());
    }
    return sPxPromotioInfoService;
  }

  /**
   * PxPromotioDetails
   */
  public static PxPromotioDetailsDao getPromotioDetailsDao() {
    return DbCore.getDaoSession().getPxPromotioDetailsDao();
  }

  public static PxPromotioDetailsService getPromotioDetailsService() {
    if (sPromotionDetailsService == null) {
      sPromotionDetailsService = new PxPromotioDetailsService(getPromotioDetailsDao());
    }
    return sPromotionDetailsService;
  }
}
