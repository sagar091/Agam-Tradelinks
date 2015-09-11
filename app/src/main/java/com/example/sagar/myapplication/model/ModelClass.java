package com.example.sagar.myapplication.model;

import com.google.gson.annotations.SerializedName;

/**
 * Created by sagartahelyani on 11-09-2015.
 */
public class ModelClass {

    @SerializedName("id")
    public String id;

    @SerializedName("image")
    public String image;

    @SerializedName("price")
    public String price;

    @SerializedName("stock")
    public String stock;

    @SerializedName("name")
    public String name;

}