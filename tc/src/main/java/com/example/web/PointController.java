package com.example.web;


import com.common.util.Point;
import com.example.model.PointRequestObject;
import com.example.service.PointService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;

@Profile("web")
@RestController
public class PointController {
    @Autowired
    private PointService pointService;
    @Autowired
    RabbitTemplate rabbitTemplate;
    @Value("${rabbit.input.queue}")
    private String queueName;

    @Autowired
    AmqpTemplate template;


    ObjectMapper mapper = new ObjectMapper();

    private static final Logger log = LoggerFactory.getLogger(PointController.class);

    @RequestMapping(
            value = "/api/points",
            method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Collection<Point>> getPoints() {

        log.info("getPoint: start");
        Collection<Point> points = pointService.findAll();

//        for(int i = 0;i<50;i++)
//            template.convertAndSend(queueName,"Message " + i);

        log.info("getPoint: end");
        return new ResponseEntity<Collection<Point>>(points,
                HttpStatus.OK);
    }

    @RequestMapping(
            value = "/api/points/{id}",
            method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Point> getPoint(@PathVariable("id") Long id) {

        log.info("getPoint: start");
        Point point = pointService.findOne(id);
        if (point == null) {
            return new ResponseEntity<Point>(HttpStatus.NOT_FOUND);
        }
        log.info("getPoint: end");

        return new ResponseEntity<Point>(point, HttpStatus.OK);
    }

    @RequestMapping(
            value = "/api/points",
            method = RequestMethod.POST,
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Point> createPoint(
            @RequestBody Point point) {

        log.info("createPoint: before sent");
        PointRequestObject gro = new PointRequestObject(point);
        gro.setAction("create");

        try {
            rabbitTemplate.convertAndSend(queueName, mapper.writeValueAsString(gro));
        } catch (JsonProcessingException e) {
            throw new RuntimeException();
        }
        log.info("createPoint: after sent");
        return new ResponseEntity<Point>(point, HttpStatus.CREATED);
    }

    @RequestMapping(
            value = "/api/points/{id}",
            method = RequestMethod.PUT,
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Point> updatePoint(
            @RequestBody Point point) {

//        Point updatedPoint = pointService.update(point);
//        if (updatedPoint == null) {
//            return new ResponseEntity<Point>(
//                    HttpStatus.INTERNAL_SERVER_ERROR);
//        }

        log.info("updatePoint: before sent");
        PointRequestObject gro = new PointRequestObject(point);
        gro.setAction("update");

        try {
            rabbitTemplate.convertAndSend(queueName, mapper.writeValueAsString(gro));
        } catch (JsonProcessingException e) {
            throw new RuntimeException();
        }
        log.info("updatePoint: after sent");

        return new ResponseEntity<Point>(point, HttpStatus.OK);
    }

    @RequestMapping(
            value = "/api/points/{id}",
            method = RequestMethod.DELETE,
            consumes = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<Point> deletePoint(@PathVariable("id") Long id,
                                                   @RequestBody Point point) {

//        pointService.delete(id);

        point.setId(id);
        log.info("deletePoint: before sent");
        PointRequestObject gro = new PointRequestObject(point);
        gro.setAction("delete");

        try {
            rabbitTemplate.convertAndSend(queueName, mapper.writeValueAsString(gro));
        } catch (JsonProcessingException e) {
            throw new RuntimeException();
        }
        log.info("deletePoint: after sent");

        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
