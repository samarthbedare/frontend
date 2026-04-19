package com.inventory.model;

public class Product {
    private Long productId;
    private String productName;
    private String brand;
    private String colour;
    private String size;
    private Double unitPrice;
    private Double rating;

    public Product() {}

    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }
    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }
    public String getBrand() { return brand; }
    public void setBrand(String brand) { this.brand = brand; }
    public String getColour() { return colour; }
    public void setColour(String colour) { this.colour = colour; }
    public String getSize() { return size; }
    public void setSize(String size) { this.size = size; }
    public Double getUnitPrice() { return unitPrice; }
    public void setUnitPrice(Double unitPrice) { this.unitPrice = unitPrice; }
    public Double getRating() { return rating; }
    public void setRating(Double rating) { this.rating = rating; }
}
