package com.example.learn.repository;

import com.example.learn.dto.response.PageResponse;
import com.example.learn.model.Address;
import com.example.learn.model.User;
import com.example.learn.repository.criteria.SearchCriteria;
import com.example.learn.repository.criteria.UserSearchCriteriaQueryConsumer;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import jakarta.persistence.criteria.*;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Repository
public class SearchRepository {
//    cho phép lamf việc với jpa hibernate
    @PersistenceContext
    private EntityManager entityManager;
    public PageResponse<?> getAllUserWithSortByAndSearch(int pageNo, int pageSize,String search, String sortBy){
//      String buider, string buffer
//      ghép chuỗi nên sử dụng string builder cho phe bất đồng bộ
//      query list user
        StringBuilder sqlQuery = new StringBuilder("select new com.example.learn.dto.response.UserDetailResponse(u.id, u.firstName, u.lastName, u.email, u.phone) from User u where 1 = 1");

//      không ghép chuỗi bằng phương pháp thủ công do sẽ bị sql injection

//      kiểm tra nó có giá trị không
//      ghép chuỗi
        if(StringUtils.hasLength(search)){
//          firstname là các field của entity chứ k phải các trường của tableB
            sqlQuery.append(" and lower(u.firstName) like lower(:firstName)");
            sqlQuery.append(" or lower(u.lastName) like lower(:lastName)");
            sqlQuery.append(" or lower(u.email) like lower(:email)");
        }

        if(StringUtils.hasLength(sortBy)){
            Pattern pattern = Pattern.compile("(\\w+?)(:)(.*)");
            Matcher matcher = pattern.matcher(sortBy);
            if(matcher.find()){
                sqlQuery.append(String.format(" order by u.%s %s",matcher.group(1),matcher.group(3)));
            }
        }


//      tạo query với chuỗi đã được nối ở trên
        Query selectQuery = entityManager.createQuery(sqlQuery.toString());
//      phân trang dữ lệu
//      ofset trong sql
        selectQuery.setFirstResult(pageNo);
        selectQuery.setMaxResults(pageSize);
//      set tham số cho chuỗi trên mình đã ghép
        if(StringUtils.hasLength(search)){
//            selectQuery.setParameter("firstName","%" + search + "%");
            selectQuery.setParameter("firstName",String.format("%%%s%%", search));
            selectQuery.setParameter("lastName",String.format("%%%s%%", search));
            selectQuery.setParameter("email",String.format("%%%s%%", search));
        }
//      danh sách user
        List users = selectQuery.getResultList();

//      đếm các record
        StringBuilder sqlCountQuery = new StringBuilder("select count(*) from User u where 1 = 1");
        if(StringUtils.hasLength(search)){
//          đặt tên biến theo số thứ tự
            sqlCountQuery.append(" and lower(u.firstName) like lower(?1)");
            sqlCountQuery.append(" or lower(u.lastName) like lower(?2)");
            sqlCountQuery.append(" or lower(u.email) like lower(?3)");
        }
        Query selectCountQuery = entityManager.createQuery(sqlCountQuery.toString());

        if(StringUtils.hasLength(search)){
            selectCountQuery.setParameter(1,String.format("%%%s%%", search));
            selectCountQuery.setParameter(2,String.format("%%%s%%", search));
            selectCountQuery.setParameter(3,String.format("%%%s%%", search));
        }
        Long totalCount = (Long) selectCountQuery.getSingleResult();

        Page<?> page = new PageImpl<Object>(users, PageRequest.of(pageNo,pageSize),totalCount);

        return PageResponse.builder()
                .pageNo(pageNo)
                .pageSize(pageSize)
                .totalPages(page.getTotalPages())
                .items(page.stream().toList())
                .build();
    }

    public PageResponse<?> advanceSearchCriteria(int pageNo, int pageSize,String sortBY,String address, String... search) {


//      tách ra đưa vào SearchCriteria
        List<SearchCriteria> listCriteria = new ArrayList<>();
        if(search != null){
            for(String s : search){
                Pattern pattern = Pattern.compile("(\\w+?)(:|<|>)(.*)");
                Matcher matcher = pattern.matcher(s);
                if(matcher.find()){
                    listCriteria.add(new SearchCriteria(matcher.group(1), matcher.group(2), matcher.group(3)));
                }
            }
        }

        Long totalElement = getTotalElement(listCriteria,address);

        List<User> listUser = getUsers(pageNo,pageSize,listCriteria,address,sortBY);
        return PageResponse.builder()
                .pageNo(pageNo)
                .pageSize(pageSize)
                .totalPages(totalElement.intValue())
                .items(listUser)
                .build();
    }

    private List<User> getUsers(int pageNo, int pageSize, List<SearchCriteria> listCriteria,String address,String sortBy){
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<User> query = criteriaBuilder.createQuery(User.class);
        Root<User> root = query.from(User.class);

        // xử lý các điều kiên
        Predicate predicate = criteriaBuilder.conjunction();

        UserSearchCriteriaQueryConsumer queryConsumer = new UserSearchCriteriaQueryConsumer(criteriaBuilder,predicate,root);

        if(StringUtils.hasLength(address)){
            Join<Address,User> addressUserJoin = root.join("addresses");
            Predicate addressPredicate = criteriaBuilder.like(addressUserJoin.get("city"),"%"+address+"%");
            query.where(addressPredicate);
        }else{
            listCriteria.forEach(queryConsumer);
            predicate = queryConsumer.getPredicate();
//      tìm kiếm
            query.where(predicate);
        }

//      sort
        if(StringUtils.hasLength(sortBy)){
            Pattern pattern = Pattern.compile("(\\w+?)(:)(desc|asc)");
            Matcher matcher = pattern.matcher(sortBy);
            if(matcher.find()){
                String columnName = matcher.group(1);
                if(matcher.group(3).equalsIgnoreCase("desc")){
                    query.orderBy(criteriaBuilder.desc(root.get(columnName)));
                }else{
                    query.orderBy(criteriaBuilder.asc(root.get(columnName)));
                }
            }
        }

        return entityManager.createQuery(query).setFirstResult(pageNo).setMaxResults(pageSize).getResultList();
    }

    private Long getTotalElement(List<SearchCriteria> listCriteria, String address) {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> query = criteriaBuilder.createQuery(Long.class);
        Root<User> root = query.from(User.class);


        Predicate predicate = criteriaBuilder.conjunction();

        UserSearchCriteriaQueryConsumer queryConsumer = new UserSearchCriteriaQueryConsumer(criteriaBuilder, predicate, root);

        if (StringUtils.hasLength(address)) {
            Join<Address, User> addressUserJoin = root.join("addresses");
            Predicate addressPredicate = criteriaBuilder.like(addressUserJoin.get("city"), "%" + address + "%");
            query.select(criteriaBuilder.count(root));
            query.where(addressPredicate);
        } else {
            listCriteria.forEach(queryConsumer);
            predicate = queryConsumer.getPredicate();
            query.select(criteriaBuilder.count(root));
            query.where(predicate);
        }

        return entityManager.createQuery(query).getSingleResult();
    }
}
