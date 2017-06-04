package com.example;

import de.greenrobot.daogenerator.DaoGenerator;
import de.greenrobot.daogenerator.Entity;
import de.greenrobot.daogenerator.Property;
import de.greenrobot.daogenerator.Schema;
import de.greenrobot.daogenerator.ToMany;

public class ExampleDaoGenerator {
  //公司
  private static Entity office;
  //用户
  private static Entity user;
  //商品信息
  private static Entity pxProductInfo;
  //商品分类信息
  private static Entity pxProductCategory;
  //规格信息
  private static Entity pxFormatInfo;
  //做法信息
  private static Entity pxMethodInfo;
  //桌台信息
  private static Entity pxTableInfo;
  //购物车
  private static Entity shoppingCart;
  //商品规格引用关系
  private static Entity pxProductFormatRel;
  //商品做法引用关系
  private static Entity pxProductMethodRel;
  //服务生服务桌台
  private static Entity userTableRel;
  //操作原因
  private static Entity pxOptReason;
  //商品备注
  private static Entity pxProductRemarks;
  //桌台区域
  private static Entity pxTableArea;
  //促销计划
  private static Entity pxPromotioInfo;
  // 促销计划详情
  private static Entity pxPromotioDetails;

  //@formatter:on
  public static void main(String[] args) throws Exception {
    Schema schema = new Schema(6, "com.think.firewaiter.module");
    schema.setDefaultJavaPackageDao("com.think.firewaiter.dao");
    //公司
    addOffice(schema);
    //用户
    addUser(schema);
    //商品信息
    addPxProductInfo(schema);
    //商品分类信息
    addPxProductCategory(schema);
    //规格信息
    addPxFormatInfo(schema);
    //做法信息
    addPxMethodInfo(schema);
    //桌台
    addPxTableInfo(schema);
    //商品规格引用关系
    addPxProductFormatRel(schema);
    //商品做法引用关系
    addPxProductMethodRel(schema);
    //购物车
    addShoppingCart(schema);
    //服务桌台
    addUserTableRel(schema);
    //操作原因
    addPxOptReason(schema);
    //商品备注
    addPxProductRemarks(schema);
    //桌台区域
    addTableArea(schema);
    //促销计划
    addPxPromotioInfo(schema);
    //促销计划详情
    addPxPromotioDetails(schema);
    //连接促销计划和详情
    linkPromInfoAndPromDetails(schema);
    new DaoGenerator().generateAll(schema, "D:/EasyWaiter/app/src/main/java");
  }

  /**
   * 公司
   */
  //@formatter:off
  private static void addOffice(Schema schema) {
    office = schema.addEntity("Office");
    office.setTableName("Office");
    office.setHasKeepSections(true);
    office.implementsSerializable();
    office.addIdProperty().primaryKeyAsc();

    office.addStringProperty("objectId").javaDocField("对应服务器id").codeBeforeField("@SerializedName(\"id\") @Expose");
    office.addStringProperty("code").javaDocField("机构编码").codeBeforeField("@Expose");
    office.addStringProperty("type").javaDocField("机构类型（1：公司；2：部门；3：小组）").codeBeforeField("@Expose");
    office.addStringProperty("grade").javaDocField("机构等级（1：一级；2：二级；3：三级；4：四级）").codeBeforeField("@Expose");
    office.addStringProperty("address").javaDocField("联系地址").codeBeforeField("@Expose");
    office.addStringProperty("zipCode").javaDocField("邮政编码").codeBeforeField("@Expose");
    office.addStringProperty("master").javaDocField("负责人").codeBeforeField("@Expose");
    office.addStringProperty("phone").javaDocField("电话").codeBeforeField("@Expose");
    office.addStringProperty("fax").javaDocField("传真").codeBeforeField("@Expose");
    office.addStringProperty("email").javaDocField("邮箱").codeBeforeField("@Expose");
    office.addStringProperty("useable").javaDocField("是否可用 (0:可用 1:不可用) ").codeBeforeField("@Expose");
    office.addStringProperty("logo").javaDocField("店铺logo").codeBeforeField("@Expose");
    office.addStringProperty("groupId").javaDocField("群组id").codeBeforeField("@Expose");
    office.addStringProperty("name").javaDocField("店铺名字").codeBeforeField("@Expose");
    office.addStringProperty("easeAdmin").javaDocField("环信收银端用户名").codeBeforeField("@Expose");
  }

