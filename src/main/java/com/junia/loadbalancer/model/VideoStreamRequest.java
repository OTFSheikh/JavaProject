package com.junia.loadbalancer.model;

import java.time.LocalDateTime;

public class VideoStreamRequest extends Request {

    private int durationMinutes;

    public VideoStreamRequest(String id, LocalDateTime arrivalTime, int durationMinutes) {
        super(id, arrivalTime);
        if (durationMinutes <= 0) {
            throw new IllegalArgumentException("La duree doit etre superieure a 0.");
        }
        this.durationMinutes = durationMinutes;
    }

    // 5 de base + 1 tous les quarts d'heure
    @Override
    public int getLoad() {
        return 5 + durationMinutes / 15;
    }

    @Override
    public String getType() {
        return "STREAM";
    }

    public int getDurationMinutes() {
        return durationMinutes;
    }
}
