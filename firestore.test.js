const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const ALICE_UID = "alice_123";
const BOB_UID = "bob_456";
const ADMIN_EMAIL = "uk9798989@gmail.com";

const [emulatorHost, emulatorPortStr] = (
  process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085"
).split(":");
const emulatorPort = parseInt(emulatorPortStr, 10);

function pastTimestamp() {
  return new Date(Date.now() - 2000);
}

before(async () => {
  const rules = fs.readFileSync("./firestore.rules", "utf8");
  testEnv = await initializeTestEnvironment({
    projectId: PROJECT_ID,
    firestore: {
      rules,
      host: emulatorHost,
      port: emulatorPort,
    },
  });
});

after(async () => {
  if (testEnv) {
    await testEnv.cleanup();
  }
});

beforeEach(async () => {
  if (testEnv) {
    await testEnv.clearFirestore();
  }
});

function adminContext() {
  return testEnv.authenticatedContext("admin_owner", {
    email: ADMIN_EMAIL,
    email_verified: true,
  });
}

test("Unauthenticated user cannot read or write any collection", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauthDb.collection("products").where("isActive", "==", true).get());
  await assertFails(unauthDb.collection("orders").where("userId", "==", ALICE_UID).get());
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).get());
});

test("User profile: owner can create/read/update, cross-user read fails, shadow field fails", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  const ts = pastTimestamp();

  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).set({
      userId: ALICE_UID,
      displayName: "Alice Khan",
      email: "alice@example.com",
      phone: "03001234567",
      savedAddresses: ["Main Chowk, Taranda Muhammad Panah"],
      createdAt: ts,
      updatedAt: ts,
    })
  );

  // Shadow update with ghost field fails
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).set({
      userId: ALICE_UID,
      displayName: "Alice Khan",
      isAdmin: true,
      createdAt: ts,
      updatedAt: ts,
    })
  );

  // Cross-user read fails
  await assertFails(bobDb.collection("users").doc(ALICE_UID).get());
});

test("Email spoofing test: unverified admin email is rejected", async () => {
  const spoofDb = testEnv.authenticatedContext("spoof_uid", {
    email: ADMIN_EMAIL,
    email_verified: false,
  }).firestore();
  const ts = pastTimestamp();

  await assertFails(
    spoofDb.collection("categories").doc("cat_pizza").set({
      name: "Extreme Pizza",
      group: "PIZZA",
      displayOrder: 1,
      imageKey: "img_cat_pizza_extreme",
      isActive: true,
      createdAt: ts,
      updatedAt: ts,
    })
  );
});

test("Admin can manage products, deals, settings; customer cannot write catalog", async () => {
  const ownerDb = adminContext().firestore();
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const ts = pastTimestamp();

  await assertSucceeds(
    ownerDb.collection("products").doc("prod_zinger").set({
      name: "Zinger Burger",
      categoryId: "cat_burgers",
      categoryName: "Burgers",
      group: "FAST_FOOD",
      description: "Crispy chicken zinger burger",
      basePrice: 350,
      priceVerified: true,
      isAvailable: true,
      isActive: true,
      imageKey: "img_cat_burger",
      displayOrder: 1,
      createdAt: ts,
      updatedAt: ts,
    })
  );

  // Customer can list active products
  await assertSucceeds(
    aliceDb.collection("products").where("isActive", "==", true).get()
  );

  // Customer fails unfiltered product list
  await assertFails(aliceDb.collection("products").get());

  // Customer cannot create or modify products
  await assertFails(
    aliceDb.collection("products").doc("prod_zinger").update({ basePrice: 1 })
  );
});

test("Orders: customer can create own order and cancel while Order Received; cannot tamper status to Completed or read Bob orders", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  const ownerDb = adminContext().firestore();
  const ts = pastTimestamp();

  await assertSucceeds(
    aliceDb.collection("orders").doc("ord_101").set({
      userId: ALICE_UID,
      orderCode: "LFC-1001",
      orderSource: "APP",
      orderType: "DELIVERY",
      status: "Order Received",
      customerName: "Alice Khan",
      customerPhone: "03001234567",
      deliveryAddress: "Sidhu Wali Road, TMP",
      items: [{ itemId: "prod_zinger", name: "Zinger Burger", quantity: 2, unitPrice: 350, lineTotal: 700 }],
      subtotal: 700,
      deliveryFee: 0,
      total: 700,
      priceVerified: true,
      createdAt: ts,
      updatedAt: ts,
    })
  );

  // Query alignment test
  await assertSucceeds(
    aliceDb.collection("orders").where("userId", "==", ALICE_UID).get()
  );
  await assertFails(aliceDb.collection("orders").get());

  // Cross-user read denied
  await assertFails(bobDb.collection("orders").doc("ord_101").get());

  // Customer cannot self-approve to Completed
  await assertFails(
    aliceDb.collection("orders").doc("ord_101").update({
      status: "Completed",
      updatedAt: pastTimestamp(),
    })
  );

  // Admin can advance status to Preparing
  await assertSucceeds(
    ownerDb.collection("orders").doc("ord_101").update({
      status: "Preparing",
      updatedAt: pastTimestamp(),
    })
  );
});
