package com.pirates.user.repository;

import com.pirates.user.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * @Describtion
 * @Author lining
 * @Time 2018/12/28
 * @Version 1.0.0
 */
@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
}
