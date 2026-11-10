# Load Balancer : simulateur de répartition de charge

Projet final du cours de Programmation Orientée Objet en Java (M1, JUNIA).

## But de l'application

Dans une infrastructure réseau, un répartiteur de charge (load balancer) reçoit les requêtes des clients et décide quel serveur doit traiter chacune d'elles. Le but est d'éviter qu'un serveur soit saturé pendant que les autres ne font rien, et de continuer à répondre même quand un serveur tombe en panne.

Cette application simule ce fonctionnement en console. Elle charge une liste de serveurs et une liste de requêtes depuis des fichiers CSV, répartit les requêtes selon une stratégie choisie, gère les pannes de serveurs et affiche des statistiques de charge.

## Lancer l'application

Prérequis : Java 25 et Maven.

Depuis IntelliJ, il suffit d'ouvrir le projet puis de lancer la classe `com.junia.loadbalancer.Application`. Le programme doit être lancé depuis la racine du projet, pour trouver les fichiers de `src/main/resources/`.

Au démarrage, l'application demande la stratégie de répartition (1 = Round Robin, 2 = Least Loaded, 3 = Weighted), répartit les requêtes et affiche le résultat. Un menu permet ensuite d'afficher l'état des serveurs, de mettre un serveur en panne, de réparer un serveur ou d'afficher les statistiques.

En ligne de commande, depuis la racine du projet :

```bash
mvn compile
java -cp target/classes com.junia.loadbalancer.Application
```

## Lancer les tests

```bash
mvn test
```

Dans IntelliJ : clic droit sur le dossier `src/test/java`, puis « Run 'All Tests' ».

Les tests JUnit 5 (37 tests) vérifient la logique métier, pas les getters :

- `ServerTest` : acceptation d'une requête, refus quand le serveur est plein ou en panne, requêtes rendues lors d'une panne, capacité invalide.
- `RequestTest` : calcul de la charge selon le type, valeurs minimales, paramètres invalides.
- `BalancingStrategyTest` : comportement de chaque stratégie, serveur en panne ignoré, aucun serveur possible, liste vide.
- `LoadBalancerTest` : répartition, `NoServerAvailableException` quand tout est plein, ordre d'arrivée, redistribution après une panne, requêtes replacées après une réparation, serveur inconnu, noms de serveurs en double.
- `StatisticsServiceTest` : moyenne sur les serveurs UP, serveur le plus chargé, équilibre (avec le cas limite à 20 points), comptage par type, liste vide.
- `CsvParserTest` : lignes valides, `InvalidDataException` pour les lignes invalides, lignes fausses ignorées dans un fichier, fichier introuvable.

Chaque partie teste des cas normaux, des cas limites et des cas d'erreur.

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
- Choix de la stratégie de répartition au lancement :
  - **Round Robin** : les serveurs reçoivent les requêtes à tour de rôle.
  - **Least Loaded** : la requête va au serveur le moins rempli (en %).
  - **Weighted** : la charge est divisée par le poids du serveur, un serveur de poids 3 reçoit environ 3 fois plus de travail.
