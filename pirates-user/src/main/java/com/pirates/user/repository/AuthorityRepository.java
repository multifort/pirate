package com.pirates.user.repository;

import com.pirates.user.entity.Authority;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * @Describtion
 * @Author lining
 * @Time 2018/12/28
 * @Version 1.0.0
 */
@Repository
public interface AuthorityRepository extends JpaRepository<Authority, Long> {
}
