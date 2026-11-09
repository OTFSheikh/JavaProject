package com.junia.loadbalancer.loader;

import com.junia.loadbalancer.exception.InvalidDataException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public abstract class AbstractCsvParser<T> {

    public List<T> parse(Path path) throws IOException {
        List<String> lines = Files.readAllLines(path);
        List<T> result = new ArrayList<>();

        // on commence a 1 pour sauter l'en-tete
        for (int i = 1; i < lines.size(); i++) {
            String line = lines.get(i);
            if (line.isBlank()) {
                continue;
            }

            try {
                result.add(parseLine(line));
            } catch (InvalidDataException e) {
                // une ligne fausse ne doit pas bloquer tout le chargement
                System.out.println("Ligne " + (i + 1) + " ignoree (" + path.getFileName() + ") : " + e.getMessage());
            }
        }
        return result;
    }

    protected abstract T parseLine(String line) throws InvalidDataException;
}
