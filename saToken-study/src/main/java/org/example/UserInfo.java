package org.example;

import lombok.Data;

@Data
public class UserInfo {
    private Integer id;
    private String name;
    private String password;
    private String phoneNumber;
    private String email;


    public UserInfo(Integer id, String name, String password) {
        this.id = id;
        this.name = name;
        this.password = password;
        this.phoneNumber= "11122223333";
        this.email = "kikoRepub@loveHuTao.com";
    }
}
