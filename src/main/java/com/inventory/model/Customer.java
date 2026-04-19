package com.inventory.model;

public class Customer {
    private Integer customerId;
    private String fullName;
    private String emailAddress;

    public Customer() {
    }

    public Customer(Integer customerId, String fullName, String emailAddress) {
        this.customerId = customerId;
        this.fullName = fullName;
        this.emailAddress = emailAddress;
    }

    public Integer getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Integer customerId) {
        this.customerId = customerId;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmailAddress() {
        return emailAddress;
    }

    public void setEmailAddress(String emailAddress) {
        this.emailAddress = emailAddress;
    }
}
