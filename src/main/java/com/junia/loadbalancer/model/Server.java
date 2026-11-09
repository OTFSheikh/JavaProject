package com.junia.loadbalancer.model;

import java.util.ArrayList;
import java.util.List;

public class Server {

    private String name;
    private int capacity;
    private int weight;
    private ServerStatus status;
    private List<Request> requests;

    public Server(String name, int capacity, int weight, ServerStatus status) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Le nom du serveur est vide.");
        }
        if (capacity <= 0) {
            throw new IllegalArgumentException("La capacite doit etre superieure a 0.");
        }
        if (weight <= 0) {
            throw new IllegalArgumentException("Le poids doit etre superieur a 0.");
        }

        this.name = name;
        this.capacity = capacity;
        this.weight = weight;
        this.status = status;
        this.requests = new ArrayList<>();
    }

    public boolean isUp() {
        return status == ServerStatus.UP;
    }

    public int getCurrentLoad() {
        int load = 0;
        for (Request request : requests) {
            load += request.getLoad();
        }
        return load;
    }

    public int getRemainingCapacity() {
        return capacity - getCurrentLoad();
    }

    public boolean canAccept(Request request) {
        return isUp() && request.getLoad() <= getRemainingCapacity();
    }

    public void accept(Request request) {
        if (!canAccept(request)) {
            throw new IllegalStateException(
                    "Le serveur " + name + " ne peut pas prendre la requete " + request.getId()
            );
        }
        requests.add(request);
    }

    // le serveur tombe : on rend ses requetes pour les redistribuer
    public List<Request> goDown() {
        status = ServerStatus.DOWN;
        List<Request> lostRequests = new ArrayList<>(requests);
        requests.clear();
        return lostRequests;
    }

    public void goUp() {
        status = ServerStatus.UP;
    }

    public double getLoadPercentage() {
        return getCurrentLoad() * 100.0 / capacity;
    }

    public String getName() {
        return name;
    }

    public int getCapacity() {
        return capacity;
    }

    public int getWeight() {
        return weight;
    }

    public ServerStatus getStatus() {
        return status;
    }

    public List<Request> getRequests() {
        return List.copyOf(requests);
    }

    @Override
    public String toString() {
        return name + " [" + status + "] " + getCurrentLoad() + "/" + capacity;
    }
}
