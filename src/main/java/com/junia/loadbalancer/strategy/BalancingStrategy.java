package com.junia.loadbalancer.strategy;

import com.junia.loadbalancer.model.Request;
import com.junia.loadbalancer.model.Server;

import java.util.List;
import java.util.Optional;

public interface BalancingStrategy {

    // renvoie un Optional vide si aucun serveur ne peut prendre la requete
    Optional<Server> chooseServer(List<Server> servers, Request request);

    String getName();
}