  /**
   * 用户
   */
  //@formatter:off
  private static void addUser(Schema schema) {
    user = schema.addEntity("User");
    user.setTableName("User");
    user.setHasKeepSections(true);
    user.implementsSerializable();
    user.addIdProperty().primaryKeyAsc();

    user.addStringProperty("objectId").javaDocField("").codeBeforeField("@SerializedName(\"id\") @Expose");
    user.addStringProperty("loginName").javaDocField("登录名").codeBeforeField("@Expose");
    user.addStringProperty("password").javaDocField("密码").codeBeforeField("@Expose");
    user.addStringProperty("no").javaDocField("工号").codeBeforeField("@Expose");
    user.addStringProperty("name").javaDocField("姓名").codeBeforeField("@Expose");
    user.addStringProperty("email").javaDocField("邮箱").codeBeforeField("@Expose");
    user.addStringProperty("phone").javaDocField("电话").codeBeforeField("@Expose");
    user.addStringProperty("mobile").javaDocField("手机").codeBeforeField("@Expose");
    user.addStringProperty("userType").javaDocField("用户类型").codeBeforeField("@Expose");
    user.addStringProperty("loginIp").javaDocField("最后登陆IP").codeBeforeField("@Expose");
    user.addDateProperty("loginDate").javaDocField("最后登陆日期").codeBeforeField("@Expose");
    user.addStringProperty("loginFlag").javaDocField("是否允许登陆").codeBeforeField("@Expose");
    user.addStringProperty("photo").javaDocField("头像").codeBeforeField("@Expose");
    user.addStringProperty("oldLoginName").javaDocField("原登录名").codeBeforeField("@Expose");
    user.addStringProperty("newPassword").javaDocField("新密码").codeBeforeField("@Expose");
    user.addStringProperty("oldLoginIp").javaDocField("上次登陆IP").codeBeforeField("@Expose");
    user.addDateProperty("oldLoginDate").javaDocField("上次登陆日期").codeBeforeField("@Expose");
    user.addStringProperty("delFlag").javaDocField("虚拟删除 0：正常 1：删除 2：审核").codeBeforeField("@Expose");
    user.addDoubleProperty("maxTail").javaDocField("最大抹零限制").codeBeforeField("@Expose");
    user.addStringProperty("companyCode").javaDocField("公司编码").codeBeforeField("@Expose");
    user.addStringProperty("imUserName").javaDocField("初始登录名").codeBeforeField("@Expose");
    user.addStringProperty("initPassword").javaDocField("初始化密码").codeBeforeField("@Expose");
    //1->2添加
    user.addStringProperty("canRetreat").javaDocField("是否允许退菜(1:允许 0:不允许)").codeBeforeField("@Expose");
  }

