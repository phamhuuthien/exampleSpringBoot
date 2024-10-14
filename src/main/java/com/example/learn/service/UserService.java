package com.example.learn.service;

import com.example.learn.dto.request.UserRequestDTO;
import com.example.learn.dto.response.PageResponse;
import com.example.learn.dto.response.UserDetailResponse;
import com.example.learn.model.User;
import com.example.learn.util.UserStatus;

import java.util.List;

public interface UserService {
    User findByUsernameAndPassword(String username, String password);
    long saveUser(UserRequestDTO request);
    void updateUser(long userId, UserRequestDTO request);
    void changeStatus(long userId, UserStatus status);
    void deleteUser(long userId);
    UserDetailResponse getUser(long userId);
    PageResponse<?> getAllUserWithSortBy(int pageNo, int pageSize, String sortBy);
    PageResponse<?> getAllUserWithSortByMutipleColumn(int pageNo, int pageSize, String... sorts);
    PageResponse<?> getAllUserWithSortByAndSearch(int pageNo, int pageSize,String search, String sortBy);
    PageResponse<?> advanceSearchCriteria(int pageNo, int pageSize,String sortBy,String address, String... search);
}
