package com.example.demo.controller;

import com.example.demo.dto.PageResult;
import com.example.demo.dto.UserCreateRequest;
import com.example.demo.dto.UserUpdateRequest;
import com.example.demo.entity.User;
import com.example.demo.exception.BusinessException;
import com.example.demo.service.UserService;
import com.example.demo.util.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * UserController 集成测试
 */
@SpringBootTest
@AutoConfigureMockMvc
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    @MockBean
    private JwtUtil jwtUtil;

    private User testUser;
    private String validToken;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setStatus(1);
        testUser.setCreateTime(LocalDateTime.now());
        testUser.setUpdateTime(LocalDateTime.now());

        validToken = "valid-test-token";
    }

    private void mockValidAuth() {
        when(jwtUtil.validateToken(validToken)).thenReturn(true);
        when(jwtUtil.getUserIdFromToken(validToken)).thenReturn(1L);
        when(jwtUtil.getUsernameFromToken(validToken)).thenReturn("testuser");
    }

    @Test
    @DisplayName("查询用户列表 - 成功(带Token)")
    void listUsers_withAuth_shouldReturnPageResult() throws Exception {
        // given
        mockValidAuth();

        PageResult<User> pageResult = new PageResult<>();
        pageResult.setList(Arrays.asList(testUser));
        pageResult.setTotal(1L);
        pageResult.setPages(1);
        pageResult.setPageNum(1);
        pageResult.setPageSize(10);

        when(userService.listUsers(any())).thenReturn(pageResult);

        // when & then
        mockMvc.perform(get("/api/users")
                .header("Authorization", "Bearer " + validToken)
                .param("pageNum", "1")
                .param("pageSize", "10"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200))
            .andExpect(jsonPath("$.data.list").isArray())
            .andExpect(jsonPath("$.data.list[0].username").value("testuser"));
    }

    @Test
    @DisplayName("查询用户列表 - 未认证")
    void listUsers_noAuth_shouldReturn401() throws Exception {
        // when & then
        mockMvc.perform(get("/api/users"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("查询用户详情 - 成功")
    void getUser_withAuth_shouldReturnUser() throws Exception {
        // given
        mockValidAuth();
        when(userService.getUserById(1L)).thenReturn(testUser);

        // when & then
        mockMvc.perform(get("/api/users/1")
                .header("Authorization", "Bearer " + validToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200))
            .andExpect(jsonPath("$.data.username").value("testuser"));
    }

    @Test
    @DisplayName("查询用户详情 - 用户不存在")
    void getUser_notFound_shouldReturn404() throws Exception {
        // given
        mockValidAuth();
        when(userService.getUserById(999L)).thenReturn(null);

        // when & then
        mockMvc.perform(get("/api/users/999")
                .header("Authorization", "Bearer " + validToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(404));
    }

    @Test
    @DisplayName("创建用户 - 成功")
    void createUser_withAuth_shouldReturnUser() throws Exception {
        // given
        mockValidAuth();

        UserCreateRequest request = new UserCreateRequest();
        request.setUsername("newuser");
        request.setPassword("password123"); // 必填字段
        request.setEmail("new@example.com");

        User newUser = new User();
        newUser.setId(2L);
        newUser.setUsername("newuser");
        newUser.setEmail("new@example.com");

        when(userService.createUser(any(User.class))).thenReturn(newUser);

        // when & then
        mockMvc.perform(post("/api/users")
                .header("Authorization", "Bearer " + validToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200))
            .andExpect(jsonPath("$.data.username").value("newuser"));
    }

    @Test
    @DisplayName("创建用户 - 参数校验失败")
    void createUser_invalidParams_shouldReturn400() throws Exception {
        // given
        mockValidAuth();

        UserCreateRequest request = new UserCreateRequest();
        request.setUsername(""); // 空用户名
        request.setPassword("123"); // 密码太短（少于6位）
        request.setEmail("invalid-email"); // 无效邮箱

        // when & then
        mockMvc.perform(post("/api/users")
                .header("Authorization", "Bearer " + validToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value(400));
    }

    @Test
    @DisplayName("更新用户 - 成功")
    void updateUser_withAuth_shouldReturnUser() throws Exception {
        // given
        mockValidAuth();

        UserUpdateRequest request = new UserUpdateRequest();
        request.setId(1L);
        request.setUsername("updateduser");
        request.setEmail("updated@example.com");

        User updatedUser = new User();
        updatedUser.setId(1L);
        updatedUser.setUsername("updateduser");
        updatedUser.setEmail("updated@example.com");

        when(userService.updateUser(any(User.class))).thenReturn(updatedUser);

        // when & then
        mockMvc.perform(put("/api/users/1")
                .header("Authorization", "Bearer " + validToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("删除用户 - 成功")
    void deleteUser_withAuth_shouldReturn200() throws Exception {
        // given
        mockValidAuth();

        // when & then
        mockMvc.perform(delete("/api/users/1")
                .header("Authorization", "Bearer " + validToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("分页参数 - pageSize超过上限应被截断")
    void listUsers_largePageSize_shouldBeCapped() throws Exception {
        // given
        mockValidAuth();

        PageResult<User> pageResult = new PageResult<>();
        pageResult.setList(Arrays.asList(testUser));
        pageResult.setTotal(1L);

        when(userService.listUsers(any())).thenReturn(pageResult);

        // when & then
        // 请求 pageSize=10000，应该被截断到 100
        mockMvc.perform(get("/api/users")
                .header("Authorization", "Bearer " + validToken)
                .param("pageNum", "1")
                .param("pageSize", "10000"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200));
    }
}