package com.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.reggie.dto.DishDto;
import com.reggie.entity.Dish;
import com.reggie.entity.DishFlavor;
import com.reggie.mapper.DishMapper;
import com.reggie.service.DishFlavorService;
import com.reggie.service.DishService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class DishServiceImpl extends ServiceImpl<DishMapper, Dish> implements DishService {

    @Autowired
    private DishFlavorService dishFlavorService;

    /**
     * 新增菜品同时保存口味数据
     *
     * @param dishDto
     */
    @Override
    @Transactional//两张表，保持事务一致性
    public void saveWithFlavor(DishDto dishDto) {
        log.info("开启新增菜品事务-----------");
        //保存菜品基本信息到菜品表dish
        this.save(dishDto);

        Long dishId = dishDto.getId();//菜品id
        List<DishFlavor> flavors = dishDto.getFlavors();//菜品口味
        //保证菜品的id存入口味表dish_flavor中
        flavors.stream().map((item) -> {
            item.setDishId(dishId);
            return item;
        }).collect(Collectors.toList());
        //保存菜品口味数据到dish_flavor
        dishFlavorService.saveBatch(dishDto.getFlavors());
        log.info("新增菜品事务结束-----------");
    }

    /**
     * 根据id查询菜品信息和对应口味信息
     *
     * @param id
     * @return DishDto
     */
    @Override
    public DishDto getByIdWithFlavor(Long id) {
        //查询菜品基本信息dish表
        Dish dish = this.getById(id);

        DishDto dishDto = new DishDto();
        //基本信息拷贝
        BeanUtils.copyProperties(dish, dishDto);

        //查询口味信息dish_flavor表
        LambdaQueryWrapper<DishFlavor> queryWrapper = new LambdaQueryWrapper<DishFlavor>();
        queryWrapper.eq(DishFlavor::getDishId, dish.getId());
        List<DishFlavor> dishFlavors = dishFlavorService.list(queryWrapper);
        //设置口味信息
        dishDto.setFlavors(dishFlavors);

        return dishDto;
    }

    /**
     * 更新菜品信息同时更新口味信息，需要操作两张表dish，dish_flavor
     *
     * @param dishDto
     */
    @Override
    @Transactional
    public void updateWithFlavor(DishDto dishDto) {
        //更新dish的基本信息
        this.updateById(dishDto);
        //先清理dish_flavor数据，delete操作
        LambdaQueryWrapper<DishFlavor> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(DishFlavor::getDishId, dishDto.getId());
        dishFlavorService.remove(queryWrapper);
        //添加提交过来的口味数据,insert操作
        List<DishFlavor> flavors = dishDto.getFlavors();
        //保证菜品的id存入口味表dish_flavor中
        flavors = flavors.stream().map((item) -> {
            item.setDishId(dishDto.getId());
            return item;
        }).collect(Collectors.toList());
        dishFlavorService.saveBatch(flavors);
    }
}
