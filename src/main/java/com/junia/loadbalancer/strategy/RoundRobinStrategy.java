package com.junia.loadbalancer.strategy;

import com.junia.loadbalancer.model.Request;
import com.junia.loadbalancer.model.Server;

import java.util.List;
import java.util.Optional;

public class RoundRobinStrategy implements BalancingStrategy {

    private int nextIndex = 0;

    @Override
    public Optional<Server> chooseServer(List<Server> servers, Request request) {
        for (int i = 0; i < servers.size(); i++) {
            int index = (nextIndex + i) % servers.size();
            Server server = servers.get(index);

            if (server.canAccept(request)) {
                nextIndex = index + 1;
                return Optional.of(server);
            }
        }
        return Optional.empty();
    }

    @Override
    public String getName() {
        return "Round Robin";
    }
}
