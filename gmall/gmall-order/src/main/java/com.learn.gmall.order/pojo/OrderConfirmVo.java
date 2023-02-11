package com.learn.gmall.order.pojo;

import com.learn.gmall.ums.entity.UserAddressEntity;
import lombok.Data;
import com.learn.gmall.oms.vo.OrderItemVo;
import java.util.List;

@Data
public class OrderConfirmVo {

    // 收货地址列表
    private List<UserAddressEntity> addresses;

    // 送货清单
    private List<OrderItemVo> items;

    private Integer bounds;// 购物积分

    private String orderToken; // 防止重复提交的唯一标识，以保证幂等性
}
