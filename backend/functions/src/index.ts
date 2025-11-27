// functions/src/index.ts
// v2 para callables HTTPS:
import { onCall, HttpsError } from "firebase-functions/v2/https";
import { logger } from "firebase-functions";

// v1 explícito para el trigger de Auth:
import * as functionsV1 from "firebase-functions/v1";
import { UserRecord } from "firebase-functions/v1/auth";

import { initializeApp } from "firebase-admin/app";
import { getFirestore, Timestamp } from "firebase-admin/firestore";


// Init Admin SDK
initializeApp();
const db = getFirestore();

/** =========================
 *  AUTH: onCreate (perfil + métricas)
 *  ========================= */
export const authOnCreate = functionsV1.auth.user().onCreate(async (user: UserRecord) => {
  const { uid, email } = user;
  const now = new Date();
  
  // 1. Crear documento de perfil
  await db.doc(`users/${uid}/profile/data`).set({ // CORRECCIÓN: Agregado /data
    displayName: null,
    email: email ?? null,
    currency: "USD",
    locale: "en-US",
    marketingOptIn: false,
    createdAt: now,
  });
  
  // 2. Crear documento de métricas (FALLO AQUÍ ANTES)
  await db.doc(`users/${uid}/metrics/data`).set({ // CORRECCIÓN CLAVE: Agregado /data
    goalNearestId: null,
    goalNearestPct: 0,
    lastExpenseAt: null,
    lastIncomeAt: null,
  });

  // Crear categorías por defecto
  const defaultCategories = [
    { id: "food", name: "Food", icon: "🍔" },
    { id: "transport", name: "Transport", icon: "🚗" },
    { id: "bills", name: "Bills", icon: "💡" },
    { id: "shopping", name: "Shopping", icon: "🛍️" },
    { id: "other", name: "Other", icon: "📦" }
  ];

  const batch = db.batch();
  for (const cat of defaultCategories) {
    const docRef = db.collection(`users/${uid}/categories`).doc(cat.id);
    batch.set(docRef, { name: cat.name, icon: cat.icon });
  }
  await batch.commit();
});



/** =========================
 *  PROFILE
 *  ========================= */
export const updateProfile = onCall(async (req) => {
  const uid = req.auth?.uid; if (!uid) throw new HttpsError("unauthenticated", "User must be logged in.");
  const { displayName, currency, locale, marketingOptIn } = req.data || {};
  
  // 3. CORRECCIÓN: Apunta al documento /profile/data
  await db.doc(`users/${uid}/profile/data`).set({
    ...(displayName !== undefined && { displayName }),
    ...(currency !== undefined && { currency }),
    ...(locale !== undefined && { locale }),
    ...(marketingOptIn !== undefined && { marketingOptIn }),
  }, { merge: true });
  return { ok: true };
});

/** =========================
 *  CATEGORY
 *  ========================= */
export const createCategory = onCall(async (req) => {
  const uid = req.auth?.uid; if (!uid) throw new HttpsError("unauthenticated", "User must be logged in.");
  const { name, parentId, icon } = req.data || {};
  if (!name) throw new HttpsError("invalid-argument", "'name' is required.");
  const ref = await db.collection("users").doc(uid).collection("categories")
    .add({ name, parentId: parentId ?? null, icon: icon ?? null });
  return { categoryId: ref.id };
});

/** =========================
 *  EXPENSE
 *  ========================= */
