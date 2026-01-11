# SYSTÈME DE GESTION DE RESTAURANT - PLAN VISUEL

## VUE D'ENSEMBLE EN 1 IMAGE

```text
┌─────────────────────────────────────────────────────────────────────┐
│                    RESTAURANT "LE CONCURRENT"                        │
│                    50 Clients | 11 Personnel                         │
└─────────────────────────────────────────────────────────────────────┘

┌──────────────┐  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐
│   MODULE 1   │  │   MODULE 2   │  │   MODULE 3   │  │   MODULE 4   │
│              │  │              │  │              │  │              │
│   TABLES     │  │  COMMANDES   │  │ ÉQUIPEMENTS  │  │ CAISSE+STOCK │
│              │  │              │  │              │  │              │
│  Personne 1  │  │  Personne 2  │  │  Personne 3  │  │  Personne 4  │
└──────────────┘  └──────────────┘  └──────────────┘  └──────────────┘
       │                 │                 │                 │
       │                 │                 │                 │
       v                 v                 v                 v
  wait/notify      wait/notify      ReentrantLock      synchronized
  synchronized     synchronized     tryLock()          wait/notify
  ReentrantLock    PriorityQueue    DEADLOCK!          Thread dédié
```

---

## LES 4 MODULES EXPLIQUÉS

### MODULE 1 : GESTION DES TABLES

**Personne 1**

```text
PROBLÈME : 50 clients arrivent, seulement 15 tables

┌─────────────────────────────────────────┐
│  TABLES NORMALES (10)                   │
│  ┌───┐ ┌───┐ ┌───┐ ... ┌───┐           │
│  │ 1 │ │ 2 │ │ 3 │ ... │10 │           │
│  └───┘ └───┘ └───┘     └───┘           │
│                                         │
│  TABLES VIP (5)                         │
│  ┌───┐ ┌───┐ ┌───┐ ┌───┐ ┌───┐        │
│  │V1 │ │V2 │ │V3 │ │V4 │ │V5 │        │
│  └───┘ └───┘ └───┘ └───┘ └───┘        │
└─────────────────────────────────────────┘
         │
         v
    FILE D'ATTENTE
    ┌───┐┌───┐┌───┐┌───┐┌───┐
    │C11││C12││C13││C14││C15│...
    └───┘└───┘└───┘└───┘└───┘
```

#### Concepts Utilisés

| Concept | Utilisation |
|---------|-------------|
| `wait()` | Client attend qu'une table se libère |
| `notifyAll()` | Table libérée, réveille tous les clients |
| `synchronized` | Protège le compteur de tables |
| `ReentrantLock` | Une par table VIP (réservation exclusive) |
| `tryLock(30s)` | VIP attend max 30s, sinon bascule sur table normale |

#### Flow

```java
Client arrive
    ↓
VIP? → tryLock(table VIP, 30s)
    ├─ Succès → Table VIP
    └─ Échec → File normale

Normal? → synchronized(tables) {
    if(dispo > 0) → Assigne table
    else → wait() // Attend libération
}
```

---

### MODULE 2 : FILE DE COMMANDES

**Personne 2**

```text
PROBLÈME : 4 serveurs ajoutent, 3 cuisiniers + 1 chef prennent

    SERVEURS (Producers)              CUISINIERS (Consumers)
    ┌──┐ ┌──┐ ┌──┐ ┌──┐                ┌──┐ ┌──┐ ┌──┐ ┌────┐
    │S1│ │S2│ │S3│ │S4│                │C1│ │C2│ │C3│ │CHEF│
    └┬─┘ └┬─┘ └┬─┘ └┬─┘                └─┬┘ └─┬┘ └─┬┘ └─┬──┘
     │    │    │    │                     │    │    │    │
     └────┴────┴────┘                     └────┴────┴────┘
            │                                     │
            v                                     v
    ┌───────────────────────────────────────────────┐
    │         FILE DE COMMANDES (PriorityQueue)     │
    │  ┌──────────┐  ┌──────────┐  ┌──────────┐   │
    │  │URGENTE #1│  │URGENTE #2│  │NORMALE #3│   │
    │  │Prio: 1   │  │Prio: 1   │  │Prio: 2   │   │
    │  └──────────┘  └──────────┘  └──────────┘   │
    └───────────────────────────────────────────────┘
```

