package com.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.reggie.common.BaseContext;
import com.reggie.common.R;
import com.reggie.entity.AddressBook;
import com.reggie.service.AddressBookService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/addressBook")
public class AddressBookController {

    @Autowired
    private AddressBookService addressBookService;

    /**
     * 新增地址
     * @param addressBook
     * @return R
     */
    @PostMapping
    public R<AddressBook> save(@RequestBody AddressBook addressBook) {
        //获取用户id
        addressBook.setUserId(BaseContext.getCurrentId());
        log.info("需要新添的地址：{}", addressBook.toString());
        addressBookService.save(addressBook);
        return R.success(addressBook);
    }

    /**
     * 查询用户所有地址信息
     * @return R
     */
    @GetMapping("/list")
    public R<List<AddressBook>> getAll(AddressBook addressBook) {
        //设置当前用户id
        addressBook.setUserId(BaseContext.getCurrentId());
        log.info("addressBook:{}", addressBook);
        //构造查询条件
        LambdaQueryWrapper<AddressBook> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(addressBook.getUserId() != null, AddressBook::getUserId, addressBook.getUserId());
        queryWrapper.orderByDesc(AddressBook::getUpdateTime);
        return R.success(addressBookService.list(queryWrapper));
    }

    /**
     * 设置默认地址
     * @param addressBook
     * @return R
     */
    @PutMapping("/default")
    public R<AddressBook> setDefaultAddress(@RequestBody AddressBook addressBook) {
        addressBook.setUserId(BaseContext.getCurrentId());
        log.info("用户id：{}", addressBook);
        //构造修改条件
        LambdaUpdateWrapper<AddressBook> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(AddressBook::getUserId, addressBook.getUserId());
        //1.先将该用户全部地址置为0（非默认地址）
        updateWrapper.set(AddressBook::getIsDefault, 0);
        addressBookService.update(updateWrapper);
        //2.将传入地址id对于地址改为默认地址
        addressBook.setIsDefault(1);
        addressBookService.updateById(addressBook);
        return R.success(addressBook);
    }

    /**
     * 查询默认地址,下单时需要用到
     * @return R
     */
    @GetMapping("/default")
    public R<AddressBook> selectDefaultAddress(){
        Long currentId = BaseContext.getCurrentId();
        LambdaQueryWrapper<AddressBook> queryWrapper = new LambdaQueryWrapper<>();
        //构造估计用户id查地址条件
        queryWrapper.eq(AddressBook::getUserId,currentId);
        //构造默认地址条件
        queryWrapper.eq(AddressBook::getIsDefault,1);
        AddressBook bookServiceOne = addressBookService.getOne(queryWrapper);
        return R.success(bookServiceOne);
    }

    /**
     * 根据id查询地址信息（编辑展示）
     * @param id
     * @return R
     */
    @GetMapping("/{id}")
    public R<AddressBook> selectById(@PathVariable Long id) {
        log.info("前台传入的地址id:{}", id);
        AddressBook addressBook = addressBookService.getById(id);
        if (addressBook != null) {
            return R.success(addressBook);
        }
        return R.error("没有该地址！");
    }

    /**
     * 根据id删除地址(前台传来的是ids,实际只删除单个)
     * @param ids
     * @return R
     */
    @DeleteMapping
    public R<String> deleteById(@RequestParam Long ids) {
        log.info("前台传入的地址id:{}", ids);
        AddressBook addressBook = addressBookService.getById(ids);
        if (addressBook != null) {
            addressBookService.removeById(ids);
            return R.success("删除成功");
        }
        return R.error("没有该地址！");
    }

    /**
     * 修改地址
     * @param addressBook
     * @return R
     */
    @PutMapping
    public R<String> update(@RequestBody AddressBook addressBook){
        log.info("addressBook:{}",addressBook);
        addressBookService.updateById(addressBook);
        return R.success("修改成功");
    }
}
