package com.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.reggie.common.R;
import com.reggie.dto.OrdersDto;
import com.reggie.entity.OrderDetail;
import com.reggie.entity.Orders;
import com.reggie.service.OrderDetailService;
import com.reggie.service.OrdersService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/order")
public class OrdersController {
    @Autowired
    private OrdersService ordersService;

    @Autowired
    private OrderDetailService orderDetailService;

    /**
     * 下单
     * @param orders
     * @return R
     */
    @PostMapping("/submit")
    public R<String> submit(@RequestBody Orders orders) {
        log.info("订单数据：{}", orders);
        ordersService.submit(orders);
        return R.success("下单成功！");
    }

    //请求 URL: http://localhost:8080/order/userPage?page=1&pageSize=5

    /**
     * 订单信息页查询
     * @param page
     * @param pageSize
     * @return R
     */
    @GetMapping("/userPage")
    public R<Page> page(int page, int pageSize) {
        log.info("page = {},pageSize = {}", page, pageSize);
        //构造分页构造器
        Page<Orders> ordersPage = new Page<>(page, pageSize);
        Page<OrdersDto> ordersDtoPage = new Page<>();

        //添加orders排序条件。按订单时间降序
        LambdaQueryWrapper<Orders> ordersLambdaQueryWrapper = new LambdaQueryWrapper<>();
        ordersLambdaQueryWrapper.orderByDesc(Orders::getCheckoutTime);
        //执行查询orders
        ordersService.page(ordersPage, ordersLambdaQueryWrapper);

        //对象拷贝
        BeanUtils.copyProperties(ordersPage, ordersDtoPage, "records");
        //设置 ordersDtoPage 的records
        List<Orders> ordersPageRecords = ordersPage.getRecords();
        List<OrdersDto> ordersDtoList = ordersPageRecords.stream().map((item) -> {
            OrdersDto ordersDto = new OrdersDto();
            //将orders信息拷贝给ordersDto
            BeanUtils.copyProperties(item, ordersDto);
            //获取orderId
            Long orderId = item.getId();
            //构造orderDetail的查询条件
            LambdaQueryWrapper<OrderDetail> orderDetailLambdaQueryWrapper = new LambdaQueryWrapper<>();
            //查询出每个订单的订单详情(各种菜品及其数量等等)
            orderDetailLambdaQueryWrapper.eq(OrderDetail::getOrderId, orderId);
            List<OrderDetail> orderDetailList = orderDetailService.list(orderDetailLambdaQueryWrapper);
            //给ordersDto设置orderDetails
            ordersDto.setOrderDetails(orderDetailList);
            return ordersDto;
        }).collect(Collectors.toList());

        //设置ordersDtoPage的records
        ordersDtoPage.setRecords(ordersDtoList);

        return R.success(ordersDtoPage);
    }

    /**
     * 后台订单明细分页查询
     * @param page
     * @param pageSize
     * @param number
     * @param beginTime
     * @param endTime
     * @return R
     */
    @GetMapping("/page")
    public R<Page> page2(int page, int pageSize, Long number, String beginTime, String endTime) {
        log.info("number:{},beginTime:{},endTime:{}", number, beginTime, endTime);
        Page<Orders> orderPage = new Page<>(page, pageSize);

        LambdaQueryWrapper<Orders> orderLambdaQueryWrapper = new LambdaQueryWrapper<>();
        //构造排序性条件
        orderLambdaQueryWrapper.orderByDesc(Orders::getCheckoutTime);
        //构造查询条件
        orderLambdaQueryWrapper.eq(number != null, Orders::getId, number);
        orderLambdaQueryWrapper.between(beginTime != null && endTime != null, Orders::getCheckoutTime, beginTime, endTime);

        ordersService.page(orderPage, orderLambdaQueryWrapper);

        return R.success(orderPage);
    }

    /**
     * 订单派送
     * @param orders
     * @return R
     */
    @PutMapping
    public R<String> paiSong(@RequestBody Orders orders){
        log.info("order:{}",orders);
        //状态3，已派送
        orders.setStatus(orders.getStatus());
        ordersService.updateById(orders);
        return R.success("派送成功！");
    }

}
