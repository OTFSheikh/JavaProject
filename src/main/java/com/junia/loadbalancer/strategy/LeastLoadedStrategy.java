package com.junia.loadbalancer.strategy;

import com.junia.loadbalancer.model.Request;
import com.junia.loadbalancer.model.Server;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public class LeastLoadedStrategy implements BalancingStrategy {

    @Override
    public Optional<Server> chooseServer(List<Server> servers, Request request) {
        return servers.stream()
                .filter(server -> server.canAccept(request))
                .min(Comparator.comparingDouble(Server::getLoadPercentage));
    }

    @Override
    public String getName() {
        return "Least Loaded";
    }
}
