package com.common.util;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.sql.Timestamp;

@Data
@Entity(name = "points")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Point {
    @Id
    @GeneratedValue
    private Long id;
    private String name;
    private String type;
    private String description;
    private Double latitude;
    private Double longitude;
    @Column(columnDefinition="TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT now()")
    private Timestamp createdOn;
    @Column(columnDefinition="INTEGER DEFAULT 15")
    private Integer lifeTime;
}
