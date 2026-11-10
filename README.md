# Load Balancer

Projet final du cours de POO en Java (M1 JUNIA).

## Le projet

C'est un simulateur de répartiteur de charge en console. Le programme lit une liste de serveurs et une liste de requêtes dans des fichiers CSV, puis répartit les requêtes sur les serveurs selon une stratégie. On peut ensuite mettre un serveur en panne ou le réparer pour voir comment les requêtes sont redistribuées.

## Lancer l'application et les tests

Il faut Java 25 et Maven.

Dans IntelliJ, il suffit de lancer la classe `Application`. En ligne de commande, depuis la racine du projet :

```bash
mvn compile
java -cp target/classes com.junia.loadbalancer.Application
```

Le programme doit être lancé depuis la racine du projet, sinon il ne trouve pas les fichiers CSV.

Pour les tests : `mvn test`, ou clic droit sur `src/test/java` puis Run All Tests dans IntelliJ. Il y a 40 tests JUnit 5 sur la logique du projet (calcul de charge, stratégies, pannes, statistiques, lecture des CSV), avec des cas normaux, des cas limites et des cas d'erreur.

## Déroulement

1. Le programme charge `servers.csv` et `requests.csv` et crée les objets.
2. Il demande la stratégie : 1 = Round Robin, 2 = Least Loaded, 3 = Weighted.
3. Il répartit les requêtes dans leur ordre d'arrivée et affiche chaque serveur avec sa charge, plus les requêtes rejetées.
4. Un menu permet ensuite de réafficher l'état des serveurs, de mettre un serveur en panne, de le réparer, de voir les statistiques ou de quitter.

Si un fichier est introuvable ou si une saisie est fausse, le programme affiche un message au lieu de planter.

## Fonctionnalités

- Trois stratégies au choix au démarrage : Round Robin (chacun son tour), Least Loaded (le serveur le moins rempli en %) et Weighted (un serveur de poids 3 reçoit environ 3 fois plus de charge).
- Une requête qui ne rentre sur aucun serveur est rejetée, et la répartition continue pour les autres.
- Panne d'un serveur : ses requêtes repartent sur les serveurs restants.
- Réparation d'un serveur : les requêtes rejetées sont retentées.
- Statistiques : charge moyenne, serveur le plus chargé, répartition équilibrée ou non, nombre de requêtes par type.

## Les données

Les deux fichiers sont dans `src/main/resources/` et sont chargés au démarrage.

- `servers.csv` : nom, capacité, poids, état (`UP` ou `DOWN`).
- `requests.csv` : id, type, heure d'arrivée et un paramètre (taille en Mo pour un upload, durée en minutes pour un stream).

Chaque requête a une charge : 1 pour HTTP, 2 + 1 par tranche de 10 Mo pour UPLOAD, 5 + 1 par quart d'heure pour STREAM. Un serveur ne peut pas dépasser sa capacité.

`AbstractCsvParser<T>` lit le fichier et saute l'en-tête. `ServerCsvParser` et `RequestCsvParser` transforment chaque ligne en objet. Une ligne mal écrite est ignorée avec un message, et le reste du fichier est chargé quand même.

## Organisation du code

```
model/      Server, ServerStatus, Request, RequestType,
            HttpRequest, FileUploadRequest, VideoStreamRequest
loader/     AbstractCsvParser, ServerCsvParser, RequestCsvParser
strategy/   BalancingStrategy, RoundRobinStrategy, LeastLoadedStrategy,
            WeightedStrategy, StrategyFactory
service/    LoadBalancer, StatisticsService
ui/         ConsoleMenu, ConsoleReport
exception/  InvalidDataException, NoServerAvailableException
Application.java
```

Les classes principales :

- `Server` garde ses requêtes et refuse celles qui dépassent sa capacité ou qui arrivent quand il est en panne.
- `Request` est la classe mère des trois types de requêtes.
- `LoadBalancer` répartit les requêtes avec la stratégie choisie et gère les pannes et les réparations.
- `StatisticsService` fait les calculs de statistiques.
- `ConsoleMenu` lit les choix de l'utilisateur et `ConsoleReport` affiche les résultats.
- `Application` charge les fichiers et lance le menu, rien d'autre.

## Héritage, polymorphisme, classe abstraite et interface

**Héritage.** `HttpRequest`, `FileUploadRequest` et `VideoStreamRequest` héritent de `Request`. Elles ont toutes un id et une heure d'arrivée, mais pas la même charge. Les deux parsers héritent aussi de `AbstractCsvParser`.

**Polymorphisme.** `Server.getCurrentLoad()` parcourt une `List<Request>` et appelle `getLoad()` sans connaître le type de chaque requête. De la même façon, le `LoadBalancer` appelle `strategy.chooseServer(...)` sans savoir quelle stratégie il a reçue.

**Classe abstraite.** `Request` est abstraite parce que les trois types partagent des attributs et la même validation. `AbstractCsvParser<T>` l'est aussi, pour ne pas écrire deux fois la lecture du fichier.

**Interface.** `BalancingStrategy` est une interface parce que les stratégies n'ont aucun attribut en commun, seulement un comportement : choisir un serveur.

## Exceptions

- `InvalidDataException` : une ligne d'un CSV est invalide (nombre, date ou type incorrect).
- `NoServerAvailableException` : aucun serveur ne peut prendre la requête. Le `LoadBalancer` l'attrape pour ranger la requête dans les rejetées.

Les constructeurs lancent aussi des `IllegalArgumentException` (capacité négative, nom vide, deux serveurs avec le même nom, deux requêtes avec le même id). Dans ces deux derniers cas, l'application s'arrête avec un message.

## SOLID

- **S** : chaque classe a un seul rôle. La lecture des fichiers, la répartition, les statistiques, la saisie et l'affichage sont dans des classes séparées.
- **O** : pour ajouter une stratégie, on crée une nouvelle classe qui implémente `BalancingStrategy`, sans modifier le `LoadBalancer`.
- **L** : n'importe quelle sous-classe de `Request` et n'importe quelle stratégie peuvent être utilisées à la place d'une autre.
- **I** : `BalancingStrategy` ne contient que deux méthodes, celles dont une stratégie a besoin.
- **D** : le `LoadBalancer` reçoit sa stratégie dans son constructeur et ne connaît que l'interface.

## Choix techniques

1. Une ligne invalide dans un CSV est ignorée au lieu de bloquer tout le chargement. Une seule erreur de saisie ne doit pas empêcher l'application de démarrer.
2. La charge d'un serveur est recalculée à partir de ses requêtes, au lieu d'être stockée dans un compteur. Elle ne peut donc pas être fausse.
3. Une stratégie renvoie un `Optional<Server>` plutôt que `null`. Le `LoadBalancer` est obligé de traiter le cas où aucun serveur n'est disponible.
4. Les statistiques ne comptent que les serveurs `UP`, parce qu'un serveur en panne à 0 % fausserait la moyenne.
