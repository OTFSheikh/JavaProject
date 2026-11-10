package com.junia.loadbalancer.service;

import com.junia.loadbalancer.exception.NoServerAvailableException;
import com.junia.loadbalancer.model.Request;
import com.junia.loadbalancer.model.Server;
import com.junia.loadbalancer.strategy.BalancingStrategy;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class LoadBalancer {

    private final List<Server> servers;
    private final BalancingStrategy strategy;
    private final List<Request> rejectedRequests;

    public LoadBalancer(List<Server> servers, BalancingStrategy strategy) {
        if (servers == null || strategy == null) {
            throw new IllegalArgumentException("Les serveurs et la strategie sont obligatoires.");
        }
        checkUniqueNames(servers);
        this.servers = new ArrayList<>(servers);
        this.strategy = strategy;
        this.rejectedRequests = new ArrayList<>();
    }

    public Server assign(Request request) throws NoServerAvailableException {
        Server server = strategy.chooseServer(servers, request)
                .orElseThrow(() -> new NoServerAvailableException(
                        "Aucun serveur disponible pour la requete " + request.getId()
                ));
        server.accept(request);
        return server;
    }

    public void assignAll(List<Request> requests) {
        List<Request> sorted = new ArrayList<>(requests);
        sorted.sort(Comparator.comparing(Request::getArrivalTime));

        for (Request request : sorted) {
            tryAssign(request);
        }
    }

    // le serveur tombe en panne et ses requetes partent sur les autres
    public List<Request> failServer(String name) {
        Server server = findServer(name)
                .orElseThrow(() -> new IllegalArgumentException("Serveur inconnu : " + name));

        List<Request> lostRequests = new ArrayList<>();
        for (Request request : server.goDown()) {
            if (!tryAssign(request)) {
                lostRequests.add(request);
            }
        }
        return lostRequests;
    }

    // le serveur revient : on retente les requetes rejetees
    public List<Request> recoverServer(String name) {
        Server server = findServer(name)
                .orElseThrow(() -> new IllegalArgumentException("Serveur inconnu : " + name));
        server.goUp();
        return retryRejected();
    }

    public List<Request> retryRejected() {
        List<Request> toRetry = new ArrayList<>(rejectedRequests);
        rejectedRequests.clear();

        List<Request> placed = new ArrayList<>();
        for (Request request : toRetry) {
            if (tryAssign(request)) {
                placed.add(request);
            }
        }
        return placed;
    }

    public Optional<Server> findServer(String name) {
        return servers.stream()
                .filter(server -> server.getName().equalsIgnoreCase(name))
                .findFirst();
    }

    // on retrouve les serveurs par leur nom, donc deux serveurs ne peuvent pas avoir le meme
    private void checkUniqueNames(List<Server> servers) {
        Set<String> names = new HashSet<>();
        for (Server server : servers) {
            if (!names.add(server.getName().toLowerCase())) {
                throw new IllegalArgumentException("Deux serveurs ont le meme nom : " + server.getName());
            }
        }
    }

    private boolean tryAssign(Request request) {
        try {
            assign(request);
            return true;
        } catch (NoServerAvailableException e) {
            rejectedRequests.add(request);
            return false;
        }
    }

    public List<Server> getServers() {
        return List.copyOf(servers);
    }

    public List<Request> getRejectedRequests() {
        return List.copyOf(rejectedRequests);
    }

    public BalancingStrategy getStrategy() {
        return strategy;
    }
}