#### Concepts Utilisés

| Concept | Utilisation |
|---------|-------------|
| `wait()` | Cuisinier attend si file vide |
| `notify()` | Serveur ajoute commande, réveille UN cuisinier |
| `synchronized` | Protège la PriorityQueue |
| `PriorityQueue` | Tri automatique par priorité |

#### Flow

```java
// SERVEUR:
synchronized(fileCommandes) {
    fileCommandes.add(nouvelleCommande);
    notify(); // Réveille UN cuisinier
}

// CUISINIER:
synchronized(fileCommandes) {
    while(fileCommandes.isEmpty()) {
        wait(); // Dort jusqu'à réveil
    }
    commande = fileCommandes.poll(); // Prend selon priorité
}
```

#### Types de Commandes

| Type | Priorité | Exemples | Temps |
|------|----------|----------|-------|
| URGENTE | 1 | Dessert, Boisson | 30 sec |
| NORMALE | 2 | Plat principal | 3 min |
| LENTE | 3 | Plat mijoté | 5 min |

---

### MODULE 3 : ÉQUIPEMENTS DE CUISINE

**Personne 3**

> [!CAUTION]
> Ce module démontre intentionnellement un **DEADLOCK CIRCULAIRE** et ses solutions!

```text
PROBLÈME : Ressources limitées partagées → DEADLOCK!

ÉQUIPEMENTS (Ressources):
┌──────────┐  ┌──────────┐  ┌──────────┐
│  FOUR-1  │  │  FOUR-2  │  │  FOUR-3  │
│ [LOCK-1] │  │ [LOCK-2] │  │ [LOCK-3] │
└──────────┘  └──────────┘  └──────────┘

┌──────────┐  ┌──────────┐
│  GRILL-1 │  │  GRILL-2 │
│ [LOCK-4] │  │ [LOCK-5] │
└──────────┘  └──────────┘

┌──────────┐
│ FRITEUSE │
│ [LOCK-6] │
└──────────┘
```

#### Le Deadlock Circulaire

```text
SCÉNARIO QUI BLOQUE:

Cuisinier-1: Fait PIZZA
    1. Lock(FOUR-1) ✓
    2. Attend Lock(FRITEUSE) ⏳ [occupée par C2]

Cuisinier-2: Fait STEAK-FRITES
    1. Lock(FRITEUSE) ✓
    2. Attend Lock(GRILL-1) ⏳ [occupé par C3]

Cuisinier-3: Fait VIANDE AU FOUR
    1. Lock(GRILL-1) ✓
    2. Attend Lock(FOUR-1) ⏳ [occupé par C1]

    ┌──────────┐
    │    C1    │
    │ (FOUR-1) │
    │  ↓ attend│
    └────FRIT──┘
         ↑    │
    attend│    │tient
         │    ↓
    ┌────┴─────┐      ┌──────────┐
    │    C3    │      │    C2    │
    │(GRILL-1) │◄─────│(FRITEUSE)│
    │          │attend│          │
    └──────────┘      └──────────┘

→ DEADLOCK CIRCULAIRE! Personne ne peut avancer!
```

#### Concepts Utilisés

| Concept | Utilisation |
|---------|-------------|
| `ReentrantLock` | Un verrou par équipement |
| `lock()` | Acquisition exclusive |
| `unlock()` | Libération (TOUJOURS dans finally!) |
| `tryLock(timeout)` | Tentative avec limite de temps |

#### Solutions

##### VERSION 1 - DÉMO (Bloque)

```java
// MAUVAIS - Ordre différent
four.lock();
friteuse.lock();  // Deadlock!
```

##### VERSION 2 - tryLock (OK)

```java
// BON - Timeout + retry
if(four.tryLock(2, SECONDS)) {
    try {
        if(friteuse.tryLock(2, SECONDS)) {
            try {
                cuisiner();
            } finally { friteuse.unlock(); }
        } else {
            // Timeout → Libère tout, réessaye
        }
    } finally { four.unlock(); }
}
```

##### VERSION 3 - Ordre cohérent (OK)