  /**
   * 商品信息
   */
  //@formatter:off
  private static void addPxProductInfo(Schema schema) {
    pxProductInfo = schema.addEntity("PxProductInfo");
    pxProductInfo.setTableName("ProductInfo");
    pxProductInfo.setHasKeepSections(true);
    pxProductInfo.implementsSerializable();
    pxProductInfo.addIdProperty().primaryKeyAsc();

    pxProductInfo.addStringProperty("objectId").javaDocField("对应服务器id").codeBeforeField("@SerializedName(\"id\") @Expose");
    pxProductInfo.addStringProperty("name").javaDocField("商品名称").codeBeforeField("@Expose");
    pxProductInfo.addStringProperty("py").javaDocField("中文拼音首字母缩写（由程序生成").codeBeforeField("@Expose");
    pxProductInfo.addStringProperty("code").javaDocField("商品编码").codeBeforeField("@Expose");
    pxProductInfo.addDoubleProperty("price").javaDocField("商品单价").codeBeforeField("@Expose");
    pxProductInfo.addDoubleProperty("vipPrice").javaDocField("会员特价（默认与商品价格一致）").codeBeforeField("@Expose");
    pxProductInfo.addStringProperty("unit").javaDocField("结账单位").codeBeforeField("@Expose");
    pxProductInfo.addStringProperty("multipleUnit").javaDocField("是否多单位菜（0：是 1：否").codeBeforeField("@Expose");
    pxProductInfo.addStringProperty("orderUnit").javaDocField("点菜单位").codeBeforeField("@Expose");
    pxProductInfo.addStringProperty("isDiscount").javaDocField("允许打折（0：允许 1：不允许）").codeBeforeField("@Expose");
    pxProductInfo.addStringProperty("isGift").javaDocField("是否为赠品(0：是  1 ：否)").codeBeforeField("@Expose");
    pxProductInfo.addStringProperty("isPrint").javaDocField("商品发送后厨是否出单(0:出单 1：不出单)").codeBeforeField("@Expose");
    pxProductInfo.addStringProperty("changePrice").javaDocField("是否允许收银改价（0：是 1：否）").codeBeforeField("@Expose");
    pxProductInfo.addStringProperty("status").javaDocField("商品状态 (0:正常 1：停售)").codeBeforeField("@Expose");
    pxProductInfo.addBooleanProperty("isCustom").javaDocField("是否为自定义商品").codeBeforeField("@Expose");
    pxProductInfo.addStringProperty("delFlag").javaDocField("虚拟删除 0：正常 1：删除 2：审核").codeBeforeField("@Expose");
    //2->3
    pxProductInfo.addDoubleProperty("overPlus").javaDocField("剩余数量").codeBeforeField("@Expose");
    //2->3
    pxProductInfo.addStringProperty("type").javaDocField("类型 0:普通分类 1：套餐分类").codeBeforeField("@Expose");
    //4->5
    pxProductInfo.addStringProperty("shelf").javaDocField("上架 (0:上架  1：下架)").codeBeforeField("@Expose");
    //4->5
    pxProductInfo.addStringProperty("visible").javaDocField("是否在微信点餐页面显示 0：显示 1：不显示").codeBeforeField("@Expose");
  }

  /**
   * 商品分类信息
   */
  //@formatter:off
  private static void addPxProductCategory(Schema schema) {
    pxProductCategory = schema.addEntity("PxProductCategory");
    pxProductCategory.setTableName("ProductCategory");
    pxProductCategory.setHasKeepSections(true);
    pxProductCategory.implementsSerializable();
    pxProductCategory.addIdProperty().primaryKeyAsc();

    pxProductCategory.addStringProperty("objectId").javaDocField("对应服务器id").codeBeforeField("@SerializedName(\"id\") @Expose");
    pxProductCategory.addIntProperty("orderNo").javaDocField("排序号").codeBeforeField("@Expose");
    pxProductCategory.addStringProperty("code").javaDocField("类型编码").codeBeforeField("@Expose");
    pxProductCategory.addIntProperty("version").javaDocField("数据版本").codeBeforeField("@Expose");
    pxProductCategory.addStringProperty("name").javaDocField("分类名称").codeBeforeField("@Expose");
    pxProductCategory.addStringProperty("delFlag").javaDocField("虚拟删除 0：正常 1：删除 2：审核").codeBeforeField("@Expose");
    pxProductCategory.addStringProperty("parentId").javaDocField("父节点id").codeBeforeField("@Expose");
    pxProductCategory.addStringProperty("leaf").javaDocField("是否为叶子节点 0：是 1：否").codeBeforeField("@Expose");
    //2->3
    pxProductCategory.addStringProperty("type").javaDocField("类型 0:普通分类 1：套餐分类").codeBeforeField("@Expose");
    //4->5
    pxProductCategory.addStringProperty("shelf").javaDocField("上架 (0:上架  1：下架)").codeBeforeField("@Expose");
    //4->5
    pxProductCategory.addStringProperty("visible").javaDocField("是否在微信点餐页面显示 0：显示 1：不显示").codeBeforeField("@Expose");

    Property pxProductCategoryId = pxProductInfo.addLongProperty("pxProductCategoryId").notNull().getProperty();
    pxProductInfo.addToOne(pxProductCategory, pxProductCategoryId, "dbCategory");

    ToMany prodCateToProdInfo = pxProductCategory.addToMany(pxProductInfo, pxProductCategoryId);
    prodCateToProdInfo.setName("dbProductInfoList");
  }

