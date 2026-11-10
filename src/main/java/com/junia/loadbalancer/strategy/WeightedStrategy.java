package com.junia.loadbalancer.strategy;

import com.junia.loadbalancer.model.Request;
import com.junia.loadbalancer.model.Server;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public class WeightedStrategy implements BalancingStrategy {

    @Override
    public Optional<Server> chooseServer(List<Server> servers, Request request) {
        // charge divisee par le poids : un serveur de poids 3 doit recevoir 3 fois plus
        return servers.stream()
                .filter(server -> server.canAccept(request))
                .min(Comparator.comparingDouble(server -> (double) server.getCurrentLoad() / server.getWeight()));
    }

    @Override
    public String getName() {
        return "Weighted";
    }
}
