package com.example.loginapp.model;

public class User {
    private String account;
    private String password; // For checking, not typically exposed directly
    private String name;
    private String email;
    private int age; // Example additional field

    public User() {
    }

    public User(String account, String password, String name, String email, int age) {
        this.account = account;
        this.password = password;
        this.name = name;
        this.email = email;
        this.age = age;
    }

    // Getters and Setters
    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }
}