- Répartition des requêtes dans l'ordre d'arrivée. Une requête qu'aucun serveur ne peut prendre est rejetée, sans arrêter le reste.
- Panne d'un serveur : il passe `DOWN` et ses requêtes sont redistribuées aux autres serveurs. Celles qui ne trouvent pas de place sont rejetées.
- Réparation d'un serveur : il repasse `UP` et l'application retente de placer les requêtes rejetées.
- Statistiques : charge moyenne, serveur le plus chargé, répartition équilibrée ou non (moins de 20 points d'écart), nombre de requêtes par type.

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
├── strategy/               choix du serveur
│   ├── BalancingStrategy   (interface)
│   ├── RoundRobinStrategy
│   ├── LeastLoadedStrategy
│   ├── WeightedStrategy
│   └── StrategyFactory
├── service/                logique métier
│   ├── LoadBalancer
│   └── StatisticsService
├── ui/                     interaction avec l'utilisateur
│   ├── ConsoleMenu
│   └── ConsoleReport
└── exception/
    ├── InvalidDataException
    └── NoServerAvailableException
```

- `Server` : un serveur avec une capacité, un poids, un état et la liste de ses requêtes. On ne peut lui ajouter une requête que par `accept()`, qui vérifie qu'il est UP et qu'il a assez de place.
- `Request` : une requête avec un id et une heure d'arrivée. Chaque type calcule sa propre charge.
- `AbstractCsvParser` et ses sous-classes : lecture des fichiers de données.
- `BalancingStrategy` : choisit le serveur qui prend une requête. Trois implémentations.
- `LoadBalancer` : répartit les requêtes avec la stratégie reçue, garde les requêtes rejetées, gère les pannes et les réparations.
- `StatisticsService` : calcule les statistiques à partir des serveurs.
- `ConsoleMenu` : lit les choix de l'utilisateur. `ConsoleReport` : s'occupe de tout l'affichage.
- `Application` : charge les fichiers, crée les objets et lance le menu, sans logique métier.

## Concepts de POO utilisés

### Héritage
`HttpRequest`, `FileUploadRequest` et `VideoStreamRequest` héritent de `Request`. Ce sont toutes des requêtes avec un id et une heure d'arrivée, mais elles n'ont pas le même poids pour un serveur.

`ServerCsvParser` et `RequestCsvParser` héritent de `AbstractCsvParser`.

### Polymorphisme
`Server.getCurrentLoad()` parcourt une `List<Request>` et appelle `getLoad()` sur chaque requête sans connaître son type. C'est la bonne version de `getLoad()` qui est appelée selon le vrai type de l'objet.

Le `LoadBalancer` appelle `strategy.chooseServer(...)` sans savoir quelle stratégie il a reçue. Le serveur choisi dépend de l'objet réel (`RoundRobinStrategy`, `LeastLoadedStrategy` ou `WeightedStrategy`).

### Classe abstraite et interface
`Request` est abstraite : une requête « générique » n'a pas de sens, c'est toujours un type précis. On a choisi une classe abstraite et pas une interface parce que tous les types partagent des attributs (id, heure d'arrivée) et la même validation.

`AbstractCsvParser<T>` est abstraite et générique : elle contient la lecture commune du fichier, et les sous-classes ne s'occupent que de leur format de ligne.

`BalancingStrategy` est une interface : les stratégies ne partagent aucun attribut, seulement un comportement (choisir un serveur). C'est la différence avec `Request`, où les sous-classes partagent des attributs.

### Exceptions personnalisées
- `InvalidDataException` : lancée quand une ligne d'un fichier CSV est invalide. Elle remplace les exceptions de Java (`NumberFormatException`, `DateTimeParseException`...) par un message clair.
- `NoServerAvailableException` : lancée par `LoadBalancer.assign()` quand aucun serveur ne peut prendre la requête (tous pleins ou en panne). Elle est attrapée pour ranger la requête dans la liste des rejetées.

Les constructeurs lancent aussi des `IllegalArgumentException` (capacité négative, nom vide, deux serveurs avec le même nom dans le `LoadBalancer`...) et `Server.accept()` lance une `IllegalStateException` si le serveur ne peut pas prendre la requête.

## Principes SOLID appliqués

- **S (responsabilité unique)** : chaque classe a un rôle. La lecture des fichiers est dans `loader`, la répartition dans `LoadBalancer`, les statistiques dans `StatisticsService`, l'affichage dans `ConsoleReport` et la saisie dans `ConsoleMenu`. `Application` ne fait que relier les morceaux.
- **O (ouvert/fermé)** : pour ajouter une stratégie, on crée une classe qui implémente `BalancingStrategy` (et on l'ajoute au choix dans `StrategyFactory`). Le `LoadBalancer` ne change pas. Pareil pour un nouveau type de requête : une sous-classe de `Request`.
- **L (substitution de Liskov)** : n'importe quelle sous-classe de `Request` peut être utilisée partout où une `Request` est attendue, et n'importe quelle stratégie peut être donnée au `LoadBalancer`.
- **I (ségrégation des interfaces)** : `BalancingStrategy` est petite et ne contient que ce qu'une stratégie doit faire.
- **D (inversion des dépendances)** : le `LoadBalancer` reçoit sa stratégie dans son constructeur et ne dépend que de l'interface `BalancingStrategy`, jamais d'une classe concrète.

## Choix techniques

1. **Une ligne invalide est ignorée, pas tout le fichier.** Si une seule ligne est fausse, on préfère charger le reste et afficher un avertissement plutôt que de bloquer toute l'application.
2. **La charge d'un serveur est calculée à partir de ses requêtes**, au lieu d'être stockée dans un compteur à part. Comme ça, elle ne peut pas devenir fausse si on oublie de la mettre à jour.
3. **Une stratégie renvoie un `Optional<Server>`** plutôt que `null` quand aucun serveur n'est possible. Le `LoadBalancer` est obligé de gérer ce cas, et il lance alors `NoServerAvailableException`.
4. **Une requête rejetée n'arrête pas la répartition.** Elle est gardée dans une liste, et on la retente quand un serveur est réparé. Un vrai load balancer continue de servir les autres clients.
5. **Les statistiques ne prennent en compte que les serveurs `UP`.** Un serveur en panne à 0 % fausserait la moyenne et l'équilibre.
