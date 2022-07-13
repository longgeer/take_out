package com.reggie;

import com.reggie.common.BaseContext;
import com.reggie.entity.Orders;
import com.reggie.service.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class ReggieTakeOutApplicationTests {

    @Autowired
    private SendMailService sendMailService;
    @Autowired
    private OrdersService ordersService;

    @Test
    void contextLoads() {
    }

    /**
     * 邮件发送测试
     */
    @Test
    void sendEmailTest(){
        String userEmail = "2743999033@qq.com";
        String code = "1024";
        sendMailService.sendUserMail(userEmail,code);
    }

    /**
     * 用户下单测试
     * 空数据测试
     */
    @Test
    void ordersServiceTestNull(){
        //测试空数据
        Orders ordersNull = new Orders();
        ordersService.submit(ordersNull);
    }

    /**
     * 用户下单测试
     * 正常测试
     */
    @Test
    void ordersServiceTest(){
        BaseContext.setCurrentId(1534422521296752642L);
        Long currentId = BaseContext.getCurrentId();
        //正常测试
        Orders orders = new Orders();
        //设置测试userId
        orders.setUserId(currentId);
        //设置测试地址
        orders.setAddressBookId(1538716962023018498L);
        ordersService.submit(orders);
    }

}
