package com.junia.loadbalancer.model;

import java.time.LocalDateTime;

public abstract class Request {

    private final String id;
    private final LocalDateTime arrivalTime;

    public Request(String id, LocalDateTime arrivalTime) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("L id de la requete est vide.");
        }
        if (arrivalTime == null) {
            throw new IllegalArgumentException("L heure d arrivee est obligatoire.");
        }

        this.id = id;
        this.arrivalTime = arrivalTime;
    }

    // chaque type de requete calcule sa propre charge
    public abstract int getLoad();

    public abstract RequestType getType();

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
