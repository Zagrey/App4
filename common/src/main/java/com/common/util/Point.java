package com.common.util;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Data
@Entity
public class Point {
    @Id
    @GeneratedValue
    private Long id;
    private String name;
    private String type;
    private String description;
    private Long latitude;
    private Long longitude;
}