  /**
   * 规格信息
   */
  //@formatter:off
  private static void addPxFormatInfo(Schema schema) {
    pxFormatInfo = schema.addEntity("PxFormatInfo");
    pxFormatInfo.setJavaDoc("规格信息");
    pxFormatInfo.setTableName("FormatInfo");
    pxFormatInfo.setHasKeepSections(true);
    pxFormatInfo.implementsSerializable();
    pxFormatInfo.addIdProperty().primaryKeyAsc();

    pxFormatInfo.addStringProperty("objectId").javaDocField("对应服务器id").codeBeforeField("@SerializedName(\"id\") @Expose");
    pxFormatInfo.addStringProperty("name").javaDocField("规格名称").codeBeforeField("@Expose");
    pxFormatInfo.addStringProperty("delFlag").javaDocField("虚拟删除 0：正常 1：删除 2：审核").codeBeforeField("@Expose");
  }

  /**
   * 做法信息
   */
  //@formatter:off
  private static void addPxMethodInfo(Schema schema) {
    pxMethodInfo = schema.addEntity("PxMethodInfo");
    pxMethodInfo.setJavaDoc("做法信息");
    pxMethodInfo.setTableName("MethodInfo");
    pxMethodInfo.setHasKeepSections(true);
    pxMethodInfo.implementsSerializable();
    pxMethodInfo.addIdProperty().primaryKeyAsc();

    pxMethodInfo.addStringProperty("objectId").javaDocField("对应服务器id").codeBeforeField("@SerializedName(\"id\") @Expose");
    pxMethodInfo.addStringProperty("name").javaDocField("做法名称").codeBeforeField("@Expose");
    pxMethodInfo.addStringProperty("delFlag").javaDocField("虚拟删除 0：正常 1：删除 2：审核").codeBeforeField("@Expose");
  }

  /**
   * 桌台
   */
  //@formatter:off
  private static void addPxTableInfo(Schema schema) {
    pxTableInfo = schema.addEntity("PxTableInfo");
    pxTableInfo.setJavaDoc("桌台信息");
    pxTableInfo.setTableName("TableInfo");
    pxTableInfo.setHasKeepSections(true);
    pxTableInfo.implementsSerializable();
    pxTableInfo.addIdProperty().primaryKeyAsc();

    pxTableInfo.addStringProperty("objectId").javaDocField("对应服务器id").codeBeforeField("@SerializedName(\"id\") @Expose");
    pxTableInfo.addStringProperty("code").javaDocField("桌位编号)").codeBeforeField("@Expose");
    pxTableInfo.addStringProperty("name").javaDocField("桌位名称").codeBeforeField("@Expose");
    pxTableInfo.addStringProperty("type").javaDocField("桌位区域(0:大厅 1：包厢)").codeBeforeField("@Expose");
    pxTableInfo.addIntProperty("peopleNum").javaDocField("建议人数").codeBeforeField("@Expose");
    pxTableInfo.addStringProperty("status").javaDocField("桌位状态(0:空闲 1：占用)").codeBeforeField("@Expose");
    pxTableInfo.addStringProperty("delFlag").javaDocField("虚拟删除 0：正常 1：删除 2：审核").codeBeforeField("@Expose");
    pxTableInfo.addIntProperty("sortNo").javaDocField("排序号").codeBeforeField("@Expose");
  }

