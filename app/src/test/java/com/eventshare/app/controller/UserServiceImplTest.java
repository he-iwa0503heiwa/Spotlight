package com.eventshare.app.controller;

import com.eventshare.app.entity.User;
import com.eventshare.app.repository.UserRepository;
import com.eventshare.app.service.impl.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

/*
UserServiceImplのテストクラス
ユーザー管理のビジネスロジックをテスト
 */
@ExtendWith(MockitoExtension.class)
public class UserServiceImplTest {
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserServiceImpl userService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setPassword("testpassword");
        testUser.setBio("testbio");
        testUser.setCreatedAt(LocalDateTime.now());
        testUser.setUpdatedAt(LocalDateTime.now());
    }
    /*
    全ユーザー取得テスト
     */
    @Test
    void testGetAllUsers() {
        //モックの設定
        List<User> users = Arrays.asList(testUser);
        when(userRepository.findAll()).thenReturn(users);

        //テストの実行
        List<User> result = userService.getAllUsers();

        //検証
        assertEquals(1, result.size());
        assertEquals("testuser", result.get(0).getUsername());
        verify(userRepository, times(1)).findAll();
    }

    //ID指定でのユーザー取得テスト
    @Test
    void testGetUserById_Success() {
        //モックの設定
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        //テスト実行
        User result = userService.getUserById(1L);

        //検証
        assertEquals("testuser", result.getUsername());
        assertEquals("testbio", result.getBio());
        verify(userRepository, times(1)).findById(1L);
    }
}
