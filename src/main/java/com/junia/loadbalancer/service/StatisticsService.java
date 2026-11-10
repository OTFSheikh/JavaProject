package com.junia.loadbalancer.service;

import com.junia.loadbalancer.model.Request;
import com.junia.loadbalancer.model.RequestType;
import com.junia.loadbalancer.model.Server;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class StatisticsService {

    // ecart max en points de % pour dire que c'est equilibre
    private static final double BALANCE_THRESHOLD = 20.0;

    public double averageLoadPercentage(List<Server> servers) {
        return servers.stream()
                .filter(Server::isUp)
                .mapToDouble(Server::getLoadPercentage)
                .average()
                .orElse(0);
    }

    public Optional<Server> mostLoadedServer(List<Server> servers) {
        return servers.stream()
                .filter(Server::isUp)
                .max(Comparator.comparingDouble(Server::getLoadPercentage));
    }

    public boolean isBalanced(List<Server> servers) {
        List<Double> loads = servers.stream()
                .filter(Server::isUp)
                .map(Server::getLoadPercentage)
                .toList();

        if (loads.size() < 2) {
            return true;
        }
        double max = loads.stream().mapToDouble(Double::doubleValue).max().orElse(0);
        double min = loads.stream().mapToDouble(Double::doubleValue).min().orElse(0);
        return max - min <= BALANCE_THRESHOLD;
    }

    public Map<RequestType, Long> countRequestsByType(List<Server> servers) {
        return servers.stream()
                .flatMap(server -> server.getRequests().stream())
                .collect(Collectors.groupingBy(Request::getType, Collectors.counting()));
    }
}
