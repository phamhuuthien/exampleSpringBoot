package com.example.learn.controller;

import com.example.learn.dto.request.UserRequestDTO;
import com.example.learn.dto.response.ResponseData;
import com.example.learn.dto.response.ResponseError;
import com.example.learn.dto.response.ResponseSuccess;
import com.example.learn.exception.ResourceNotFoundException;
import com.example.learn.service.UserService;
import com.example.learn.util.UserStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/user")
@Validated
@Slf4j
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;
    @PostMapping("")
    public ResponseData<?> addUser(@Valid @RequestBody UserRequestDTO userRequestDTO) {
        try {
            long idUser = userService.saveUser(userRequestDTO);
            return new ResponseData<>(HttpStatus.CREATED.value(), "create successfully",idUser);
        }catch (Exception e){
            return new ResponseError(HttpStatus.BAD_REQUEST.value(),e.getMessage());
        }
    }
    @PutMapping("/{userId}")
    public ResponseData<?> updateUser( @PathVariable @Min(value = 1, message = "User ID must be greater than or equal to 1") long userId, @Valid @RequestBody UserRequestDTO user) {
        try {
            userService.updateUser(userId,user);
            return new ResponseData<>(HttpStatus.OK.value(),"update user successfully");
        }catch (Exception e) {
            log.error("error message", e.getMessage(), e.getCause());
            return new ResponseError(HttpStatus.BAD_REQUEST.value(),"update user faild");
        }
    }
    @PatchMapping("/{userId}")
    public ResponseData<?> changeStatus (@PathVariable long userId, @RequestParam(required = false) UserStatus status){
        try {
            userService.changeStatus(userId,status);
            return new ResponseData<>(HttpStatus.OK.value(),"update status successfully");
        }catch (Exception e) {
            log.error("error message", e.getMessage(), e.getCause());
            return new ResponseError(HttpStatus.BAD_REQUEST.value(),"update status user faild");
        }
    }

    @DeleteMapping("/{userId}")
    public ResponseData<?> deleteUser (@PathVariable int userId){
        return new ResponseData<>(HttpStatus.NO_CONTENT.value(), "delete user");
    }

    @GetMapping("/{userId}")
    public ResponseData<?> getUser(@PathVariable long userId) {
        try {
            return new ResponseData<>(HttpStatus.OK.value(),"get user detail",userService.getUser(userId));
        }catch (ResourceNotFoundException e){
            log.error("error message", e.getMessage(), e.getCause());
            return new ResponseError(HttpStatus.BAD_REQUEST.value(), e.getMessage());
        }
    }

    @GetMapping("/list")
    public ResponseData<?> getAllUser(@RequestParam(defaultValue = "0", required = false) int pageNo,
                                     @Min(10) @RequestParam(defaultValue = "20", required = false) int pageSize,
                                      @RequestParam(required = false) String sortBy
                                    ) {
        return new ResponseData<>(HttpStatus.OK.value(), "users", userService.getAllUserWithSortBy(pageNo,pageSize,sortBy));
    }

    @GetMapping("/list-user-mutiple-sort")
    public ResponseData<?> getAllUserWithMutipleColumn(@RequestParam(defaultValue = "0", required = false) int pageNo,
                                      @Min(1) @RequestParam(defaultValue = "20", required = false) int pageSize,
                                      @RequestParam(required = false) String... sorts
                                    ) {
        return new ResponseData<>(HttpStatus.OK.value(), "users", userService.getAllUserWithSortByMutipleColumn(pageNo,pageSize,sorts));
    }

    @GetMapping("/list-user-sort-search")
    public ResponseData<?> getAllUserWithSortByAndSearch(@RequestParam(defaultValue = "0", required = false) int pageNo,
                                                       @Min(1) @RequestParam(defaultValue = "20", required = false) int pageSize,
                                                       @RequestParam(required = false) String sortBy,
                                                        @RequestParam(required = false) String search
    ) {
        return new ResponseData<>(HttpStatus.OK.value(), "users", userService.getAllUserWithSortByAndSearch(pageNo,pageSize,search,sortBy));
    }

    @GetMapping("/list-user-sort-search-criteria")
    public ResponseData<?> advanceSearchCriteria(@RequestParam(defaultValue = "0", required = false) int pageNo,
                                                         @Min(1) @RequestParam(defaultValue = "20", required = false) int pageSize,
                                                         @RequestParam(required = false) String sortBy,
                                                            @RequestParam(required = false) String address,
                                                         @RequestParam(required = false) String... search
    ) {
        return new ResponseData<>(HttpStatus.OK.value(), "users", userService.advanceSearchCriteria(pageNo,pageSize,sortBy,address,search));
    }

    @GetMapping("/")
    public ResponseData<?> getUser() {
        return  new ResponseData<>(HttpStatus.OK.value(),"getlist",List.of(new UserRequestDTO("Tay", "Java", "admin@tayjava.vn", "0123456789"),new UserRequestDTO("Tay", "Java", "admin@tayjava.vn", "0123456789")) );
    }

}