  /**
   * 商品规格引用信息
   */
  //@formatter:off
  private static void addPxProductFormatRel(Schema schema) {
    pxProductFormatRel = schema.addEntity("PxProductFormatRel");
    pxProductFormatRel.setTableName("ProductFormatRel");
    pxProductFormatRel.setHasKeepSections(true);
    pxProductFormatRel.implementsSerializable();
    pxProductFormatRel.addIdProperty().primaryKeyAsc();

    pxProductFormatRel.addStringProperty("objectId").javaDocField("对应服务器id").codeBeforeField("@SerializedName(\"id\") @Expose");
    pxProductFormatRel.addDoubleProperty("price").javaDocField("价格").codeBeforeField("@Expose");
    pxProductFormatRel.addDoubleProperty("vipPrice").javaDocField("会员价").codeBeforeField("@Expose");
    pxProductFormatRel.addStringProperty("delFlag").javaDocField("虚拟删除 0：正常 1：删除 2：审核").codeBeforeField("@Expose");
    //3->4
    pxProductFormatRel.addDoubleProperty("stock").javaDocField("库存余量").codeBeforeField("@Expose");
    //3->4
    pxProductFormatRel.addStringProperty("status").javaDocField("销售状态(0:正常  1:停售)").codeBeforeField("@Expose");

    Property pxFormatInfoId = pxProductFormatRel.addLongProperty("pxFormatInfoId").getProperty();
    pxProductFormatRel.addToOne(pxFormatInfo, pxFormatInfoId, "dbFormat");

    Property pxProductInfoId = pxProductFormatRel.addLongProperty("pxProductInfoId").getProperty();
    pxProductFormatRel.addToOne(pxProductInfo, pxProductInfoId, "dbProduct");
  }

  /**
   * 商品做法引用关系
   */
  private static void addPxProductMethodRel(Schema schema) {
    pxProductMethodRel = schema.addEntity("PxProductMethodRef");
    pxProductMethodRel.setTableName("PxProductMethodRef");
    pxProductMethodRel.setHasKeepSections(true);
    pxProductMethodRel.implementsSerializable();
    pxProductMethodRel.addIdProperty().primaryKeyAsc();

    pxProductMethodRel.addStringProperty("objectId").javaDocField("对应服务器id").codeBeforeField("@SerializedName(\"id\") @Expose");
    pxProductMethodRel.addStringProperty("delFlag").javaDocField("虚拟删除 0：正常 1：删除 2：审核").codeBeforeField("@Expose");

    Property pxMethodInfoId = pxProductMethodRel.addLongProperty("pxMethodInfoId").getProperty();
    pxProductMethodRel.addToOne(pxMethodInfo, pxMethodInfoId, "dbMethod");

    Property pxProductInfoId = pxProductMethodRel.addLongProperty("pxProductInfoId").getProperty();
    pxProductMethodRel.addToOne(pxProductInfo, pxProductInfoId, "dbProduct");
  }

  /**
   * 购物车
   */
  //@formatter:off
  private static void addShoppingCart(Schema schema) {
    shoppingCart = schema.addEntity("ShoppingCart");
    shoppingCart.setTableName("ShoppingCart");
    shoppingCart.setHasKeepSections(true);
    shoppingCart.implementsSerializable();
    shoppingCart.addIdProperty().primaryKeyAsc();

    shoppingCart.addDoubleProperty("num").javaDocField("数量").codeBeforeField("@Expose");
    shoppingCart.addDoubleProperty("multipleUnitNum").javaDocField("多单位数量").codeBeforeField("@Expose");
    shoppingCart.addBooleanProperty("isDelay").javaDocField("是否延迟").codeBeforeField("@Expose");
    //4->5
    shoppingCart.addStringProperty("remarks").javaDocField("备注").codeBeforeField("@Expose");

    //连接用户
    Property userId = shoppingCart.addLongProperty("userId").getProperty();
    shoppingCart.addToOne(user,userId,"dbUser");
    //连接桌台
    Property tableId = shoppingCart.addLongProperty("tableId").getProperty();
    shoppingCart.addToOne(pxTableInfo,tableId,"dbTable");
    //连接商品
    Property prodId = shoppingCart.addLongProperty("prodId").getProperty();
    shoppingCart.addToOne(pxProductInfo,prodId,"dbProd");
    //连接规格
    Property formatId = shoppingCart.addLongProperty("formatId").getProperty();
    shoppingCart.addToOne(pxFormatInfo,formatId,"dbFormat");
    //连接做法
    Property methodId = shoppingCart.addLongProperty("methodId").getProperty();
    shoppingCart.addToOne(pxMethodInfo,methodId,"dbMethod");
  }