```java
// BON - Toujours même ordre
friteuse.lock();  // 1
four.lock();      // 2
grill.lock();     // 3
try {
    cuisiner();
} finally {
    grill.unlock();
    four.unlock();
    friteuse.unlock();
}
```

---

### MODULE 4 : CAISSE + STOCK

**Personne 4**

#### PARTIE A: CAISSE (Race Condition)

> [!WARNING]
> Démontre une **race condition** classique sur une variable partagée!

```text
2 Caissiers traitent paiements simultanément:
┌──────┐        ┌──────┐
│Caiss1│        │Caiss2│
└───┬──┘        └───┬──┘
    │               │
    └───────┬───────┘
            v
    ┌───────────────┐
    │ revenuTotal   │  ← VARIABLE PARTAGÉE
    │ (int)         │
    └───────────────┘
```

**SANS synchronized:**
```java
Caiss1 lit: 100€
Caiss2 lit: 100€
Caiss1 écrit: 100 + 15 = 115€
Caiss2 écrit: 100 + 20 = 120€
→ Résultat: 120€ (15€ perdus!)
```

**AVEC synchronized:**
```java
Caiss1 lock → lit 100 → écrit 115 → unlock
Caiss2 lock → lit 115 → écrit 135 → unlock
→ Résultat: 135€ OK
```

#### PARTIE B: STOCK (wait/notify + Thread dédié)

```text
┌────────────────────────────────────────┐
│  STOCK INGRÉDIENTS                     │
│  ┌──────────┐  ┌──────────┐           │
│  │ Tomates  │  │ Fromage  │           │
│  │   50     │  │   30     │           │
│  └──────────┘  └──────────┘           │
│  ┌──────────┐  ┌──────────┐           │
│  │  Pâtes   │  │  Viande  │           │
│  │   40     │  │   25     │           │
│  └──────────┘  └──────────┘           │
└────────────────────────────────────────┘
         │                    ↑
         │ consomme           │ réapprovisionne
         v                    │
    CUISINIERS           GESTIONNAIRE STOCK
    ┌──┐┌──┐┌──┐            ┌────┐
    │C1││C2││C3│            │ GS │ (Thread dédié)
    └──┘└──┘└──┘            └────┘
```

#### Flow Stock

```java
// CUISINIER consomme:
synchronized(stock) {
    if(stock.tomates < 5) {
        notify(gestionnaireStock); // Signal stock bas!
        wait(); // Attend réapprovisionnement
    }
    stock.tomates -= 5;
}

// GESTIONNAIRE STOCK (thread qui tourne):
while(true) {
    synchronized(stock) {
        while(!stock.estBas()) {
            wait(); // Dort jusqu'à signal
        }
        // Stock bas détecté!
        sleep(3000); // Simule livraison
        stock.reapprovisionner(+50);
        notifyAll(); // Réveille cuisiniers bloqués
    }
}
```

#### Concepts Utilisés

| Concept | Utilisation |
|---------|-------------|
| `synchronized` | Protège compteur revenus (race condition) |
| `wait()` | Cuisinier attend réapprovisionnement |
| `notify()` | Signal stock bas |
| `notifyAll()` | Stock rempli, réveille tous cuisiniers |
| `Thread` dédié | GestionnaireStock tourne en arrière-plan |

---

## FLOW GLOBAL - VIE D'UN CLIENT

