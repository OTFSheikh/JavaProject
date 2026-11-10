package com.junia.loadbalancer.ui;

import com.junia.loadbalancer.model.Request;
import com.junia.loadbalancer.service.LoadBalancer;
import com.junia.loadbalancer.strategy.BalancingStrategy;
import com.junia.loadbalancer.strategy.StrategyFactory;

import java.util.List;
import java.util.Scanner;

public class ConsoleMenu {

    private final Scanner scanner;
    private final ConsoleReport report;

    public ConsoleMenu(Scanner scanner, ConsoleReport report) {
        this.scanner = scanner;
        this.report = report;
    }

    public BalancingStrategy chooseStrategy() {
        while (true) {
            report.printMessage("Choisis une strategie : 1 = Round Robin, 2 = Least Loaded, 3 = Weighted");
            try {
                return StrategyFactory.fromChoice(readNumber());
            } catch (IllegalArgumentException e) {
                report.printMessage(e.getMessage());
            }
        }
    }

    public void run(LoadBalancer loadBalancer) {
        report.printMessage("\n=== Repartition avec " + loadBalancer.getStrategy().getName() + " ===");
        showState(loadBalancer);

        boolean running = true;
        while (running) {
            printMenu();
            try {
                int choice = readNumber();
                switch (choice) {
                    case 1 -> showState(loadBalancer);
                    case 2 -> failServer(loadBalancer);
                    case 3 -> recoverServer(loadBalancer);
                    case 4 -> report.printStatistics(loadBalancer.getServers());
                    case 0 -> running = false;
                    default -> report.printMessage("Choix inconnu : " + choice);
                }
            } catch (IllegalArgumentException e) {
                // nombre mal tape ou serveur inconnu : on affiche et on continue
                report.printMessage("Erreur : " + e.getMessage());
            }
        }
        report.printMessage("Fin du programme.");
    }

    private void printMenu() {
        report.printMessage("\n1 = etat des serveurs | 2 = panne | 3 = reparation | 4 = statistiques | 0 = quitter");
    }

    private void showState(LoadBalancer loadBalancer) {
        report.printServers(loadBalancer.getServers());
        report.printRejected(loadBalancer.getRejectedRequests());
    }

    private void failServer(LoadBalancer loadBalancer) {
        String name = readText("Nom du serveur a mettre en panne : ");
        List<Request> lost = loadBalancer.failServer(name);
        report.printMessage(name + " est en panne. Requetes perdues : " + lost);
        showState(loadBalancer);
    }

    private void recoverServer(LoadBalancer loadBalancer) {
        String name = readText("Nom du serveur a reparer : ");
        List<Request> placed = loadBalancer.recoverServer(name);
        report.printMessage(name + " est repare. Requetes replacees : " + placed);
        showState(loadBalancer);
    }

    private int readNumber() {
        String line = scanner.nextLine().trim();
        try {
            return Integer.parseInt(line);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("il faut taper un nombre, pas \"" + line + "\"");
        }
    }

    private String readText(String question) {
        report.printMessage(question);
        return scanner.nextLine().trim();
    }
}