  /**
   * 服务桌台
   * @param schema
   */
  private static void addUserTableRel(Schema schema) {
    userTableRel = schema.addEntity("UserTableRel");
    userTableRel.setTableName("UserTableRel");
    userTableRel.setHasKeepSections(true);
    userTableRel.implementsSerializable();
    userTableRel.addIdProperty().primaryKeyAsc();

    //连接用户
    Property userId = userTableRel.addLongProperty("userId").getProperty();
    userTableRel.addToOne(user,userId,"dbUser");
    //连接桌台
    Property tableId = userTableRel.addLongProperty("tableId").getProperty();
    userTableRel.addToOne(pxTableInfo,tableId,"dbTable");
  }


  /**
   * 操作原因
   */
  private static void addPxOptReason(Schema schema) {
    pxOptReason = schema.addEntity("PxOptReason");
    pxOptReason.setJavaDoc("操作原因");
    pxOptReason.setTableName("OptReason");
    pxOptReason.setHasKeepSections(true);
    pxOptReason.implementsSerializable();
    pxOptReason.addIdProperty().primaryKeyAsc();

    pxOptReason.addStringProperty("objectId").javaDocField("对应服务器id").codeBeforeField("@SerializedName(\"id\") @Expose");
    pxOptReason.addStringProperty("name").javaDocField("原因").codeBeforeField("@Expose");
    pxOptReason.addStringProperty("type").javaDocField("类型(0:打折原因 1：撤单原因 2：取消结账原因 3：退货原因)").codeBeforeField("@Expose");
    pxOptReason.addStringProperty("delFlag").javaDocField("虚拟删除 0：正常 1：删除 2：审核").codeBeforeField("@Expose");
  }

