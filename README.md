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

Chaque type de requête a une charge différente :

| Type | Charge |
|------|--------|
| HTTP | 1 |
| UPLOAD | 2 + 1 tous les 10 Mo |
| STREAM | 5 + 1 tous les 15 minutes |

### Comment les données sont chargées

La lecture est faite par le package `loader`. La classe abstraite `AbstractCsvParser<T>` lit le fichier, saute la ligne d'en-tête et les lignes vides, puis appelle `parseLine` pour chaque ligne. `ServerCsvParser` et `RequestCsvParser` héritent de cette classe et savent chacun transformer une ligne en objet.

Si une ligne est mal écrite (mauvais nombre de colonnes, nombre ou date invalide, type inconnu...), elle est ignorée et un message indique le numéro de la ligne et la raison. Le reste du fichier est quand même chargé. Si le fichier est introuvable, l'application affiche un message d'erreur au lieu de planter.

## Fonctionnalités principales

- Chargement des serveurs et des requêtes depuis des fichiers CSV, avec gestion des lignes invalides.
- *(répartition, pannes et statistiques : à compléter)*

## Structure du code et classes principales

```
com.junia.loadbalancer
├── Application.java        point d'entrée
├── model/                  les objets du domaine
│   ├── Server
│   ├── ServerStatus        (enum UP / DOWN)
│   ├── Request             (abstraite)
│   ├── HttpRequest
│   ├── FileUploadRequest
│   └── VideoStreamRequest
├── loader/                 lecture des fichiers CSV
│   ├── AbstractCsvParser
│   ├── ServerCsvParser
│   └── RequestCsvParser
└── exception/
    └── InvalidDataException
```

- `Server` : un serveur avec une capacité, un poids, un état et la liste de ses requêtes. On ne peut lui ajouter une requête que par `accept()`, qui vérifie qu'il est UP et qu'il a assez de place.
- `Request` : une requête avec un id et une heure d'arrivée. Chaque type calcule sa propre charge.
- `AbstractCsvParser` et ses sous-classes : lecture des fichiers de données.

## Concepts de POO utilisés

### Héritage
`HttpRequest`, `FileUploadRequest` et `VideoStreamRequest` héritent de `Request`. Ce sont toutes des requêtes avec un id et une heure d'arrivée, mais elles n'ont pas le même poids pour un serveur.

`ServerCsvParser` et `RequestCsvParser` héritent de `AbstractCsvParser`.

### Polymorphisme
`Server.getCurrentLoad()` parcourt une `List<Request>` et appelle `getLoad()` sur chaque requête sans connaître son type. C'est la bonne version de `getLoad()` qui est appelée selon le vrai type de l'objet.

*(stratégies de répartition : à compléter)*

### Classe abstraite et interface
`Request` est abstraite : une requête « générique » n'a pas de sens, c'est toujours un type précis. On a choisi une classe abstraite et pas une interface parce que tous les types partagent des attributs (id, heure d'arrivée) et la même validation.

`AbstractCsvParser<T>` est abstraite et générique : elle contient la lecture commune du fichier, et les sous-classes ne s'occupent que de leur format de ligne.

*(interface des stratégies : à compléter)*

### Exceptions personnalisées
- `InvalidDataException` : lancée quand une ligne d'un fichier CSV est invalide. Elle remplace les exceptions de Java (`NumberFormatException`, `DateTimeParseException`...) par un message clair.
- *(à compléter)*

Les constructeurs du modèle lancent aussi des `IllegalArgumentException` (capacité négative, nom vide...) et `Server.accept()` lance une `IllegalStateException` si le serveur ne peut pas prendre la requête.

## Principes SOLID appliqués

*(à compléter)*

## Choix techniques

1. **Une ligne invalide est ignorée, pas tout le fichier.** Si une seule ligne est fausse, on préfère charger le reste et afficher un avertissement plutôt que de bloquer toute l'application.
2. **La charge d'un serveur est calculée à partir de ses requêtes**, au lieu d'être stockée dans un compteur à part. Comme ça, elle ne peut pas devenir fausse si on oublie de la mettre à jour.

*(à compléter)*
