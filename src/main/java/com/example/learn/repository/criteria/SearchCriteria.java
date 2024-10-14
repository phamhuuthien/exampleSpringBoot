package com.example.learn.repository.criteria;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SearchCriteria {
//    fisrtname : thien
    private String key;
    private String operator; // > < :
    private Object value; // date string int
}
