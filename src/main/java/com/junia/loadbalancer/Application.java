package com.junia.loadbalancer;

import com.junia.loadbalancer.loader.RequestCsvParser;
import com.junia.loadbalancer.loader.ServerCsvParser;
import com.junia.loadbalancer.model.Request;
import com.junia.loadbalancer.model.Server;
import com.junia.loadbalancer.service.LoadBalancer;
import com.junia.loadbalancer.service.StatisticsService;
import com.junia.loadbalancer.strategy.BalancingStrategy;
import com.junia.loadbalancer.ui.ConsoleMenu;
import com.junia.loadbalancer.ui.ConsoleReport;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Scanner;

// point d'entree du programme
public class Application {

    private static final Path SERVERS_FILE = Path.of("src/main/resources/servers.csv");
    private static final Path REQUESTS_FILE = Path.of("src/main/resources/requests.csv");

    public static void main(String[] args) {
        List<Server> servers;
        List<Request> requests;
        try {
            servers = new ServerCsvParser().parse(SERVERS_FILE);
            requests = new RequestCsvParser().parse(REQUESTS_FILE);
        } catch (IOException e) {
            System.out.println("Impossible de lire les fichiers de donnees : " + e.getMessage());
            return;
        }

        Scanner scanner = new Scanner(System.in);
        ConsoleMenu menu = new ConsoleMenu(scanner, new ConsoleReport(new StatisticsService()));

        BalancingStrategy strategy = menu.chooseStrategy();
        LoadBalancer loadBalancer;
        try {
            loadBalancer = new LoadBalancer(servers, strategy);
            loadBalancer.assignAll(requests);
        } catch (IllegalArgumentException e) {
            System.out.println("Donnees incoherentes : " + e.getMessage());
            return;
        }

        menu.run(loadBalancer);
    }
}
