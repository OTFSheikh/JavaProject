package com.junia.loadbalancer;

import com.junia.loadbalancer.loader.RequestCsvParser;
import com.junia.loadbalancer.loader.ServerCsvParser;
import com.junia.loadbalancer.model.Request;
import com.junia.loadbalancer.model.Server;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

// point d'entree du programme
public class Application {

    private static final Path SERVERS_FILE = Path.of("src/main/resources/servers.csv");
    private static final Path REQUESTS_FILE = Path.of("src/main/resources/requests.csv");

    public static void main(String[] args) {
        try {
            List<Server> servers = new ServerCsvParser().parse(SERVERS_FILE);
            List<Request> requests = new RequestCsvParser().parse(REQUESTS_FILE);

            System.out.println("Serveurs charges : " + servers.size());
            servers.forEach(System.out::println);

            System.out.println();
            System.out.println("Requetes chargees : " + requests.size());
            requests.forEach(System.out::println);
        } catch (IOException e) {
            System.out.println("Impossible de lire les fichiers de donnees : " + e.getMessage());
        }
    }
}
