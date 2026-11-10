package com.junia.loadbalancer;

import com.junia.loadbalancer.loader.RequestCsvParser;
import com.junia.loadbalancer.loader.ServerCsvParser;
import com.junia.loadbalancer.model.Request;
import com.junia.loadbalancer.model.Server;
import com.junia.loadbalancer.service.LoadBalancer;
import com.junia.loadbalancer.service.StatisticsService;
import com.junia.loadbalancer.strategy.LeastLoadedStrategy;

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

            LoadBalancer loadBalancer = new LoadBalancer(servers, new LeastLoadedStrategy());
            StatisticsService stats = new StatisticsService();

            loadBalancer.assignAll(requests);
            System.out.println("=== Repartition avec " + loadBalancer.getStrategy().getName() + " ===");
            printState(loadBalancer, stats);

            System.out.println();
            System.out.println("=== Panne de web-01 ===");
            List<Request> lost = loadBalancer.failServer("web-01");
            System.out.println("Requetes perdues pendant la panne : " + lost);
            printState(loadBalancer, stats);
        } catch (IOException e) {
            System.out.println("Impossible de lire les fichiers de donnees : " + e.getMessage());
        }
    }

    // affichage temporaire, il sera range dans une classe a part a l'etape 6
    private static void printState(LoadBalancer loadBalancer, StatisticsService stats) {
        for (Server server : loadBalancer.getServers()) {
            System.out.printf("%s (%.0f%%) -> %s%n", server, server.getLoadPercentage(), server.getRequests());
        }
        System.out.println("Requetes rejetees : " + loadBalancer.getRejectedRequests());
        System.out.printf("Charge moyenne : %.1f%%%n", stats.averageLoadPercentage(loadBalancer.getServers()));
        System.out.println("Equilibre : " + (stats.isBalanced(loadBalancer.getServers()) ? "oui" : "non"));
        System.out.println("Requetes par type : " + stats.countRequestsByType(loadBalancer.getServers()));
    }
}