  /**
   * 商品备注
   */
  //4->5
  private static void addPxProductRemarks(Schema schema) {
    pxProductRemarks = schema.addEntity("PxProductRemarks");
    pxProductRemarks.setJavaDoc("商品备注");
    pxProductRemarks.setTableName("ProductRemarks");
    pxProductRemarks.setHasKeepSections(true);
    pxProductRemarks.implementsSerializable();
    pxProductRemarks.addIdProperty().primaryKeyAsc();

    pxProductRemarks.addStringProperty("objectId").javaDocField("对应服务器id").codeBeforeField("@SerializedName(\"id\") @Expose");
    pxProductRemarks.addStringProperty("delFlag").javaDocField("虚拟删除 0：正常 1：删除 2：审核").codeBeforeField("@Expose");
    pxProductRemarks.addStringProperty("remarks").javaDocField("备注").codeBeforeField("@Expose");
  }
  /**
   * 桌台区域
   */
  private static void addTableArea(Schema schema) {
    pxTableArea = schema.addEntity("PxTableArea");
    pxTableArea.setJavaDoc("桌台区域");
    pxTableArea.setTableName("PxTableArea");
    pxTableArea.setHasKeepSections(true);
    pxTableArea.implementsSerializable();
    pxTableArea.addIdProperty().primaryKeyAsc();

    pxTableArea.addStringProperty("delFlag").javaDocField("虚拟删除 0：正常 1：删除 2：审核").codeBeforeField("@Expose");
    pxTableArea.addStringProperty("objectId").javaDocField("对应服务器id").codeBeforeField("@SerializedName(\"id\") @Expose");
    pxTableArea.addStringProperty("type").javaDocField("区域(0:大厅，1包厢)").codeBeforeField("@Expose");
    pxTableArea.addStringProperty("name").javaDocField("名称").codeBeforeField("@Expose");
  }
  /**
   * 促销计划信息
   */
  private static void addPxPromotioInfo(Schema schema) {
    pxPromotioInfo = schema.addEntity("PxPromotioInfo");
    pxPromotioInfo.setTableName("PromotioInfo");
    pxPromotioInfo.setHasKeepSections(true);
    pxPromotioInfo.implementsSerializable();
    pxPromotioInfo.addIdProperty().primaryKeyAsc();

    pxPromotioInfo.addStringProperty("objectId").javaDocField("对应服务器id").codeBeforeField("@SerializedName(\"id\") @Expose");
    pxPromotioInfo.addStringProperty("name").javaDocField("促销计划名").codeBeforeField("@Expose");
    pxPromotioInfo.addStringProperty("code").javaDocField("促销编码").codeBeforeField("@Expose");
    pxPromotioInfo.addStringProperty("type").javaDocField("促销计划类型（0：长期有效 1：指定时间 2：每周特定）").codeBeforeField("@Expose");
    pxPromotioInfo.addStringProperty("startTime").javaDocField("开始时间").codeBeforeField("@Expose");
    pxPromotioInfo.addStringProperty("endTime").javaDocField("结束时间").codeBeforeField("@Expose");
    pxPromotioInfo.addDateProperty("startDate").javaDocField("开始日期").codeBeforeField("@Expose");
    pxPromotioInfo.addDateProperty("endDate").javaDocField("结束日期").codeBeforeField("@Expose");
    pxPromotioInfo.addStringProperty("weekly").javaDocField("每周几有效").codeBeforeField("@Expose");



    pxPromotioInfo.addStringProperty("delFlag").javaDocField("虚拟删除 0：正常 1：删除 2：审核").codeBeforeField("@Expose");
  }

  /**
   * 促销计划详情
   */
  private static void addPxPromotioDetails(Schema schema) {
    pxPromotioDetails = schema.addEntity("PxPromotioDetails");
    pxPromotioDetails.setTableName("PromotioDetails");
    pxPromotioDetails.setHasKeepSections(true);
    pxPromotioDetails.implementsSerializable();
    pxPromotioDetails.addIdProperty().primaryKeyAsc();

    pxPromotioDetails.addStringProperty("objectId").javaDocField("对应服务器id").codeBeforeField("@SerializedName(\"id\") @Expose");
    pxPromotioDetails.addDoubleProperty("promotionalPrice").javaDocField("促销价").codeBeforeField("@Expose");
    pxPromotioDetails.addStringProperty("delFlag").javaDocField("虚拟删除 0：正常 1：删除 2：审核").codeBeforeField("@Expose");
    //Product
    Property pxProductInfoId = pxPromotioDetails.addLongProperty("pxProductInfoId").notNull().getProperty();
    pxPromotioDetails.addToOne(pxProductInfo, pxProductInfoId, "dbProduct");
    //Format
    Property pxFormatId = pxPromotioDetails.addLongProperty("pxFormatId").getProperty();
    pxPromotioDetails.addToOne(pxFormatInfo, pxFormatId, "dbFormat");
  }
  /**
   * 关联 促销计划信息和促销计划详情
   */
  private static void linkPromInfoAndPromDetails(Schema schema) {
    Property pxPromotioInfoId = pxPromotioDetails.addLongProperty("pxPromotioInfoId").notNull().getProperty();
    pxPromotioDetails.addToOne(pxPromotioInfo, pxPromotioInfoId, "dbPromotio");

    ToMany promInfoToPromDetails = pxPromotioInfo.addToMany(pxPromotioDetails, pxPromotioInfoId);
    promInfoToPromDetails.setName("dbPromDetailsList");
  }
}
