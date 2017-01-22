package com.example.service;

import com.common.util.Point;

import java.util.Collection;


public interface PointService {

    Collection<Point> findAll();
    Collection<Point> findAll(Point point);

    Point findOne(Long id);

    Point create(Point point);

    Point update(Point point);

    void delete(Long id);

}