package com.think.firewaiter.module;


import com.google.gson.annotations.SerializedName;
import java.util.Date;
import java.util.List;

public class PxOrderInfo implements java.io.Serializable {

  public static final String STATUS_UNFINISH = "0";
  public static final String STATUS_FINISH = "1";
  public static final String STATUS_CANCEL = "2";

  private String QRcodes;

  private boolean chooseDiscount;//是否选择优惠方案

  public static final String DISC_TYPE_NO = "0";//无优惠
  public static final String DISC_TYPE_ENTIRETY = "1";//整体
  public static final String DISC_TYPE_PART = "2";//部分
  public static final String DISC_TYPE_SCHEME = "3";//方案

  /**
   * 对应服务器id
   */
  @SerializedName("id") private String objectId;
  /**
   * 订单编号
   */
  private String code;
  /**
   * 订单总价
   */
  private Double totalPrice;
  /**
   * 应收款
   */
  private Double accountReceivable;
  /**
   * 实收款
   */
  private Double realPrice;
  /**
   * 总的找零
   */
  private Double totalChange;
  /**
   * 优惠金额
   */
  private Double discountPrice;
  /**
   * 支付方式(0:现金 1:刷卡 2：会员卡 3:其他)
   */
  private String payType;
  /**
   * 订单状态(0:未结账 1：结账 2:撤单)
   */
  private String status;
  /**
   * 是否抹零（0：是 1：否  2:请选择
   */
  private String tail;
  /**
   * 抹零金额
   */
  private Double tailMoney;
  /**
   * 是否刷会员卡(0：不刷 1：刷)
   */
  private String useVipCard;
  /**
   * 打折方式(0:整单打折  1：部分打折 2：自定义方案)
   */
  private String discountType;
  /**
   * 折扣率(0-100)
   */
  private Double discountRate;
  /**
   * App使用，实际用餐人数.
   */
  private Integer actualPeopleNumber;
  /**
   * App使用,开始时间
   */
  private java.util.Date statTime;
  /**
   * App使用，结束时间
   */
  private java.util.Date endTime;
  /**
   * App使用,有付款信息
   */
  private Boolean hasPayInfo;
  /**
   * App使用,所在餐桌类型
   */
  private String tableType;
  /**
   * 公司名称
   */
  private String officeName;
  /**
   * 优惠方案类型
   */
  private String discType;

  /**
   * 所在桌台
   */
  private PxTableInfo tableInfo;

  /**
   * 订单详情
   */
  private List<PxOrderDetails> detailsList;

  /**
   * 用户信息
   */
  private User user;

  public String getObjectId() {
    return objectId;
  }

  public void setObjectId(String objectId) {
    this.objectId = objectId;
  }

  public String getCode() {
    return code;
  }

  public void setCode(String code) {
    this.code = code;
  }

  public Double getTotalPrice() {
    return totalPrice;
  }

  public void setTotalPrice(Double totalPrice) {
    this.totalPrice = totalPrice;
  }

  public Double getAccountReceivable() {
    return accountReceivable;
  }

  public void setAccountReceivable(Double accountReceivable) {
    this.accountReceivable = accountReceivable;
  }

  public Double getRealPrice() {
    return realPrice;
  }

  public void setRealPrice(Double realPrice) {
    this.realPrice = realPrice;
  }

  public Double getTotalChange() {
    return totalChange;
  }

  public void setTotalChange(Double totalChange) {
    this.totalChange = totalChange;
  }

  public Double getDiscountPrice() {
    return discountPrice;
  }

  public void setDiscountPrice(Double discountPrice) {
    this.discountPrice = discountPrice;
  }

  public String getPayType() {
    return payType;
  }

  public void setPayType(String payType) {
    this.payType = payType;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public String getTail() {
    return tail;
  }

  public void setTail(String tail) {
    this.tail = tail;
  }

  public Double getTailMoney() {
    return tailMoney;
  }

  public void setTailMoney(Double tailMoney) {
    this.tailMoney = tailMoney;
  }

  public String getUseVipCard() {
    return useVipCard;
  }

  public void setUseVipCard(String useVipCard) {
    this.useVipCard = useVipCard;
  }

  public String getDiscountType() {
    return discountType;
  }

  public void setDiscountType(String discountType) {
    this.discountType = discountType;
  }

  public Double getDiscountRate() {
    return discountRate;
  }

  public void setDiscountRate(Double discountRate) {
    this.discountRate = discountRate;
  }

  public Integer getActualPeopleNumber() {
    return actualPeopleNumber;
  }

  public void setActualPeopleNumber(Integer actualPeopleNumber) {
    this.actualPeopleNumber = actualPeopleNumber;
  }

  public Date getStatTime() {
    return statTime;
  }

  public void setStatTime(Date statTime) {
    this.statTime = statTime;
  }

  public Date getEndTime() {
    return endTime;
  }

  public void setEndTime(Date endTime) {
    this.endTime = endTime;
  }

  public Boolean getHasPayInfo() {
    return hasPayInfo;
  }

  public void setHasPayInfo(Boolean hasPayInfo) {
    this.hasPayInfo = hasPayInfo;
  }

  public String getTableType() {
    return tableType;
  }

  public void setTableType(String tableType) {
    this.tableType = tableType;
  }

  public String getOfficeName() {
    return officeName;
  }

  public void setOfficeName(String officeName) {
    this.officeName = officeName;
  }

  public String getDiscType() {
    return discType;
  }

  public void setDiscType(String discType) {
    this.discType = discType;
  }

  public PxTableInfo getTableInfo() {
    return tableInfo;
  }

  public void setTableInfo(PxTableInfo tableInfo) {
    this.tableInfo = tableInfo;
  }

  public List<PxOrderDetails> getDetailsList() {
    return detailsList;
  }

  public void setDetailsList(List<PxOrderDetails> detailsList) {
    this.detailsList = detailsList;
  }

  public User getUser() {
    return user;
  }

  public void setUser(User user) {
    this.user = user;
  }
}
