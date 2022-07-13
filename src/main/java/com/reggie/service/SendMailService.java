package com.reggie.service;

public interface SendMailService {
    void sendMail();
    void sendMailMax();
    void sendUserMail(String userEmail,String code);
}
