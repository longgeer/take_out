package com.reggie.dto;

import com.reggie.entity.Dish;
import lombok.Data;

@Data
public class SetmealDishDto extends Dish {
    //份数
    private Integer copies;
}
