package com.eventshare.app.repository;

import com.eventshare.app.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    //ユーザー詳細検索
    Optional<User> findByUsername(String username);

    //ユーザー存在チェック
    boolean existByUsername(String username);
}