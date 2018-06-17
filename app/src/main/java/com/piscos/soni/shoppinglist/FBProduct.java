package com.piscos.soni.shoppinglist;

public class FBProduct {
    public String name;
    public String code;
    public String alexaHint;
    public String photoUrl;
    public String thumbnailUrl;

    public FBProduct(){}

    public FBProduct(String name, String code, String alexaHint, String photoUrl, String thumbnailUrl){
        this.name = name;
        this.code = code;
        this.alexaHint = alexaHint;
        this.photoUrl = photoUrl;
        this.thumbnailUrl = thumbnailUrl;
    }
}
