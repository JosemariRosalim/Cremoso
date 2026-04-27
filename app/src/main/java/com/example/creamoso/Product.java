package com.example.creamoso;

import java.util.ArrayList;
import java.util.List;

public class Product {
    private String id;
    private String name;
    private String category;
    private double price;
    private String imageUrl;
    private List<AddOn> addOns;
    private int stockCount;
    private boolean isAvailable;

    public Product() {
        // Default constructor required for calls to DataSnapshot.getValue(Product.class)
        this.addOns = new ArrayList<>();
        this.isAvailable = true; // Default to available
        this.stockCount = 0;
    }

    public Product(String id, String name, String category, double price, String imageUrl) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.price = price;
        this.imageUrl = imageUrl;
        this.addOns = new ArrayList<>();
        this.isAvailable = true;
        this.stockCount = 0;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public List<AddOn> getAddOns() { return addOns; }
    public void setAddOns(List<AddOn> addOns) { this.addOns = addOns; }

    public int getStockCount() { return stockCount; }
    public void setStockCount(int stockCount) { this.stockCount = stockCount; }

    public boolean isAvailable() { return isAvailable; }
    public void setAvailable(boolean available) { isAvailable = available; }

    public static class AddOn {
        private String name;
        private double price;
        private boolean isAvailable;

        public AddOn() {
            this.isAvailable = true;
        }

        public AddOn(String name, double price) {
            this.name = name;
            this.price = price;
            this.isAvailable = true;
        }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public double getPrice() { return price; }
        public void setPrice(double price) { this.price = price; }

        public boolean isAvailable() { return isAvailable; }
        public void setAvailable(boolean available) { isAvailable = available; }
    }
}