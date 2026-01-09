# Restaurant Management System - Project Management

## Quick Start for Engineers

### Step 1: Sync with Repository

```bash
git pull origin main
```

### Step 2: Find Your Task File

Each engineer has a personalized task file with detailed instructions:

| Engineer | Module | Task File | Priority |
|----------|--------|-----------|----------|
| **Walid** | Tables Management | `WALID_TABLES.md` | Start with Table.java |
| **Marwan** | Kitchen Equipment | `MARWAN_EQUIPMENT.md` | Start with deadlock demo |
| **Cranky** | Order Queue | `CRANKY_COMMANDS.md` | Start with Dish.java |
| **Saladin** | Finance & Stock | `SALADIN_FINANCE_STOCK.md` | Start with FinanceManager |

### Step 3: Read Your Task File

Your task file contains:
- Overview of your responsibilities
- All classes you need to implement
- Detailed algorithms with code examples
- Implementation order (what to do first)
- Integration points with other modules
- Testing requirements
- Checklist to track progress

### Step 4: Understand the Architecture

Read `TASK_DISTRIBUTION.md` for the overall system architecture using the Mediator pattern.

The key architectural files already exist:
- `src/main/java/ma/emsi/restaurant/Restaurant.java` (Mediator - Singleton)
- `src/main/java/ma/emsi/restaurant/Constants.java` (Shared constants)

Your code will fit into:
```
src/main/java/ma/emsi/restaurant/
├── entities/          # Data classes (POJOs)
├── managers/          # Monitors (synchronization logic)
└── actors/            # Threads (active behavior)
```

### Step 5: Create Your Branch (Optional but Recommended)

```bash
# Walid creates:
git checkout -b walid/tables-management

# Marwan creates:
git checkout -b marwan/equipment-management

# Cranky creates:
git checkout -b cranky/order-queue

# Saladin creates:
git checkout -b saladin/finance-stock
```

### Step 6: Start Implementation

Follow the "Implementation Order" section in your task file.

**Example for Walid:**
1. First: Complete `entities/Table.java` (30 min)
2. Then: Implement `managers/TableManager.java` (3 hours)
3. Finally: Create `actors/Client.java` (2 hours)

### Step 7: Use Stubs for Dependencies

If your module depends on another module that isn't ready yet, use stubs:

```java
// Example stub until Cranky's OrderQueue is ready
public void placeOrder() {
    System.out.println("Client placing order (STUB)");
    // TODO: Replace with actual call when OrderQueue ready
}
```

### Step 8: Test Your Module

Each task file has a "Tests to Implement" section. Write unit tests as you go.

### Step 9: Integration

Once all modules are ready (Day 4-5), we'll integrate through the `Restaurant` mediator:

```java
Restaurant restaurant = Restaurant.getInstance();
TableManager tables = restaurant.getTableManager();
OrderQueue orders = restaurant.getOrderQueue();
KitchenManager kitchen = restaurant.getKitchenManager();
// ... etc
```

---

## Development Timeline

### Week 1

**Days 1-3: Independent Module Development**
- Each engineer works on their module
- Use stubs for dependencies
- Focus on correctness of synchronization

**Day 4: Integration Prep**
- Remove stubs
- Test module interfaces
- Verify method signatures match

**Day 5: Full Integration**
- Connect all modules through Restaurant.java
- Test full system with all 61 threads
- Fix integration bugs

### Week 2 (if needed)

**Days 6-7: Testing & Debugging**
- Stress test with high loads
- Verify no deadlocks
- Verify no race conditions
- Performance tuning

**Days 8-9: Dashboard & Statistics**
- Real-time monitoring UI
- Statistics tracking

**Day 10: Final Demo & Documentation**

---

## Communication Protocol

### Daily Standup (10 minutes)

Each engineer answers:
1. What did you complete yesterday?
2. What are you working on today?
3. Any blockers or dependencies?

### Integration Dependencies

| Module | Depends On | Dependency Type |
|--------|------------|----------------|
| Client (Walid) | OrderQueue (Cranky) | Client places orders |
| Client (Walid) | FinanceManager (Saladin) | Client pays |
| Cook (Cranky) | KitchenManager (Marwan) | Cook needs equipment |
| Cook (Cranky) | StockManager (Saladin) | Cook needs ingredients |

**Important:** If you need another module's functionality, either:
1. Use a stub temporarily
2. Define the interface together (method signatures)
3. Coordinate timing for integration

---

## Code Quality Standards

### 1. Synchronization Rules

- **Always** release locks in `finally` blocks
- Use `synchronized` for simple mutual exclusion
- Use `ReentrantLock` when you need `tryLock()` or fine-grained control
- Document WHY you're using each synchronization mechanism

### 2. Thread Safety

- Mark shared mutable state as `volatile` if accessed outside synchronized blocks
- Use `synchronized` or locks for compound operations (read-modify-write)
- Never hold a lock while calling unknown code

### 3. Code Comments

Add comments for:
- Synchronization decisions (why synchronized here?)
- Deadlock prevention strategies
- Assumptions about thread safety
- Integration points with other modules

### 4. Error Handling

- Handle `InterruptedException` properly (restore interrupt flag)
- Validate inputs in public methods
- Log important events for debugging

---

## Testing Checklist (By Module)

### Walid (Tables)
- [ ] VIP clients get VIP tables first
- [ ] VIP timeout fallback works (30s)
- [ ] Clients wait when no tables available
- [ ] `notifyAll()` wakes all waiting clients
- [ ] No race conditions on table assignment

### Marwan (Equipment)
- [ ] Deadlock scenario demonstrated
- [ ] Prevention strategy eliminates deadlock
- [ ] `tryLock()` timeout works
- [ ] All locks released in finally
- [ ] Multiple cooks don't deadlock

### Cranky (Orders)
- [ ] PriorityQueue maintains priority order
- [ ] FIFO within same priority
- [ ] Cooks wait when queue empty
- [ ] `notify()` wakes exactly one cook
- [ ] Multiple servers don't corrupt queue

### Saladin (Finance & Stock)
- [ ] Race condition demonstrated (unsafe version)
- [ ] Synchronized version prevents race condition
- [ ] Stock consumption tracked correctly
- [ ] Stock replenishment triggers when low
- [ ] Background thread waits/notifies correctly

---

## Getting Help

1. **Check your task file first** - Most questions are answered there
2. **Read TASK_DISTRIBUTION.md** - For architectural questions
3. **Check Constants.java** - For shared configuration values
4. **Ask in team chat** - For clarifications or coordination
5. **Pair programming** - If stuck on synchronization logic

---

## Git Workflow

### Commit Frequently

```bash
git add .
git commit -m "Implement TableManager.requestTable() method"
git push origin your-branch-name
```

### Merge Strategy

When your module is ready:
```bash
git checkout main
git pull origin main
git merge your-branch-name
# Resolve any conflicts
git push origin main
```

---

## Good Luck!

Each engineer has a well-defined module with clear requirements. Follow your task file, test thoroughly, and communicate with the team. We'll have a working concurrent restaurant simulation in no time!

**Remember:** This project demonstrates concurrency concepts. Focus on:
- Correct synchronization
- Avoiding deadlocks
- Preventing race conditions
- Clear, understandable code

The goal is to showcase your understanding of concurrent programming, not to build the fastest or most complex system.
