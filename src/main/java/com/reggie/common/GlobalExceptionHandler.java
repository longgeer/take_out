package com.reggie.common;

import com.sun.mail.smtp.SMTPSendFailedException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.MailSendException;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.sql.SQLIntegrityConstraintViolationException;

/**
 * 全局异常处理器
 */
@ControllerAdvice(annotations = {RestController.class, Controller.class})
@ResponseBody
@Slf4j
public class GlobalExceptionHandler {
    /**
     * 邮箱发送异常
     * 异常处理方法
     * @return
     */
    @ExceptionHandler(MailSendException.class)
    public R<String> exceptionHandler(MailSendException ex){
        log.error(ex.getMessage());
        if (ex.getMessage().contains("550 Mail content denied")){
            String msg =  "不存在该邮箱！";
            return R.error(msg);
        }
        return R.error("未知错误！");
    }

    /**
     * 异常处理方法
     * @return
     */
    @ExceptionHandler(SQLIntegrityConstraintViolationException.class)
    public R<String> exceptionHandler(SQLIntegrityConstraintViolationException ex){
        log.error(ex.getMessage());
        //异常信息:Duplicate entry '123456' for key 'idx_username'
        if (ex.getMessage().contains("Duplicate entry")){
            String[] strings = ex.getMessage().split(" ");
            String msg = strings[2] +  "已存在！请勿重复添加。";
            return R.error(msg);
        }
        return R.error("未知错误！");
    }

    /**
     * 自定义异常处理
     * @param ex
     * @return
     */
    @ExceptionHandler(CustomException.class)
    public R<String> exceptionHandler(CustomException ex){
        log.error(ex.getMessage());
        return R.error(ex.getMessage());
    }
}
