package com.example.model;

import com.common.util.Greeting;
import com.common.util.Point;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Data;

@Data
@JsonTypeInfo(
        use = JsonTypeInfo.Id.CLASS,
        include = JsonTypeInfo.As.PROPERTY,
        property = "class")
@JsonSubTypes({
        @JsonSubTypes.Type(value = Point.class, name = "point"),
        @JsonSubTypes.Type(value = Greeting.class, name = "greeting")})
public class RequestObject {
    private String action = "create";
}
