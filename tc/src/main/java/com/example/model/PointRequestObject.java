package com.example.model;

import com.common.util.Point;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;

@Data
@AllArgsConstructor
public class PointRequestObject extends RequestObject implements Serializable{
    Point point;
}