```text
┌─────────────────────────────────────────────────────────────┐
│  1. CLIENT ARRIVE                                           │
│     Thread démarre                                          │
└────────────────┬────────────────────────────────────────────┘
                 │
                 v
┌─────────────────────────────────────────────────────────────┐
│  2. DEMANDE TABLE                    [MODULE 1]             │
│     VIP?                                                    │
│     ├─ Oui → tryLock(tableVIP, 30s)                        │
│     │         ├─ OK → Table VIP                            │
│     │         └─ Timeout → File normale                    │
│     └─ Non → synchronized(tables) {                        │
│                  if(dispo) → Table                          │
│                  else → wait() // Attend                    │
│               }                                             │
└────────────────┬────────────────────────────────────────────┘
                 │
                 v
┌─────────────────────────────────────────────────────────────┐
│  3. ASSIS À TABLE                                           │
│     sleep(1000-2000) // Regarde menu                        │
└────────────────┬────────────────────────────────────────────┘
                 │
                 v
┌─────────────────────────────────────────────────────────────┐
│  4. SERVEUR PREND COMMANDE           [MODULE 2]             │
│     synchronized(fileCommandes) {                           │
│         fileCommandes.add(commande)                         │
│         notify() // Réveille cuisinier                      │
│     }                                                       │
└────────────────┬────────────────────────────────────────────┘
                 │
                 v
┌─────────────────────────────────────────────────────────────┐
│  5. CUISINIER PREND COMMANDE         [MODULE 2]             │
│     synchronized(fileCommandes) {                           │
│         while(vide) wait()                                  │
│         commande = poll() // Selon priorité                 │
│     }                                                       │
└────────────────┬────────────────────────────────────────────┘
                 │
                 v
┌─────────────────────────────────────────────────────────────┐
│  6. VÉRIFIER STOCK                   [MODULE 4]             │
│     synchronized(stock) {                                   │
│         if(stock.suffisant()) {                             │
│             stock.consommer()                               │
│         } else {                                            │
│             notify(gestStock)                               │
│             wait() // Attend réappro                        │
│         }                                                   │
│     }                                                       │
└────────────────┬────────────────────────────────────────────┘
                 │
                 v
┌─────────────────────────────────────────────────────────────┐
│  7. ACQUÉRIR ÉQUIPEMENTS             [MODULE 3]             │
│     if(four.tryLock(2, SEC)) {                              │
│         try {                                               │
│             if(friteuse.tryLock(2, SEC)) {                  │
│                 try {                                       │
│                     sleep(3000) // CUISINE                  │
│                 } finally { friteuse.unlock(); }            │
│             }                                               │
│         } finally { four.unlock(); }                        │
│     }                                                       │
└────────────────┬────────────────────────────────────────────┘
                 │
                 v
┌─────────────────────────────────────────────────────────────┐
│  8. SERVEUR LIVRE PLAT                                      │
└────────────────┬────────────────────────────────────────────┘
                 │
                 v
┌─────────────────────────────────────────────────────────────┐
│  9. CLIENT MANGE                                            │
│     sleep(3000-5000) // Dégustation                         │
└────────────────┬────────────────────────────────────────────┘
                 │
                 v
┌─────────────────────────────────────────────────────────────┐
│  10. PAYER                           [MODULE 4]             │
│      synchronized(caisse) {                                 │
│          revenuTotal += montant // Race condition!          │
│          nbClients++                                        │
│      }                                                      │
└────────────────┬────────────────────────────────────────────┘
                 │
                 v
┌─────────────────────────────────────────────────────────────┐
│  11. LIBÉRER TABLE                   [MODULE 1]             │
│      synchronized(tables) {                                 │
│          tables[num] = false                                │
│          disponibles++                                      │
│          notifyAll() // Réveille clients en attente         │
│      }                                                      │
│      Thread se termine                                      │
└─────────────────────────────────────────────────────────────┘
```

---

## MAPPING CONCEPTS → UTILISATION

| Concept | Où Utilisé |
|---------|------------|
| **wait()** | • Client attend table<br>• Cuisinier attend commande<br>• Cuisinier attend stock<br>• GestStock attend signal stock bas |
| **notify()** | • Commande ajoutée (réveille 1 cuisinier)<br>• Stock bas (réveille GestStock) |
| **notifyAll()** | • Table libérée (réveille tous clients)<br>• Stock rempli (réveille tous cuisiniers) |
| **synchronized** | • Compteur tables disponibles<br>• File de commandes (PriorityQueue)<br>• Stock ingrédients<br>• Compteur revenus (race condition!)<br>• File attente clients |
| **ReentrantLock** | • Chaque table VIP (5 locks)<br>• Chaque four (3 locks)<br>• Chaque grill (2 locks)<br>• Friteuse (1 lock) |
| **tryLock()** | • Tables VIP (timeout 30s → bascule)<br>• Équipements (timeout 2s → retry)<br>• Évite DEADLOCK circulaire |
| **sleep()** | • Client regarde menu (1-2s)<br>• Client mange (3-5s)<br>• Cuisinier cuisine (2-4s)<br>• Livraison stock (3s) |

---

## LES ACTEURS (Threads)

