package com.example.model;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;

@Data
@AllArgsConstructor
public class GreetingRequestObject implements Serializable{
    String action;
    Greeting greeting;
}
