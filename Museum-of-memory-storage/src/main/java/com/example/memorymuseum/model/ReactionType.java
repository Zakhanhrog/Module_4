package com.example.memorymuseum.model;

public enum ReactionType {
    LIKE("fas fa-thumbs-up"),
    LOVE("fas fa-heart"),
    HAHA("fas fa-laugh-squint"),
    WOW("fas fa-surprise"),
    SAD("fas fa-sad-tear"),
    ANGRY("fas fa-angry"),
    SYMPATHY("fas fa-hand-holding-heart");

    private final String iconClass;

    ReactionType(String iconClass) {
        this.iconClass = iconClass;
    }

    public String getIconClass() {
        return iconClass;
    }
}