package com.junia.loadbalancer.strategy;

public class StrategyFactory {

    public static BalancingStrategy fromChoice(int choice) {
        return switch (choice) {
            case 1 -> new RoundRobinStrategy();
            case 2 -> new LeastLoadedStrategy();
            case 3 -> new WeightedStrategy();
            default -> throw new IllegalArgumentException("Choix de strategie invalide : " + choice);
        };
    }
}
