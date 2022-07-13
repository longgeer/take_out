package com.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.reggie.common.R;
import com.reggie.entity.User;
import com.reggie.service.SendMailService;
import com.reggie.service.UserService;
import com.reggie.utils.ValidateCodeUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpSession;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private SendMailService sendMailService;

    /**
     * 发送验证码
     *
     * @param user
     * @param session
     * @return R
     */
    @PostMapping("/sendMsg")
    public R<String> sendMsg(@RequestBody User user, HttpSession session) {
        //获取邮箱号
        String email = user.getPhone();
        if (StringUtils.isNotEmpty(email)) {
            //生产4位验证码
            String code = ValidateCodeUtils.generateValidateCode(4).toString();
            log.info("生成的验证码:{}", code);
            //发送邮件
            sendMailService.sendUserMail(email, code);
            //将生成的验证码保存到session
            session.setAttribute(email, code);
            return R.success("验证码已发送邮箱！" + code);
        }
        return R.error("发送失败！请稍后再试");
    }

    /**
     * 移动端用户登录
     * （因为前端没改，所以前端还是用的phone，但它表示email）
     *
     * @param map
     * @param session
     * @return R
     */
    @PostMapping("/login")
    public R<User> login(@RequestBody Map map, HttpSession session) {
        log.info(map.toString());
        //获取输入的email
        String email = map.get("phone").toString();
        //获取输入的验证码
        String code = map.get("code").toString();
        //从session中获取保存验证码
        Object codeInSession = session.getAttribute(email);
        //进行验证码比对
        if (codeInSession != null && codeInSession.equals(code)) {
            //比对成功，登录
            //判断邮箱是否为新用户，如果是，自动完成注册（添入表中）
            LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(User::getPhone, email);
            User user = userService.getOne(queryWrapper);
            if (user == null) {
                log.info("该用户还未注册，新添该用户到user表中。");
                user = new User();
                user.setPhone(email);
                user.setStatus(1);
                userService.save(user);
            }
            //设置session通过过滤器
            session.setAttribute("user", user.getId());
            return R.success(user);
        }
        return R.error("验证码错误!");
    }
}
