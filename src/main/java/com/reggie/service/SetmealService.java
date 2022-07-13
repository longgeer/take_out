package com.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.reggie.dto.SetmealDto;
import com.reggie.entity.Setmeal;

import java.util.List;

public interface SetmealService extends IService<Setmeal> {
    //新增套餐，同时需要保存套餐和菜品的关系
    public void saveWithDish(SetmealDto setmealDto);
    //删除setmeal套餐信息，同时删除setmeal_dish中的关联菜品
    public void removeWithDish(List<Long> ids);
    //根据id查询套餐信息和对应菜品信息
    SetmealDto getByIdWithSetmealDish(Long id);
    //修改setmeal套餐信息，同时修改setmeal_dish中的关联菜品
    void updateWithSetmealDish(SetmealDto setmealDto);
}
