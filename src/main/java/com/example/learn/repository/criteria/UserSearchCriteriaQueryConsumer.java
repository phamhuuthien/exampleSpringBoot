package com.example.learn.repository.criteria;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.function.Consumer;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserSearchCriteriaQueryConsumer implements Consumer<SearchCriteria> {

    private CriteriaBuilder builder;
    private Predicate predicate;
    private Root root;

//    firstName:T
    @Override
    public void accept(SearchCriteria params) {
        if(params.getOperator().equals(">")){
            predicate = builder.and(predicate, builder.greaterThanOrEqualTo(root.get(params.getKey()),params.getValue().toString()));
        }else if(params.getValue().equals("<")){
            predicate = builder.and(predicate, builder.lessThanOrEqualTo(root.get(params.getKey()),params.getValue().toString()));
        }else{
            if(root.get(params.getKey()).getJavaType() == String.class){
                predicate = builder.and(predicate,builder.like(root.get(params.getKey()),"%"+ params.getValue().toString()+ "%"));
            }else{
                predicate = builder.and(predicate,builder.equal(root.get(params.getKey()),params.getValue().toString()));
            }
        }
    }
}
