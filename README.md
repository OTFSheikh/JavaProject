# Load Balancer : simulateur de répartition de charge

Projet final du cours de Programmation Orientée Objet en Java (M1, JUNIA).

## But de l'application

Dans une infrastructure réseau, un répartiteur de charge (load balancer) reçoit les requêtes des clients et décide quel serveur doit traiter chacune d'elles. Le but est d'éviter qu'un serveur soit saturé pendant que les autres ne font rien, et de continuer à répondre même quand un serveur tombe en panne.

Cette application simule ce fonctionnement en console. Elle charge une liste de serveurs et une liste de requêtes depuis des fichiers CSV, répartit les requêtes selon une stratégie choisie, gère les pannes de serveurs et affiche des statistiques de charge à la fin.

## Lancer l'application

Prérequis : Java 25 et Maven.

Depuis IntelliJ, il suffit d'ouvrir le projet puis de lancer la classe `com.junia.loadbalancer.Application`.

En ligne de commande, depuis la racine du projet :

```bash
mvn compile
java -cp target/classes com.junia.loadbalancer.Application
```

## Lancer les tests

```bash
mvn test
```

## Données utilisées

Les données sont chargées au démarrage depuis deux fichiers situés dans `src/main/resources/` :

- `servers.csv` décrit les serveurs disponibles : nom, capacité maximale, poids et état (`UP` ou `DOWN`).
- `requests.csv` décrit les requêtes à répartir : identifiant, type (`HTTP`, `UPLOAD` ou `STREAM`), heure d'arrivée et un paramètre propre au type (taille du fichier en Mo pour un upload, durée en minutes pour un stream).

## Fonctionnalités principales

*(à compléter au fil du projet)*

## Structure du code et classes principales

*(à compléter au fil du projet)*

## Concepts de POO utilisés

### Héritage
*(à compléter)*

### Polymorphisme
*(à compléter)*

### Classe abstraite et interface
*(à compléter)*

### Exceptions personnalisées
*(à compléter)*

## Principes SOLID appliqués

*(à compléter)*

## Choix techniques

*(à compléter : entre 2 et 5 choix expliqués)*
