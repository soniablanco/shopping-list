package com.piscos.soni.shoppinglist;

public class Product {
    public String name;
    public String code;
    public String alexaHint;

    public Product(){}

    public Product (String name, String code, String alexaHint){
        this.name = name;
        this.code = code;
        this.alexaHint = alexaHint;
    }
}
