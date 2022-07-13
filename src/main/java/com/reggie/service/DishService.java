package com.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.reggie.dto.DishDto;
import com.reggie.entity.Category;
import com.reggie.entity.Dish;

public interface DishService extends IService<Dish> {
    //新增菜品，同时插入对应口味数据，需要操作两张表dish，dish_flavor
    public void saveWithFlavor(DishDto dishDto);
    //根据id查询菜品信息和对应口味信息
    public DishDto getByIdWithFlavor(Long id);

    //更新菜品信息同时更新口味信息，需要操作两张表dish，dish_flavor
    void updateWithFlavor(DishDto dishDto);
}
