package com.sczg.apple.utils.eventbus;

public interface EventCons {
    String ADDRESS_REFRESH = "address_refresh";//新增收货地址通知列表刷新
    String UPDATE_NAME = "update_name";//修改昵称通知前一个页面刷新
    String SWITCH_COMMENT = "switch_comment";//商品页切换到评价页面
    String SHOP_REFRESH = "shop_refresh";//通知购物车刷新数据
    String ADDRESS_SELECT = "address_select";//创建订单时选择收货地址，通知更新
    String COMMENT_REFRESH = "comment_refresh";//评论商品后，通知更新列表状态
    String WX_PAY_SUCCESS = "wx_pay_success";//微信支付成功
    String WX_PAY_FAIL = "wx_pay_fail";//微信支付失败
}
