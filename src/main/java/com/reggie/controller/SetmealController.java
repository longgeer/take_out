package com.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.reggie.common.CustomException;
import com.reggie.common.R;
import com.reggie.dto.DishDto;
import com.reggie.dto.SetmealDishDto;
import com.reggie.dto.SetmealDto;
import com.reggie.entity.Category;
import com.reggie.entity.Dish;
import com.reggie.entity.Setmeal;
import com.reggie.entity.SetmealDish;
import com.reggie.service.CategoryService;
import com.reggie.service.DishService;
import com.reggie.service.SetmealDishService;
import com.reggie.service.SetmealService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 套餐管理
 */

@Slf4j
@RestController
@RequestMapping("/setmeal")
public class SetmealController {

    @Autowired
    private SetmealService setmealService;

    @Autowired
    private SetmealDishService setmealDishService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private DishService dishService;

    /**
     * 新增套餐信息
     *
     * @param setmealDto
     * @return
     */
    @PostMapping
    public R<String> save(@RequestBody SetmealDto setmealDto) {
        log.info("套餐信息：{}", setmealDto);
        setmealService.saveWithDish(setmealDto);
        return R.success("新增套餐信息成功");
    }

    /**
     * 套餐分页查询
     *
     * @param page
     * @param pageSize
     * @param name
     * @return
     */
    @GetMapping("/page")
    public R<Page> page(int page, int pageSize, String name) {
        //分页构造器
        Page<Setmeal> setmealPage = new Page<>(page, pageSize);
        Page<SetmealDto> setmealDtoPage = new Page<>();

        LambdaQueryWrapper<Setmeal> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(name != null, Setmeal::getName, name);
        queryWrapper.orderByDesc(Setmeal::getUpdateTime);
        setmealService.page(setmealPage, queryWrapper);

        //拷贝records以外的属性
        BeanUtils.copyProperties(setmealPage, setmealDtoPage, "records");

        //设置setmealDtoPage的records
        List<Setmeal> setmealRecords = setmealPage.getRecords();
        List<SetmealDto> setmealDtoRecords = setmealRecords.stream().map((item) -> {
            SetmealDto setmealDto = new SetmealDto();
            BeanUtils.copyProperties(item, setmealDto);
            Long categoryId = item.getCategoryId();//拿到分类id
            //根据分类id查找分类
            Category category = categoryService.getById(categoryId);
            if (category != null) {
                //对象setmealDto
                setmealDto.setCategoryName(category.getName());
            }
            return setmealDto;
        }).collect(Collectors.toList());

        setmealDtoPage.setRecords(setmealDtoRecords);
        return R.success(setmealDtoPage);
    }

    /**
     * 删除套餐，只有停售状态才能删除
     * 操作的表：setmeal,setmeal_dish
     *
     * @param ids
     * @return
     */
    @DeleteMapping
    public R<String> delete(@RequestParam List<Long> ids) {
        log.info("ids:{}", ids);
        setmealService.removeWithDish(ids);
        return R.success("删除成功！");
    }

    /**
     * 停售起售
     * @param ids
     * @return
     */
    @PostMapping("/status/{status}")
    public R<String> updateStatus(@RequestParam List<Long> ids,@PathVariable int status) {
        log.info("修改状态的ids:{}", ids);
        //条件构造
        LambdaQueryWrapper<Setmeal> queryWrapper = new LambdaQueryWrapper<>();
        //构造查询条件：查询出选中所有id的菜品
        queryWrapper.in(Setmeal::getId, ids);
        List<Setmeal> setmealList = setmealService.list(queryWrapper);
        if (setmealList.size() == 0) {
            throw new CustomException("您没有选中任何套餐！");
        }
        //修改
        for (int i = 0; i < setmealList.size(); i++) {
            Setmeal setmeal = setmealList.get(i);
            //1起售,0停售
            if (status == 1) {
                //起售操作，将状态置为1
                setmeal.setStatus(1);
            } else {
                //停售，将状态置为0
                setmeal.setStatus(0);
            }
            //更新
            setmealService.updateById(setmeal);
        }

        return R.success("修改状态成功");
    }


    /**
     * 根据id查询套餐信息和其菜品信息（修改时回显）
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public R<SetmealDto> get(@PathVariable Long id) {
        SetmealDto setmealDto = setmealService.getByIdWithSetmealDish(id);
        return R.success(setmealDto);
    }

    /**
     * 修改setmeal套餐信息，同时修改setmeal_dish中的关联菜品
     * @param setmealDto
     * @return
     */
    @PutMapping
    public R<String> update(@RequestBody SetmealDto setmealDto){
        log.info(setmealDto.toString());
        setmealService.updateWithSetmealDish(setmealDto);
        return R.success("修改套餐信息成功！");
    }

    /**
     * 根据套餐查询对应菜品
     * @param setmeal
     * @return
     */
    @GetMapping("/list")
    public R<List<Setmeal>> list(Setmeal setmeal) {
        //查询条件构造
        LambdaQueryWrapper<Setmeal> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(setmeal.getCategoryId() != null, Setmeal::getCategoryId, setmeal.getCategoryId());
        //查询状态为1，表示只查启售状态
        queryWrapper.eq(Setmeal::getStatus, 1);
        //排序条件构造
        queryWrapper.orderByDesc(Setmeal::getUpdateTime);
        List<Setmeal> list = setmealService.list(queryWrapper);
        return R.success(list);
    }

    /**
     * 根据套餐id查询其具体的所有菜品信息
     * 因为前台还需要每个dish的份数，所以返回SetmealDishDto
     * @param id
     * @return
     */
    @GetMapping("/dish/{id}")
    public R<List<SetmealDishDto>> dishListBySetmeal(@PathVariable Long id){
        log.info("套餐id：{}",id);
        //查出对应id套餐中的所有菜品
        LambdaQueryWrapper<SetmealDish> setmealDishLambdaQueryWrapper = new LambdaQueryWrapper<>();
        setmealDishLambdaQueryWrapper.eq(id!=null,SetmealDish::getSetmealId,id);
        setmealDishLambdaQueryWrapper.orderByDesc(SetmealDish::getUpdateTime);
        List<SetmealDish> setmealDishList = setmealDishService.list(setmealDishLambdaQueryWrapper);
        //通过setmeal_dish表中的dish_id查询出对应菜品
        List<SetmealDishDto> setmealDishDtoList = setmealDishList.stream().map((item) -> {
            SetmealDishDto setmealDishDto = new SetmealDishDto();
            Long dishId = item.getDishId();//获取dish_id
            Dish dish = dishService.getById(dishId);
            //设置Dto的dish,直接拷贝
            BeanUtils.copyProperties(dish, setmealDishDto);
            //设置Dto的份数
            setmealDishDto.setCopies(item.getCopies());
            return setmealDishDto;
        }).collect(Collectors.toList());
        return R.success(setmealDishDtoList);
    }
}
