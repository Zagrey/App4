package com.example.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import com.common.util.Greeting;

import java.io.Serializable;

@Data
@AllArgsConstructor
public class GreetingRequestObject extends RequestObject implements Serializable{
    Greeting greeting;
}
