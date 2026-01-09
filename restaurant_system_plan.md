# SYSTEME DE GESTION DE RESTAURANT - ARCHITECTURE TECHNIQUE

## VUE D'ENSEMBLE ARCHITECTURALE

Le projet utilise le design pattern **Mediator** pour orchestrer les interactions entre les differents acteurs du systeme. La classe centrale `Restaurant` (Singleton) contient des references vers tous les **Moniteurs** de ressources partagees.

```text
┌─────────────────────────────────────────────────────────────┐
│                    RESTAURANT (MEDIATOR)                    │
│      Gere l'acces centralise aux ressources partagees       │
└──────────────────────────────┬──────────────────────────────┘
                               │
       ┌───────────────────────┼───────────────────────┐
       v                       v                       v
┌──────────────┐       ┌──────────────┐       ┌──────────────┐
│ TABLE MANAGER│       │ ORDER QUEUE  │       │KITCHEN MGR   │
│ (Monitor)    │       │ (Monitor)    │       │ (Monitor)    │
└──────────────┘       └──────────────┘       └──────────────┘
       ^                       ^                       ^
       │                       │                       │
       └───────────────────────┼───────────────────────┘
                               │
                       ┌───────┴───────┐
                       │    ACTEURS    │
                       │   (Threads)   │
                       └───────────────┘
```

---

## LES COMPOSANTS DU SYSTEME

### 1. LES MONITEURS (Managers)

Les moniteurs encapsulent l'etat partage et gerent la synchronisation via des verrous intrinsèques (`synchronized`) ou explicites (`ReentrantLock`).

#### TableManager
- **Role:** Gestion des 15 tables (10 Normales, 5 VIP).
- **Mecanismes:** 
    - `wait()` / `notifyAll()` pour la file d'attente.
    - `tryLock(timeout)` pour la priorite VIP.
- **Flow:** Le client demande une table; s'il n'y en a pas, il attend dans le moniteur.

#### OrderQueue
- **Role:** File de commandes triee par priorite.
- **Mecanismes:**
    - `PriorityQueue` pour le tri automatique.
    - Modele Producteur-Consommateur (`wait`/`notify`).
- **Flow:** Le Serveur ajoute une commande (`notify`), le Cuisinier la recupère (`wait` si vide).

#### KitchenManager
- **Role:** Gestion des ressources critiques (Fours, Grills, Friteuse).
- **Mecanismes:**
    - `ReentrantLock` individuel par equipement.
    - Strategies d'acquisition ordonnee pour eviter les **DEADLOCKS**.
- **Flow:** Un cuisinier verrouille les ressources necessaires pour un plat et les libère dans un bloc `finally`.

#### Finance & Stock Managers
- **Role:** Gestion du revenu total et des niveaux d'ingredients.
- **Mecanismes:**
    - `synchronized` pour eviter les **Race Conditions** sur la caisse.
    - Thread de gestion de stock dedie qui reapprovisionne automatiquement.

---

## LES ACTEURS (Threads)

Le systeme simule environ **61 threads** concurrents :

| Acteur | Nombre | Role |
|--------|--------|------|
| **Client** | 50 | Arrive, demande une table, commande, mange et paie. |
| **Serveur** | 4 | Intermediaire entre clients et cuisine. |
| **Cuisinier**| 3 | Prepare les plats en utilisant les equipements. |
| **Chef** | 1 | Priorise les commandes urgentes. |
| **Caissier** | 2 | Gere les transactions financières. |
| **Gest. Stock**| 1 | Thread d'arrière-plan pour le reapprovisionnement. |

---

## MAPPING DES CONCEPTS CONCURRENTS

| Concept | Utilisation Concrete |
|---------|----------------------|
| **wait() / notify()** | File d'attente des tables, file de commandes, signaux de stock bas. |
| **synchronized** | Compteurs de revenus, modification de l'etat des tables, acces a la file d'attente. |
| **ReentrantLock** | Equipements de cuisine, tables VIP (verrouillage fin). |
| **tryLock()** | Tentative d'acquisition d'equipements multiples pour eviter le deadlock. |
| **PriorityQueue** | Ordonnancement des plats (VIP/Urgent vs Normal). |

---

## STRUCTURE DU PROJET (Refactorisee)

```text
ma.emsi.restaurant/
│
├── Main.java               # Point d'entree de la simulation
├── Restaurant.java         # Mediator (Singleton)
│
├── entities/               # POJOs (Donnees passives)
│   ├── Table.java
│   ├── Order.java
│   └── Dish.java
│
├── managers/               # Moniteurs (Logique de synchronisation)
│   ├── TableManager.java
│   ├── OrderQueue.java
│   ├── KitchenManager.java
│   ├── FinanceManager.java
│   └── StockManager.java
│
└── actors/                 # Threads (Logique active)
    ├── Client.java
    ├── Server.java
    ├── Cook.java
    └── StockManagerThread.java
```

---

## TIMELINE DE DEVELOPPEMENT

1. **Phase 1: Architecture & Squelette** (Mediator, Entities, Signatures des Managers).
2. **Phase 2: Logique de Synchronisation** (Implementer wait/notify, Locks, PriorityQueues).
3. **Phase 3: Tests de Concurrence** (Verification des deadlocks et des race conditions).
4. **Phase 4: Dashboard & Statistiques** (Affichage temps reel de l'etat du restaurant).