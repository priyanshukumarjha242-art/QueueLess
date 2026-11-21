package com.queueless.entity;

public enum TokenPriority {
    VIP(3),
    PRIORITY(2),
    REGULAR(1);

    private final int weight;

    TokenPriority(int weight) {
        this.weight = weight;
    }

    public int getWeight() {
        return weight;
    }
}
