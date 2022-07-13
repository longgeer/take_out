package com.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.reggie.common.CustomException;
import com.reggie.common.R;
import com.reggie.dto.DishDto;
import com.reggie.entity.Category;
import com.reggie.entity.Dish;
import com.reggie.entity.DishFlavor;
import com.reggie.entity.Setmeal;
import com.reggie.service.CategoryService;
import com.reggie.service.DishFlavorService;
import com.reggie.service.DishService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/dish")
public class DishController {

    @Autowired
    private DishService dishService;

    @Autowired
    private DishFlavorService dishFlavorService;

    @Autowired
    private CategoryService categoryService;

    /**
     * 新增菜品
     *
     * @param dishDto
     * @return
     */
    @PostMapping
    public R<String> save(@RequestBody DishDto dishDto) {
        log.info(dishDto.toString());
        dishService.saveWithFlavor(dishDto);
        return R.success("新增菜品成功");
    }

    /**
     * 菜品分页查询
     *
     * @param page
     * @param pageSize
     * @param name
     * @return
     */
    @GetMapping("/page")
    public R<Page> page(int page, int pageSize, String name) {
        //构造分页构造器对象
        Page<Dish> dishPage = new Page<>(page, pageSize);
        Page<DishDto> dishDtoPage = new Page<>();
        //条件构造器
        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
        //添加过滤条件
        queryWrapper.like(name != null, Dish::getName, name);
        //条件构造:只构造未逻辑删除的
        queryWrapper.eq(Dish::getIsDeleted,0);
        //排序条件
        queryWrapper.orderByDesc(Dish::getUpdateTime);
        //执行分页查询
        dishService.page(dishPage, queryWrapper);
        //对象拷贝
        BeanUtils.copyProperties(dishPage, dishDtoPage, "records");

        //设置dishDtoPage的records
        List<Dish> dishRecords = dishPage.getRecords();
        List<DishDto> dishDtoRecords = dishRecords.stream().map((item) -> {
            DishDto dishDto = new DishDto();
            BeanUtils.copyProperties(item, dishDto);
            Long categoryId = item.getCategoryId();//拿到该分类id
            //根据分类id查分类表
            Category category = categoryService.getById(categoryId);
            //判断空条件
            if (category != null) {
                String categoryName = category.getName();//获取该分类名称
                dishDto.setCategoryName(categoryName);//将该分类名称赋给dishDto对象
            }
            return dishDto;
        }).collect(Collectors.toList());

        dishDtoPage.setRecords(dishDtoRecords);
        return R.success(dishDtoPage);
    }

    /**
     * 根据id查询菜品信息和口味信息（修改时回显）
     *
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public R<DishDto> get(@PathVariable Long id) {
        DishDto dishDto = dishService.getByIdWithFlavor(id);
        return R.success(dishDto);
    }

    /**
     * 修改菜品
     *
     * @param dishDto
     * @return
     */
    @PutMapping
    public R<String> update(@RequestBody DishDto dishDto) {
        log.info(dishDto.toString());
        dishService.updateWithFlavor(dishDto);
        return R.success("修改成功");
    }

    /**
     * 根据菜品种类查询对应菜品
     * @param dish
     * @return
     */
   /* @GetMapping("/list")
    public R<List<Dish>> list(Dish dish) {
        //查询条件构造
        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
        //构造套餐添加页面->添加菜品框里的按菜类别查询的条件
        queryWrapper.eq(dish.getCategoryId() != null, Dish::getCategoryId, dish.getCategoryId());
        //构造套餐添加页面->添加菜品框里的按名称模糊查询条件
        queryWrapper.like(dish.getName()!=null,Dish::getName,dish.getName());
        //查询状态为1，表示只查启售状态
        queryWrapper.eq(Dish::getStatus, 1);
        //排序条件构造
        queryWrapper.orderByAsc(Dish::getSort).orderByDesc(Dish::getUpdateTime);
        List<Dish> list = dishService.list(queryWrapper);
        return R.success(list);
    }*/
    @GetMapping("/list")
    public R<List<DishDto>> list(Dish dish) {
        //查询条件构造
        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
        //构造套餐添加页面->添加菜品框里的按菜类别查询的条件
        queryWrapper.eq(dish.getCategoryId() != null, Dish::getCategoryId, dish.getCategoryId());
        //构造套餐添加页面->添加菜品框里的按名称模糊查询条件
        queryWrapper.like(dish.getName()!=null,Dish::getName,dish.getName());
        //查询状态为1，表示只查启售状态
        queryWrapper.eq(Dish::getStatus, 1);
        //排序条件构造
        queryWrapper.orderByAsc(Dish::getSort).orderByDesc(Dish::getUpdateTime);
        List<Dish> list = dishService.list(queryWrapper);

        //-----------------追加Dto的数据------------------------
        List<DishDto> dishDtoList = list.stream().map((item) -> {
            DishDto dishDto = new DishDto();
            BeanUtils.copyProperties(item, dishDto);
            Long dishId = item.getId();//获取dish的id
            //根据id查询口味
            LambdaQueryWrapper<DishFlavor> dishFlavorLambdaQueryWrapper = new LambdaQueryWrapper<>();
            dishFlavorLambdaQueryWrapper.eq(DishFlavor::getDishId, dishId);
            List<DishFlavor> dishFlavorList = dishFlavorService.list(dishFlavorLambdaQueryWrapper);
            dishDto.setFlavors(dishFlavorList);
            return dishDto;
        }).collect(Collectors.toList());
        //List<DishDto> dishDtoList = null;
        return R.success(dishDtoList);
    }

    /**
     * 停售启售操作
     * @param ids
     * @return
     */
    @PostMapping("/status/{status}")
    public R<String> updateStatus(@RequestParam List<Long> ids,@PathVariable int status) {
        log.info("需要修改状态的ids:{}，启售操作1还是停售操作0:{}", ids,status);
        //条件构造
        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
        //构造查询条件：查询出选中所有id的菜品
        queryWrapper.in(Dish::getId, ids);
        List<Dish> dishList = dishService.list(queryWrapper);
        if (dishList.size()==0){
            throw new CustomException("您没有选中任何菜品！");
        }
        //修改
        for (int i = 0; i < dishList.size(); i++) {
            Dish dish = dishList.get(i);
            //1起售,0停售
            if (status == 1) {
                //起售操作，将状态置为1
                dish.setStatus(1);
            } else {
                //停售，将状态置为0
                dish.setStatus(0);
            }
            //更新
            dishService.updateById(dish);
        }
        return R.success("修改状态成功");
    }

    /**
     * 逻辑删除菜品信息
     * （因为没有用到该处mybatisplus，此处删除后相应关联处也需要修改，如page等请求）
     * 目前只在page中修改了：逻辑删除不展示
     * @param ids
     * @return
     */
    @DeleteMapping
    public R<String> deleteLogic(@RequestParam List<Long> ids){
        log.info("逻辑删除ids:{}",ids);
        //1.构造查询条件，先将选中ids查询出来
        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(Dish::getId,ids);
        List<Dish> dishList = dishService.list(queryWrapper);
        //2.遍历结果集合，将每个对象的status置为1，表示逻辑上删除
        if (dishList.size()==0){
            throw new CustomException("您没有选中任何菜品！");
        }
        for (int i = 0; i < dishList.size(); i++) {
            Dish dish = dishList.get(i);
            dish.setIsDeleted(1);
            dishService.updateById(dish);
        }

        return R.success("逻辑删除成功！");
    }
}