### Clients (50 threads)
```text
┌──┐┌──┐┌──┐┌──┐┌──┐
│C1││C2││C3││C4││C5│ ... x50
└──┘└──┘└──┘└──┘└──┘
```
**Type:** 70% Normal, 30% VIP

### Serveurs (4 threads)
```text
┌──┐┌──┐┌──┐┌──┐
│S1││S2││S3││S4│
└──┘└──┘└──┘└──┘
```
**Rôle:** Prendre commandes

### Cuisiniers (3 threads)
```text
┌──┐┌──┐┌──┐
│C1││C2││C3│
└──┘└──┘└──┘
```
**Rôle:** Préparer plats

### Chef (1 thread)
```text
┌────┐
│CHEF│
└────┘
```
**Rôle:** Priorité sur commandes URGENTES

### Caissiers (2 threads)
```text
┌──┐┌──┐
│$1││$2│
└──┘└──┘
```
**Rôle:** Encaisser paiements

### Gestionnaire Stock (1 thread)
```text
┌──┐
│GS│
└──┘
```
**Rôle:** Réapprovisionner automatiquement

> [!NOTE]
> **TOTAL: 61 THREADS CONCURRENTS!**

---

## 🏆 POURQUOI C'EST CHALLENGEANT

- ✅ **61 threads concurrents** (vs 10-20 dans version simple)
- ✅ **4 niveaux sync différents** (wait, synchronized, lock, tryLock)
- ✅ **Deadlock RÉEL** (3+ ressources circulaires)
- ✅ **Race condition démontrée** (caisse sans/avec synchronized)
- ✅ **Système priorités** (3 niveaux de commandes)
- ✅ **Thread dédié stock** (tourne en arrière-plan)
- ✅ **Gestion timeout complexe** (tryLock multiples)
- ✅ **15+ fichiers organisés** (architecture propre)
- ✅ **Dashboard temps réel** (affichage concurrent)

---

## 📂 STRUCTURE FICHIERS

```text
restaurant/
│
├── models/
│   ├── Commande.java          (id, type, priorité, ingrédients)
│   ├── Table.java             (numero, type: VIP/Normal, occupée)
│   └── Plat.java              (nom, prix, temps, équipements requis)
│
├── modules/
│   ├── Module1_Tables/
│   │   ├── GestionnaireTables.java       (P1)
│   │   └── FileAttenteClients.java       (P1)
│   │
│   ├── Module2_Commandes/
│   │   └── FileCommandes.java            (P2)
│   │
│   ├── Module3_Equipements/
│   │   ├── GestionnaireEquipements.java  (P3)
│   │   └── DemoDeadlock.java             (P3)
│   │
│   └── Module4_CaisseStock/
│       ├── Caisse.java                   (P4)
│       └── GestionnaireStock.java        (P4)
│
├── threads/
│   ├── ClientThread.java                 (P4)
│   ├── ServeurThread.java                (P4)
│   ├── CuisinierThread.java              (P4)
│   ├── ChefThread.java                   (P4)
│   ├── CaissierThread.java               (P4)
│   └── StockThread.java                  (P4)
│
├── utils/
│   ├── Statistiques.java
│   └── Dashboard.java (affichage temps réel)
│
└── Restaurant.java (MAIN)                (P4)
```

---

## 📅 TIMELINE

### Semaine 1
- **Jours 1-3:** Développement modules individuels
- **Jour 4:** Tests unitaires
- **Jour 5:** Réunion intégration

### Semaine 2
- **Jours 6-8:** Intégration progressive
- **Jours 9-10:** Tests + corrections bugs
- **Jours 11-12:** Démos + dashboard
- **Jours 13-14:** Documentation + présentation

---

## 🎯 RÉSUMÉ RAPIDE

### 4 PERSONNES = 4 MODULES

1. **Tables** → wait/notify + ReentrantLock + tryLock
2. **Commandes** → wait/notify + synchronized + PriorityQueue
3. **Équipements** → ReentrantLock + tryLock + DEADLOCK démo/fix
4. **Caisse+Stock** → synchronized + wait/notify + Thread dédié

> [!IMPORTANT]
> **CHAQUE CONCEPT UTILISÉ NATURELLEMENT**
> **61 THREADS | 15+ FICHIERS | 1.5-2 SEMAINES**