export const createExpense = onCall(async (req) => {
  const uid = req.auth?.uid;
  if (!uid) throw new HttpsError("unauthenticated", "User must be logged in.");

  logger.info("createExpense called", { uid, data: req.data });

  const { amount, currency, categoryId, note, description, date, receiptImageUrl } = req.data || {};

  if (!(amount > 0) || !currency || !categoryId) {
    logger.error("Invalid arguments for createExpense", { uid, data: req.data });
    throw new HttpsError("invalid-argument", "amount, currency, and categoryId are required.");
  }

  const timestamp = date
    ? Timestamp.fromDate(new Date(date))
    : Timestamp.now();

  const ref = await db.collection("users").doc(uid).collection("expenses")
    .add({
      amount,
      currency,
      categoryId,
      note: note ?? null,
      description: description ?? null,
      receiptImageUrl: receiptImageUrl ?? null,
      ts: timestamp
    });

  // 4. CORRECCIÓN CLAVE: Apunta al documento /metrics/data
  await db.doc(`users/${uid}/metrics/data`).set({
    lastExpenseAt: timestamp
  }, { merge: true });

  logger.info("createExpense success", { expenseId: ref.id });
  return { expenseId: ref.id };
});

/** =========================
 *  INCOME
 *  ========================= */
export const createIncome = onCall(async (req) => {
  const uid = req.auth?.uid;
  if (!uid) throw new HttpsError("unauthenticated", "User must be logged in.");

  logger.info("createIncome called", { uid, data: req.data });

  const { amount, currency, source, description, date } = req.data || {};

  if (!(amount > 0) || !currency) {
    logger.error("Invalid arguments for createIncome", { uid, data: req.data });
    throw new HttpsError("invalid-argument", "amount and currency are required.");
  }

  const timestamp = date
    ? Timestamp.fromDate(new Date(date))
    : Timestamp.now();

  const ref = await db.collection("users").doc(uid).collection("incomes")
    .add({
      amount,
      currency,
      source: source ?? null,
      description: description ?? null,
      ts: timestamp
    });

  // 5. CORRECCIÓN CLAVE: Apunta al documento /metrics/data
  await db.doc(`users/${uid}/metrics/data`).set({
    lastIncomeAt: timestamp
  }, { merge: true });
  
  logger.info("createIncome success", { incomeId: ref.id });
  return { incomeId: ref.id };
});

/** =========================
 *  GOALS
 *  ========================= */
export const createGoal = onCall(async (req) => {
  const uid = req.auth?.uid; if (!uid) throw new HttpsError("unauthenticated", "User must be logged in.");
  const { name, targetAmount, currency, deadline, priority } = req.data || {};
  if (!name || !(targetAmount > 0) || !currency) throw new HttpsError("invalid-argument", "name, targetAmount, and currency are required.");
  const ref = await db.collection("users").doc(uid).collection("goals").add({
    name, targetAmount, currency,
    deadline: deadline ? Timestamp.fromDate(new Date(deadline)) : null,
    priority: priority ?? 0,
  });
  return { goalId: ref.id };
});

export const updateGoalProgress = onCall(async (req) => {
  const uid = req.auth?.uid; if (!uid) throw new HttpsError("unauthenticated", "User must be logged in.");
  const { goalId, pct, deltaAmount } = req.data || {};
  if (!goalId || pct < 0 || pct > 100) throw new HttpsError("invalid-argument", "goalId and pct are required.");
  const pRef = db.doc(`users/${uid}/goals/${goalId}/progress/${db.collection('_').doc().id}`);
  await pRef.set({ pct, deltaAmount: deltaAmount ?? null, ts: Timestamp.now() });
  await updateNearestGoalMetrics(uid);
  return { ok: true };
});

async function updateNearestGoalMetrics(uid: string) {
  const goalsSnap = await db.collection(`users/${uid}/goals`).get();
  let nearest: { id: string, pct: number, deadline?: Timestamp | null } | null = null;
  for (const g of goalsSnap.docs) {
    const progSnap = await g.ref.collection("progress").orderBy("ts", "desc").limit(1).get();
    const pct = progSnap.empty ? 0 : (progSnap.docs[0].get("pct") as number);
    const deadline = g.get("deadline") as Timestamp | null;
    if (!nearest) nearest = { id: g.id, pct, deadline };
    else {
      const dA = nearest.deadline?.toMillis() ?? Number.MAX_SAFE_INTEGER;
      const dB = deadline?.toMillis() ?? Number.MAX_SAFE_INTEGER;
      if (dB < dA) nearest = { id: g.id, pct, deadline };
    }
  }
  // 6. CORRECCIÓN CLAVE: Apunta al documento /metrics/data
  await db.doc(`users/${uid}/metrics/data`).set({
    goalNearestId: nearest?.id ?? null,
    goalNearestPct: nearest?.pct ?? 0,
  }, { merge: true });
}

