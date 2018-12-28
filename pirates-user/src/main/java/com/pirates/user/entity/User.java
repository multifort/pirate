package com.pirates.user.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;

/**
 * @Describtion User entity
 * @Author lining
 * @Time 2018/12/28
 * @Version 1.0.0
 */
@Entity
@Data
public class User {
    @Id
    @GeneratedValue
    private Long id;

    @Column(name = "name", length = 64)
    private String name;

    @Column(name = "password", length = 128)
    private String password;
}
