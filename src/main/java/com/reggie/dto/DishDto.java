package com.reggie.dto;

import com.reggie.entity.Dish;
import com.reggie.entity.DishFlavor;
import lombok.Data;
import java.util.ArrayList;
import java.util.List;

@Data
public class DishDto extends Dish {

    //菜品口味
    private List<DishFlavor> flavors = new ArrayList<>();

    private String categoryName;

    //份数
    private Integer copies;
}
