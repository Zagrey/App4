package com.example.model;

import com.common.util.Point;
import com.example.service.PointService;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@AllArgsConstructor
@EqualsAndHashCode
public class PointTask extends Task {
    private Point point;
    private String action = "create";
    private PointService pointService;

    @Override
    public String call() throws Exception {

//        logger.info("Point Task started");

        if ("create".equals(action)){
            Point savedPoint = pointService.create(point);
        } else if ("update".equals(action)){
            Point savedPoint = pointService.update(point);
        } else if ("delete".equals(action)){
            pointService.delete(point.getId());
        } else {
            logger.info("Unknown task type");
        }

//        logger.info("Point Task finished");

        return null;
    }
}
