/*
 * Copyright (c) 2019. Yaroslav Khazanov y@5505.ru
 */

package org.slhouse.yamt.entity;

import lombok.Getter;
import lombok.Setter;

import java.util.Random;

/**
 * @author Yaroslav V. Khazanov
 **/
@Getter
@Setter
public class Quote {
    private String name;
    private Double price;

    public Quote() {
    }

    public Quote(String name) {
        this(name, new Random().nextDouble()*100);
    }

    public Quote(String name, Double price) {
        this.name = name;
        this.price = price;
    }
}
