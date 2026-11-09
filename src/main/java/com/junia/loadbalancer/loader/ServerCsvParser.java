package com.junia.loadbalancer.loader;

import com.junia.loadbalancer.exception.InvalidDataException;
import com.junia.loadbalancer.model.Server;
import com.junia.loadbalancer.model.ServerStatus;

public class ServerCsvParser extends AbstractCsvParser<Server> {

    @Override
    protected Server parseLine(String line) throws InvalidDataException {
        String[] columns = line.split(",");
        if (columns.length != 4) {
            throw new InvalidDataException("4 colonnes attendues : " + line);
        }

        String name = columns[0].trim();
        int capacity = parseNumber(columns[1]);
        int weight = parseNumber(columns[2]);
        ServerStatus status = parseStatus(columns[3]);

        try {
            return new Server(name, capacity, weight, status);
        } catch (IllegalArgumentException e) {
            throw new InvalidDataException(e.getMessage());
        }
    }

    private int parseNumber(String value) throws InvalidDataException {
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            throw new InvalidDataException("nombre invalide : " + value);
        }
    }

    private ServerStatus parseStatus(String value) throws InvalidDataException {
        try {
            return ServerStatus.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new InvalidDataException("statut invalide (UP ou DOWN attendu) : " + value);
        }
    }
}