/** =========================
 *  SUMMARIES
 *  ========================= */
export const getMonthlySummary = onCall(async (req) => {
  const uid = req.auth?.uid;
  if (!uid) {
    throw new HttpsError("unauthenticated", "User must be logged in.");
  }
  
  const { month } = req.data || {}; // YYYY-MM
  logger.info("getMonthlySummary called", { uid, month });

  if (!month || !/\d{4}-\d{2}/.test(month as string)) {
    throw new HttpsError("invalid-argument", "'month' is required and must be in YYYY-MM format.");
  }

  const [y, m] = (month as string).split("-").map(Number);
  const start = Timestamp.fromDate(new Date(Date.UTC(y, m - 1, 1)));
  const end = Timestamp.fromDate(new Date(Date.UTC(y, m, 1)));
  
  logger.info(`Querying ${month} for user ${uid}`, { start: start.toDate().toISOString(), end: end.toDate().toISOString() });

  try {
    const exSnap = await db.collection(`users/${uid}/expenses`)
      .where("ts", ">=", start).where("ts", "<", end).get();
    const incSnap = await db.collection(`users/${uid}/incomes`)
      .where("ts", ">=", start).where("ts", "<", end).get();

    let totalExpenses = 0, totalIncome = 0;
    const byCategory: Record<string, number> = {};
    exSnap.forEach(d => {
      const a = d.get("amount") as number;
      totalExpenses += a;
      const c = d.get("categoryId") as string;
      byCategory[c] = (byCategory[c] || 0) + a;
    });
    incSnap.forEach(d => { totalIncome += (d.get("amount") as number); });
    
    const result = {
      totalExpenses,
      totalIncome,
      byCategory: Object.entries(byCategory).map(([categoryId, total]) => ({ categoryId, total }))
    };

    logger.info("getMonthlySummary success", { result });
    return result;

  } catch (error: any) {
    logger.error("GET_MONTHLY_SUMMARY_FAILED", { uid, month, error: error.message });
    if (error.message && error.message.includes("requires an index")) {
      throw new HttpsError('failed-precondition', `Query requires an index. Check logs for the creation URL.`);
    } 
    throw new HttpsError('internal', 'An unexpected error occurred while fetching the summary.');
  }
});

export const getGoalProgressSummary = onCall(async (req) => {
  const uid = req.auth?.uid; if (!uid) throw new HttpsError("unauthenticated", "User must be logged in.");
  const { goalId } = req.data || {};
  const gRef = goalId
    ? db.doc(`users/${uid}/goals/${goalId}`)
    : null;

  const gDoc = gRef
    ? await gRef.get()
    : (await db.collection(`users/${uid}/goals`).orderBy("priority", "desc").limit(1).get()).docs[0];

  if (!gDoc) throw new HttpsError("not-found", "No goal found for this user.");
  const progSnap = await gDoc.ref.collection("progress").orderBy("ts", "desc").limit(1).get();
  const pct = progSnap.empty ? 0 : (progSnap.docs[0].get("pct") as number);
  const targetAmount = gDoc.get("targetAmount") as number;
  const deadline = gDoc.get("deadline") as Timestamp | null;

  let daysLeft: number | undefined = undefined;
  if (deadline) {
    const msLeft = deadline.toMillis() - Date.now();
    daysLeft = Math.max(0, Math.ceil(msLeft / (1000 * 60 * 60 * 24)));
  }
  const suggestedDaily = daysLeft ? ((100 - pct) / 100 * targetAmount / daysLeft) : 0;
  return { goalId: gDoc.id, pct, daysLeft, targetAmount, suggestedDaily };
});
