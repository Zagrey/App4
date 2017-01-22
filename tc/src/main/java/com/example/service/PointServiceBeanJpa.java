package com.example.service;

import com.common.util.Point;
import com.example.repository.PointRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;

@Service
@Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
public class PointServiceBeanJpa implements PointService {
    @Autowired
    private PointRepository pointRepository;

    @Cacheable(
            value = "points", key = "100"
            )
    @Override
    public Collection<Point> findAll() {

        return pointRepository.findAll();
    }

    @Override
    public Collection<Point> findAll(Point point) {

        return pointRepository.findAll();
    }

    @Cacheable(
            value = "points",
            key = "#id")
    @Override
    public Point findOne(Long id) {

        Point point = pointRepository.findOne(id);

        return point;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED, readOnly = false)
    @CachePut(
            value = "points",
            key = "#result.id")
    @CacheEvict(
            value = "points",
            key = "100")
    public Point create(Point point) {

        // Ensure the entity object to be created does NOT exist in the
        // repository. Prevent the default behavior of save() which will update
        // an existing entity if the entity matching the supplied id exists.
        if (point.getId() != null) {
            // Cannot create Point with specified ID value
            return null;
        }

        return pointRepository.save(point);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED, readOnly = false)
    @CachePut(
            value = "points",
            key = "#point.id")
    public Point update(Point point) {

        // Ensure the entity object to be updated exists in the repository to
        // prevent the default behavior of save() which will persist a new
        // entity if the entity matching the id does not exist
        Point pointToUpdate = findOne(point.getId());
        if (pointToUpdate == null) {
            // Cannot update Point that hasn't been persisted
            return null;
        }

        pointToUpdate.setName(point.getName());
        Point updatedPoint = pointRepository.save(pointToUpdate);

        return updatedPoint;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED, readOnly = false)
    @CacheEvict(
            value = "points",
            key = "#id")
    public void delete(Long id) {

        pointRepository.delete(id);

    }

}