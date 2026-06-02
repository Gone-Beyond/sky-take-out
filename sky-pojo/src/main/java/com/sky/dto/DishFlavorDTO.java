package com.sky.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class DishFlavorDTO implements Serializable {

    private Long id;

    //口味名称
    private String name;

    //口味值
    private String value;
}
