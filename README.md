# Restaurant Management System

Simulation de restaurant en Java démontrant les concepts avancés de concurrence : multi-threading, synchronisation, locks, et prévention des deadlocks.

## Vue d'ensemble

Le système simule un restaurant avec ~61 threads concurrents représentant les clients, serveurs, cuisiniers et personnel. Il démontre les patterns producteur-consommateur, tampons bornés, files de priorité, et stratégies de prévention de deadlock.

## Concepts de Concurrence Démontrés

| Concept | Implémentation |
|---------|---------------|
| Producteur-Consommateur | `OrderQueue` avec `wait()/notify()` |
| Tampon Borné | La file se bloque quand pleine, notifie quand de la place |
| ReentrantLock | Réservations VIP avec `tryLock(timeout)` |
| Prévention Deadlock | Équipement cuisine utilise `tryLock()` avec fallback |
| Synchronized | Prévention des race conditions dans `FinanceManager` |
| PriorityQueue | Commandes triées par priorité (1=URGENT, 2=NORMAL, 3=SLOW) |
| FIFO | Même priorité = tri par timestamp |
| Thread Safety | Tout l'état partagé est synchronisé |

## Architecture

```
src/main/java/ma/emsir/restaurant/
├── managers/
│   ├── FinanceManager.java    - Paiements thread-safe
│   ├── StockManager.java       - Inventaire avec wait/notify
│   ├── OrderQueue.java         - File bornée avec priorité
│   ├── TableManager.java       - Priorité VIP avec ReentrantLock
│   └── KitchenManager.java     - Locks équipement, prévention deadlock
├── entities/
│   ├── Order.java              - Commande avec priorité comparable
│   ├── Dish.java               - Plats avec équipement/ingrédients
│   └── Table.java              - Table avec lock
├── actors/
│   ├── Client.java             - Acquisition table, commande, paiement
│   ├── Server.java             - Producteur : ajoute commandes à la file
│   ├── Cook.java               - Consommateur : prépare les commandes
│   ├── Cashier.java            - Traitement paiements
│   └── StockManagerThread.java - Réapprovisionnement en arrière-plan
└── Démos
    ├── Main.java               - Simulation complète
    ├── Demo.java               - Preuve concept par concept
    ├── TeamDemo.java           - Showcase par contributeur
    └── EdgeCaseDemo.java       - Tests cas limites
```

## Exécution

```bash
# Compiler et installer
mvn clean install

# Simulation principale
mvn exec:java -Dexec.mainClass="ma.emsi.restaurant.Main"

# Démo concepts
mvn exec:java -Dexec.mainClass="ma.emsi.restaurant.Demo"

# Démo équipe
mvn exec:java -Dexec.mainClass="ma.emsi.restaurant.TeamDemo"

# Tests cas limites
mvn exec:java -Dexec.mainClass="ma.emsi.restaurant.EdgeCaseDemo"
```

## Couverture de Tests

115 tests JUnit couvrant tous les modules :

| Module | Tests | Couverture |
|--------|-------|------------|
| FinanceManager | 14 | Paiements, concurrence, race conditions |
| OrderQueue | 10 | Priorité, tampon borné, blocage |
| StockManager | 19 | Consommation, réapprovisionnement, thread safety |
| KitchenManager | 15 | Acquisition équipement, prévention deadlock |
| Order/Dish | 42 | Entités, comparaison, equals/hashCode |
| Cook/Server | 14 | Intégration bout en bout |

Lancer les tests :
```bash
mvn test
```

## Contributions de l'Équipe

| Membre | Module | Contributions |
|--------|--------|---------------|
| Saladin | Finance & Stock | `FinanceManager`, `StockManager`, tests JUnit |
| Walid | Gestion Tables | Priorité VIP avec `ReentrantLock` et timeout |
| Anakin | File de Commandes | `OrderQueue`, entités, `Cook`, `Server` |
| Marwan | Équipement Cuisine | `KitchenManager`, prévention deadlock |

## Prérequis

- Java 11+
- Maven 3.6+
