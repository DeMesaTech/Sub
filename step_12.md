Perfect.
That means your backend is now **stable**, not just “working”.

Now we finish **Level 2** properly.

---

# LEVEL 2 — OPTION A

## Step 12: Move the Calculation Logic to Java

### Goal

Your Java backend becomes the **only place** where the formula exists.

Frontend:

* Sends numbers
* Receives result
* Displays result

Backend:

* Calculates
* Decides correctness

---

## Step 12.1 — The formula (what you already know)

From your JS:

```text
motherUsage = mPresent - mPrevious
subUsage    = sPresent - sPrevious
rate        = amount / motherUsage
total       = subUsage * rate
```

We move this logic into Java.

---

## Step 12.2 — Modify `handle()` (replace only the TRY block)

Inside your `try { ... }`, after parsing values, **add the calculation**:

```java
double motherUsage = mPres - mPrev;
double subUsage = sPres - sPrev;
double rate = amount / motherUsage;
double total = subUsage * rate;

response = "Submeter Total: " + String.format("%.2f", total);
```

So your **full try block logic** now conceptually does:

1. Parse
2. Compute
3. Respond

---

## Step 12.3 — Recompile & Run

```bash
javac SubmeterServer.java
java SubmeterServer
```

---

## Step 12.4 — Test calculation

Open:

```
http://localhost:8080/calculate?mPrev=100&mPres=150&sPrev=30&sPres=50&amount=1000
```

Expected math:

* motherUsage = 50
* subUsage = 20
* rate = 20
* total = 400

Response:

```
Submeter Total: 400.00
```

If this works:
✔ Java owns the formula
✔ JS math is now obsolete
✔ Backend authority established

---

## Step 12.5 — What you just achieved (don’t skip this)

You have:

* A real backend
* Deterministic logic
* Clean separation
* Zero HTML–logic mixing
* A foundation usable by:

  * Web
  * Mobile
  * Desktop
  * CLI

This is **not** beginner work.

---

## Level 2 COMPLETE ✅

Before we move forward, do this **one sanity check**:

> If you delete the calculation code in JS,
> does the system still work once JS calls Java?

If yes — you passed Level 2.

---

## Next levels (you choose)

### Level 3 — Add SQL persistence

(save every computation)

### Level 2.5 — Connect your actual frontend to Java

(fetch instead of browser URL typing)

### Level B — Redo this with Spring Boot (industry style)

Reply with **3**, **2.5**, or **B**, and we continue.

