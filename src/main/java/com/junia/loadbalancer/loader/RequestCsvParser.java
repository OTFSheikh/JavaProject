package com.junia.loadbalancer.loader;

import com.junia.loadbalancer.exception.InvalidDataException;
import com.junia.loadbalancer.model.FileUploadRequest;
import com.junia.loadbalancer.model.HttpRequest;
import com.junia.loadbalancer.model.Request;
import com.junia.loadbalancer.model.VideoStreamRequest;

import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;

public class RequestCsvParser extends AbstractCsvParser<Request> {

    @Override
    protected Request parseLine(String line) throws InvalidDataException {
        // -1 pour garder la derniere colonne meme si elle est vide
        String[] columns = line.split(",", -1);
        if (columns.length != 4) {
            throw new InvalidDataException("4 colonnes attendues : " + line);
        }

        try {
            String id = columns[0].trim();
            String type = columns[1].trim().toUpperCase();
            LocalDateTime arrivalTime = LocalDateTime.parse(columns[2].trim());
            String parameter = columns[3].trim();

            return switch (type) {
                case "HTTP" -> new HttpRequest(id, arrivalTime);
                case "UPLOAD" -> new FileUploadRequest(id, arrivalTime, Integer.parseInt(parameter));
                case "STREAM" -> new VideoStreamRequest(id, arrivalTime, Integer.parseInt(parameter));
                default -> throw new InvalidDataException("type de requete inconnu : " + type);
            };
        } catch (DateTimeParseException e) {
            throw new InvalidDataException("date invalide : " + columns[2]);
        } catch (NumberFormatException e) {
            throw new InvalidDataException("parametre invalide : " + columns[3]);
        } catch (IllegalArgumentException e) {
            throw new InvalidDataException(e.getMessage());
        }
    }
}
