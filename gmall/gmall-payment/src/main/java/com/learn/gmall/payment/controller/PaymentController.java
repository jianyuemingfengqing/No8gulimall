package com.learn.gmall.payment.controller;

import com.alibaba.fastjson.JSON;
import com.alipay.api.AlipayApiException;
import com.learn.gmall.common.exception.OrderException;
import com.learn.gmall.oms.entity.OrderEntity;
import com.learn.gmall.payment.config.AlipayTemplate;
import com.learn.gmall.payment.interceptors.LoginInterceptor;
import com.learn.gmall.payment.pojo.PayAsyncVo;
import com.learn.gmall.payment.pojo.PayVo;
import com.learn.gmall.payment.pojo.PaymentInfoEntity;
import com.learn.gmall.payment.pojo.UserInfo;
import com.learn.gmall.payment.service.PaymentService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.math.BigDecimal;
import java.util.Date;

@Controller
public class PaymentController {

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private AlipayTemplate alipayTemplate;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @GetMapping("pay.html")
    public String pay(@RequestParam("orderToken") String orderToken, Model model) {

        // 根据订单编号查询订单
        OrderEntity orderEntity = this.paymentService.queryOrderByToken(orderToken);

        // 判断订单是否为空，如果为空抛出异常
        if (orderEntity == null) {
            throw new OrderException("您要支付的订单不存在！");
        }

        // 判断订单状态是否是待付款状态，如果不是则抛出异常
        if (orderEntity.getStatus() != 0) {
            throw new OrderException("您要支付的订单不能支付！");
        }

        // 判断这个订单是否属于当前用户
        UserInfo userInfo = LoginInterceptor.getUserInfo();
        Long userId = userInfo.getUserId();
        if (userId != orderEntity.getUserId()) {
            throw new OrderException("该订单不属于您！");
        }

        model.addAttribute("orderEntity", orderEntity);

        return "pay";
    }

    @GetMapping("alipay.html")
    @ResponseBody // 以其他视图形式展示方法的返回结果集
    public String alipay(@RequestParam("orderToken") String orderToken) {

        // 根据订单编号查询订单
        OrderEntity orderEntity = this.paymentService.queryOrderByToken(orderToken);

        // 判断订单是否为空，如果为空抛出异常
        if (orderEntity == null) {
            throw new OrderException("您要支付的订单不存在！");
        }

        // 判断订单状态是否是待付款状态，如果不是则抛出异常
        if (orderEntity.getStatus() != 0) {
            throw new OrderException("您要支付的订单不能支付！");
        }

        // 判断这个订单是否属于当前用户
        UserInfo userInfo = LoginInterceptor.getUserInfo();
        Long userId = userInfo.getUserId();
        if (userId != orderEntity.getUserId()) {
            throw new OrderException("该订单不属于您！");
        }

        try {
            // 调用支付宝接口打开支付页面
            PayVo payVo = new PayVo();
            payVo.setOut_trade_no(orderToken);
            // 注意 注意 注意：不要设置成订单中的金额，一定要写0.01
            payVo.setTotal_amount("0.01");
            payVo.setSubject("谷粒商城支付平台");

            // 同时记录对账信息
            Long payId = this.paymentService.savePaymentInfo(payVo);
            payVo.setPassback_params(payId.toString());

            return this.alipayTemplate.pay(payVo);
        } catch (AlipayApiException e) {
            e.printStackTrace();
        }
        throw new OrderException("打开支付页面失败，请重试！");
    }

    /**
     * 同步回调接口，只做页面跳转，不做订单状态的修改
     *
     * @return
     */
    @GetMapping("pay/success")
    public String paySuccess() {

        return "paysuccess";
    }

    /**
     * 异步回调接口，修改订单状态，并减库存
     *
     * @return
     */
    @PostMapping("pay/ok")
    @ResponseBody
    public String payOk(PayAsyncVo asyncVo) {

        // 1.验签
        Boolean flag = this.alipayTemplate.checkSignature(asyncVo);
        if (!flag) {
            return "failure";
        }

        // 2.校验业务参数：app_id、out_trade_no、total_amount
        String app_id = asyncVo.getApp_id();
        String out_trade_no = asyncVo.getOut_trade_no();
        String total_amount = asyncVo.getTotal_amount();
        String payId = asyncVo.getPassback_params(); // 对账记录的id
        PaymentInfoEntity paymentInfoEntity = this.paymentService.queryPaymentInfo(payId);
        if (!StringUtils.equals(app_id, this.alipayTemplate.getApp_id())
                || !StringUtils.equals(out_trade_no, paymentInfoEntity.getOutTradeNo())
                || paymentInfoEntity.getTotalAmount().compareTo(new BigDecimal(total_amount)) != 0) {
            return "failure";
        }

        // 3.校验支付状态：trade_status    TRADE_SUCCESS
        if (!"TRADE_SUCCESS".equals(asyncVo.getTrade_status())) {
            return "failure";
        }

        // 4.修改对账记录
        paymentInfoEntity.setTradeNo(asyncVo.getTrade_no());
        paymentInfoEntity.setPaymentStatus(1);
        paymentInfoEntity.setCallbackTime(new Date());
        paymentInfoEntity.setCallbackContent(JSON.toJSONString(asyncVo));
        if (this.paymentService.updatePaymentInfo(paymentInfoEntity) == 1) {
            // 5.发送消息给OMS修改订单状态
            this.rabbitTemplate.convertAndSend("ORDER.EXCHANGE", "order.pay", out_trade_no);
        }

        // 6.返回success
        return "success";
    }
}
