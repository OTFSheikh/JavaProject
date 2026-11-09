package com.junia.loadbalancer.model;

import java.time.LocalDateTime;

public class FileUploadRequest extends Request {

    private int fileSizeMb;

    public FileUploadRequest(String id, LocalDateTime arrivalTime, int fileSizeMb) {
        super(id, arrivalTime);
        if (fileSizeMb <= 0) {
            throw new IllegalArgumentException("La taille du fichier doit etre superieure a 0.");
        }
        this.fileSizeMb = fileSizeMb;
    }

    // 2 de base + 1 tous les 10 Mo
    @Override
    public int getLoad() {
        return 2 + fileSizeMb / 10;
    }

    @Override
    public String getType() {
        return "UPLOAD";
    }

    public int getFileSizeMb() {
        return fileSizeMb;
    }
}
