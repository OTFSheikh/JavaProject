package com.junia.loadbalancer.ui;

import com.junia.loadbalancer.model.Request;
import com.junia.loadbalancer.model.Server;
import com.junia.loadbalancer.service.StatisticsService;

import java.util.List;

public class ConsoleReport {

    private StatisticsService statistics;

    public ConsoleReport(StatisticsService statistics) {
        this.statistics = statistics;
    }

    public void printServers(List<Server> servers) {
        System.out.println();
        System.out.printf("%-12s %-6s %-8s %-6s %s%n", "SERVEUR", "ETAT", "CHARGE", "%", "REQUETES");
        for (Server server : servers) {
            System.out.printf("%-12s %-6s %-8s %-6s %s%n",
                    server.getName(),
                    server.getStatus(),
                    server.getCurrentLoad() + "/" + server.getCapacity(),
                    String.format("%.0f%%", server.getLoadPercentage()),
                    requestIds(server.getRequests()));
        }
    }

    public void printRejected(List<Request> rejected) {
        System.out.println();
        if (rejected.isEmpty()) {
            System.out.println("Aucune requete rejetee.");
        } else {
            System.out.println("Requetes rejetees (" + rejected.size() + ") : " + rejected);
        }
    }

    public void printStatistics(List<Server> servers) {
        System.out.println();
        System.out.println("--- Statistiques ---");
        System.out.printf("Charge moyenne (serveurs UP) : %.1f%%%n", statistics.averageLoadPercentage(servers));
        statistics.mostLoadedServer(servers).ifPresent(server ->
                System.out.printf("Serveur le plus charge : %s (%.0f%%)%n", server.getName(), server.getLoadPercentage()));
        System.out.println("Repartition equilibree : " + (statistics.isBalanced(servers) ? "oui" : "non"));
        System.out.println("Requetes traitees par type : " + statistics.countRequestsByType(servers));
    }

    public void printMessage(String message) {
        System.out.println(message);
    }

    private String requestIds(List<Request> requests) {
        return requests.stream()
                .map(Request::getId)
                .toList()
                .toString();
    }
}
