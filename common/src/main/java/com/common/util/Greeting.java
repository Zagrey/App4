package com.common.util;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Data
@Entity
public class Greeting {
    @Id
    @GeneratedValue
    private Long id;
    private String text;
}
