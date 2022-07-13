package com.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.reggie.common.CustomException;
import com.reggie.common.R;
import com.reggie.dto.DishDto;
import com.reggie.dto.SetmealDto;
import com.reggie.entity.Setmeal;
import com.reggie.entity.SetmealDish;
import com.reggie.mapper.SetmealMapper;
import com.reggie.service.SetmealDishService;
import com.reggie.service.SetmealService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class SetmealServiceImpl extends ServiceImpl<SetmealMapper, Setmeal> implements SetmealService {

    @Autowired
    private SetmealDishService setmealDishService;

    /**
     * 新增套餐，同时需要保存套餐和菜品的关系
     * @param setmealDto
     */
    @Override
    @Transactional
    public void saveWithDish(SetmealDto setmealDto) {
        //保存套餐基本信息，操作setmeal表执行insert
        this.save(setmealDto);

        //保存套餐和菜品的关联信息，操作setmeal_dish表，执行insert操作
        List<SetmealDish> setmealDishes = setmealDto.getSetmealDishes();
        setmealDishes.stream().map((item) ->{
            item.setSetmealId(setmealDto.getId());
            return item;
        }).collect(Collectors.toList());
        setmealDishService.saveBatch(setmealDishes);
    }

    /**
     * 删除setmeal套餐信息，同时删除setmeal_dish中的关联菜品
     * @param ids
     */
    @Override
    @Transactional
    public void removeWithDish(List<Long> ids) {
        //1.查询套餐状态，确定是否可以删除
        LambdaQueryWrapper<Setmeal> queryWrapper = new LambdaQueryWrapper<>();
        //构造条件，状态为启用1
        queryWrapper.in(Setmeal::getId,ids);
        queryWrapper.eq(Setmeal::getStatus,1);
        int count = this.count(queryWrapper);
        if (count>0){
            //2.不能删除，抛业务异常
            throw new CustomException("有套餐还在售卖中，不能删除！");
        }
        //3.可以删除，先删除套餐信息
        this.removeByIds(ids);
        //4.再删除setmeal_dish表中关联的套餐菜品信息
        //构造条件，查出在该套餐中的菜品在setmeal_dish中的id
        LambdaQueryWrapper<SetmealDish> queryWrapper2 = new LambdaQueryWrapper<>();
        queryWrapper2.in(SetmealDish::getSetmealId,ids);
        setmealDishService.remove(queryWrapper2);
    }

    /**s
     * 根据id查询套餐信息和对应菜品信息
     * @param id
     */
    @Override
    public SetmealDto getByIdWithSetmealDish(Long id) {
        //查询setmeal套餐基本信息表
        Setmeal setmeal = this.getById(id);

        //基本信息拷贝
        SetmealDto setmealDto = new SetmealDto();
        BeanUtils.copyProperties(setmeal,setmealDto);

        //查寻套餐菜品setmeal_dish表
        LambdaQueryWrapper<SetmealDish> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SetmealDish::getSetmealId,id);
        List<SetmealDish> setmealDishList = setmealDishService.list(queryWrapper);
        //设置菜品信息
        setmealDto.setSetmealDishes(setmealDishList);
        return setmealDto;
    }

    /**
     * 修改setmeal套餐信息，同时修改setmeal_dish中的关联菜品
     * @param setmealDto
     */
    @Override
    @Transactional
    public void updateWithSetmealDish(SetmealDto setmealDto) {
        //更新setmeal表信息
        this.updateById(setmealDto);
        //先删除setmeal_dish数据，即delete操作
        LambdaQueryWrapper<SetmealDish> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SetmealDish::getSetmealId,setmealDto.getId());
        setmealDishService.remove(queryWrapper);
        //添加操作，添加提交过来的菜品数据，即对setmeal_dish表进行insert操作
        List<SetmealDish> setmealDishes = setmealDto.getSetmealDishes();
        //保证套餐的id存入setmeal_dish表中
        setmealDishes.stream().map((item) ->{
            item.setSetmealId(setmealDto.getId());
            return item;
        }).collect(Collectors.toList());
        setmealDishService.saveBatch(setmealDishes);
    }
}
