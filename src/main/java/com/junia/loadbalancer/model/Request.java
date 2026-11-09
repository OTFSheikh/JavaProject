package com.junia.loadbalancer.model;

import java.time.LocalDateTime;

public abstract class Request {

    private String id;
    private LocalDateTime arrivalTime;

    public Request(String id, LocalDateTime arrivalTime) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Request id cannot be empty.");
        }
        if (arrivalTime == null) {
            throw new IllegalArgumentException("Arrival time is required.");
        }

        this.id = id;
        this.arrivalTime = arrivalTime;
    }

    // chaque type de requete calcule sa propre charge
    public abstract int getLoad();

    public abstract String getType();

    public String getId() {
        return id;
    }

    public LocalDateTime getArrivalTime() {
        return arrivalTime;
    }

    @Override
    public String toString() {
        return id + " (" + getType() + ", load " + getLoad() + ")";
    }
}
