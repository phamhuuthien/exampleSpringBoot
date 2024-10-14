package com.example.learn.service.impl;

import com.example.learn.dto.request.AddressDTO;
import com.example.learn.dto.request.UserRequestDTO;
import com.example.learn.dto.response.PageResponse;
import com.example.learn.dto.response.UserDetailResponse;
import com.example.learn.exception.ResourceNotFoundException;
import com.example.learn.model.Address;
import com.example.learn.model.User;
import com.example.learn.repository.SearchRepository;
import com.example.learn.repository.UserRepository;
import com.example.learn.service.UserService;
import com.example.learn.util.UserStatus;
import com.example.learn.util.UserType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final SearchRepository searchRepository;
    @Override
    public User findByUsernameAndPassword(String username, String password) {
        return userRepository.findByUsername(username);
    }
    @Override
    public long saveUser(UserRequestDTO request) {
        User user = User.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .dateOfBirth(request.getDateOfBirth())
                .gender(request.getGender())
                .phone(request.getPhone())
                .email(request.getEmail())
                .username(request.getUsername())
                .password(request.getPassword())
                .status(request.getStatus())
                .type(UserType.valueOf(request.getType().toUpperCase()))
                .build();
        request.getAddresses().forEach(a ->
                user.saveAddress(Address.builder()
                        .apartmentNumber(a.getApartmentNumber())
                        .floor(a.getFloor())
                        .building(a.getBuilding())
                        .streetNumber(a.getStreetNumber())
                        .street(a.getStreet())
                        .city(a.getCity())
                        .country(a.getCountry())
                        .addressType(a.getAddressType())
                        .build()));

        userRepository.save(user);
        log.info("add user successfully");
        return user.getId();
    }

    @Override
    public void updateUser(long userId, UserRequestDTO request) {
//      nếu nó lỗi nó throw ra cái notfound bắt bằng try catch ở controller
//      lớp StringUtils support check string
        User user = getUserById(userId);

//        if(StringUtils.hasLength(request.getFirstName())){
//            user.setFirstName(request.getFirstName());
//        }
        if(StringUtils.hasLength(request.getPhone())){
//            check duplicate phone
//            gửi sms xác thực
        }

        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setDateOfBirth(request.getDateOfBirth());
        user.setGender(request.getGender());
        user.setPhone(request.getPhone());
        if (!request.getEmail().equals(user.getEmail())) {
            // check email from database if not exist then allow update email otherwise throw exception
            user.setEmail(request.getEmail());
        }
        user.setUsername(request.getUsername());
        user.setPassword(request.getPassword());
        user.setStatus(request.getStatus());
        user.setType(UserType.valueOf(request.getType().toUpperCase()));
        user.setAddresses(convertToAddress(request.getAddresses()));
        userRepository.save(user);

        log.info("User updated successfully");
    }

    @Override
    public void changeStatus(long userId, UserStatus status) {
        User user = getUserById(userId);
        user.setStatus(status);
        userRepository.save(user);

        log.info("status changed");
    }

    @Override
    public void deleteUser(long userId) {
        userRepository.deleteById(userId);

        log.info("user deleted = {}",userId);
    }

    @Override
    public UserDetailResponse getUser(long userId) {
        User user = getUserById(userId);
        return UserDetailResponse.builder()
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .phone(user.getPhone())
                .email(user.getEmail())
                .build();
    }

//    sort theo t tiêu chí nào đó
    @Override
    public PageResponse<?> getAllUserWithSortBy(int pageNo, int pageSize, String sortBy) {
        int p = 0;
        if(pageNo>0){
            p = pageNo-1;
        }

        List<Sort.Order> sorts = new ArrayList<>();
//        check có truyền param sortBy không
        if(StringUtils.hasLength(sortBy)){

//            firstName:desc -> 3 group : chữ : tất cả
            Pattern pattern = Pattern.compile("(\\w+?)(:)(.*)");
            Matcher matcher = pattern.matcher(sortBy);
            if(matcher.find()){
                if(matcher.group(3).equalsIgnoreCase("asc")){
                    sorts.add(new Sort.Order(Sort.Direction.ASC,matcher.group(1)));
                }else{
                    sorts.add(new Sort.Order(Sort.Direction.DESC,matcher.group(1)));
                }
            }
        }
//        Pageable pageable = PageRequest.of(p, pageSize, Sort.by(Sort.Direction.DESC,sortBy));

        Pageable pageable = PageRequest.of(p, pageSize, Sort.by(sorts));

        Page<User> users = userRepository.findAll(pageable);

        List<UserDetailResponse> response = users.stream().map(user -> UserDetailResponse.builder()
                .id(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .build()).toList();
        return PageResponse.builder()
                .pageNo(pageNo)
                .pageSize(pageSize)
                .totalPages(users.getTotalPages())
                .items(response)
                .build();
    }

//   sort theo 1 mảng các tiêu chí
//    firstName:DESC, lastName:ASC
    @Override
    public PageResponse<?> getAllUserWithSortByMutipleColumn(int pageNo, int pageSize, String... sorts) {
        int p =0;
        if(pageNo>0){
            p = pageNo-1;
        }

        List<Sort.Order> orders = new ArrayList<>();
        for(String sortBy : sorts){
            Pattern pattern = Pattern.compile("(\\w+?)(:)(.*)");
            Matcher matcher = pattern.matcher(sortBy);
            if(matcher.find()){
                if(matcher.group(3).equalsIgnoreCase("asc")){
                    orders.add(new Sort.Order(Sort.Direction.ASC,matcher.group(1)));
                }else{
                    orders.add(new Sort.Order(Sort.Direction.DESC,matcher.group(1)));
                }
            }
        }

        Pageable pageable = PageRequest.of(p, pageSize, Sort.by(orders));

        Page<User> users = userRepository.findAll(pageable);

        List<UserDetailResponse> response = users.stream().map(user -> UserDetailResponse.builder()
                .id(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .build()).toList();
        return PageResponse.builder()
                .pageNo(pageNo)
                .pageSize(pageSize)
                .totalPages(users.getTotalPages())
                .items(response)
                .build();
    }

    @Override
    public PageResponse<?> getAllUserWithSortByAndSearch(int pageNo, int pageSize, String search, String sortBy) {

        return searchRepository.getAllUserWithSortByAndSearch( pageNo, pageSize, search, sortBy);
    }

    @Override
    public PageResponse<?> advanceSearchCriteria(int pageNo, int pageSize, String sortBy,String address, String... search) {
        return searchRepository.advanceSearchCriteria(pageNo, pageSize, sortBy, address, search);
    }


    private Set<Address> convertToAddress(Set<AddressDTO> addresses) {
        Set<Address> result = new HashSet<>();
        addresses.forEach(a ->
                result.add(Address.builder()
                        .apartmentNumber(a.getApartmentNumber())
                        .floor(a.getFloor())
                        .building(a.getBuilding())
                        .streetNumber(a.getStreetNumber())
                        .street(a.getStreet())
                        .city(a.getCity())
                        .country(a.getCountry())
                        .addressType(a.getAddressType())
                        .build())
        );
        return result;
    }

//  do dùng nhiều chỗ nên tách nó ra thanhf 1 hàm riêng
    private User getUserById(long userId){
        return userRepository.findById(userId).orElseThrow(()-> new ResourceNotFoundException("User not found"));
    }
}
