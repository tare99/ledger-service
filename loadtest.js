import http from "k6/http";
import {check, group, sleep} from "k6";
import {Counter, Trend} from "k6/metrics";

const paymentCreated = new Counter("payments_created");
const paymentFailed = new Counter("payments_failed");
const refundCount = new Counter("refunds_issued");
const createPaymentDuration = new Trend("create_payment_duration", true);
const getPaymentDuration = new Trend("get_payment_duration", true);
const listPaymentsDuration = new Trend("list_payments_duration", true);

// ─── Config ──────────────────────────────────────────────────────────
const BASE_URL = __ENV.BASE_URL || "http://localhost:8080";

const ACCOUNTS = [
  "ACC-ALICE00000000001",
  "ACC-BOB000000000002",
  "ACC-CAROL00000000003",
  "ACC-DAVE00000000004",
];

export const options = {
  scenarios: {
    payment_flow: {
      executor: "ramping-vus",
      startVUs: 0,
      stages: [
        {duration: "30s", target: 20},  // ramp up
        {duration: "1m", target: 20},   // sustained load
        {duration: "15s", target: 50},  // spike
        {duration: "30s", target: 50},  // hold spike
        {duration: "15s", target: 20},  // back to normal
        {duration: "30s", target: 0},   // ramp down
      ],
      exec: "paymentFlow",
    },
    read_traffic: {
      executor: "ramping-vus",
      startVUs: 0,
      stages: [
        {duration: "30s", target: 30},
        {duration: "1m", target: 30},
        {duration: "15s", target: 60},
        {duration: "30s", target: 60},
        {duration: "15s", target: 30},
        {duration: "30s", target: 0},
      ],
      exec: "readTraffic",
    },
  },
  thresholds: {
    http_req_duration: ["p(95)<500", "p(99)<1000"],
    http_req_failed: ["rate<0.01"],
    create_payment_duration: ["p(95)<400"],
    get_payment_duration: ["p(95)<200"],
    list_payments_duration: ["p(95)<300"],
  },
};

const headers = {"Content-Type": "application/json"};

function pickRandom(arr) {
  return arr[Math.floor(Math.random() * arr.length)];
}

function pickSenderReceiver() {
  const sender = pickRandom(ACCOUNTS);
  let receiver;
  do {
    receiver = pickRandom(ACCOUNTS);
  } while (receiver === sender);
  return {sender, receiver};
}

function randomAmount() {
  const r = Math.random();
  if (r < 0.7) {
    return (Math.random() * 50 + 1).toFixed(2);
  }
  if (r < 0.95) {
    return (Math.random() * 500 + 50).toFixed(2);
  }
  return (Math.random() * 2000 + 500).toFixed(2);
}

export function setup() {
  const apiKeys = {};

  for (const account of ACCOUNTS) {
    const res = http.post(
        `${BASE_URL}/api/v1/auth/api-keys`,
        JSON.stringify({name: `loadtest-${account}`, accountNumber: account}),
        {headers}
    );

    if (res.status === 201) {
      apiKeys[account] = res.json().apiKey;
    } else {
      console.error(
          `Failed to create API key for ${account}: ${res.status} ${res.body}`);
    }
  }

  console.log(
      `Setup complete: ${Object.keys(apiKeys).length} API keys created`);
  return {apiKeys};
}

export function paymentFlow(data) {
  const {sender, receiver} = pickSenderReceiver();
  const apiKey = data.apiKeys[sender];
  if (!apiKey) {
    return;
  }

  const authHeaders = {
    ...headers,
    Authorization: `Bearer ${apiKey}`,
  };

  let paymentId;

  group("create_payment", () => {
    const payload = JSON.stringify({
      requestId: `lt-${__VU}-${__ITER}-${Date.now()}`,
      senderAccountId: sender,
      receiverAccountId: receiver,
      amount: parseFloat(randomAmount()),
      currency: "USD",
    });

    const res = http.post(`${BASE_URL}/api/v1/payments`, payload, {
      headers: authHeaders,
    });

    createPaymentDuration.add(res.timings.duration);

    const ok = check(res, {
      "payment created (201)": (r) => r.status === 201,
      "has paymentId": (r) => r.json("paymentId") !== undefined,
    });

    if (ok) {
      paymentCreated.add(1);
      paymentId = res.json("paymentId");
    } else {
      paymentFailed.add(1);
    }
  });

  sleep(Math.random() * 0.5 + 0.1);

  if (!paymentId) {
    return;
  }

  group("get_payment", () => {
    const res = http.get(`${BASE_URL}/api/v1/payments/${paymentId}`, {
      headers: authHeaders,
    });

    getPaymentDuration.add(res.timings.duration);

    check(res, {
      "payment retrieved (200)": (r) => r.status === 200,
      "correct paymentId": (r) => r.json("paymentId") === paymentId,
    });
  });

  group("get_status", () => {
    const res = http.get(`${BASE_URL}/api/v1/payments/${paymentId}/status`, {
      headers: authHeaders,
    });

    check(res, {
      "status retrieved (200)": (r) => r.status === 200,
    });
  });

  if (Math.random() < 0.1) {
    sleep(Math.random() * 0.3);

    group("refund_payment", () => {
      const res = http.post(
          `${BASE_URL}/api/v1/payments/${paymentId}/refund`,
          null,
          {headers: authHeaders}
      );

      check(res, {
        "refund successful (200)": (r) => r.status === 200,
        "status is REFUNDED": (r) => r.json("status") === "REFUNDED",
      });

      if (res.status === 200) {
        refundCount.add(1);
      }
    });
  }

  sleep(Math.random() + 0.5);
}

export function readTraffic(data) {
  const account = pickRandom(ACCOUNTS);
  const apiKey = data.apiKeys[account];
  if (!apiKey) {
    return;
  }

  const authHeaders = {
    ...headers,
    Authorization: `Bearer ${apiKey}`,
  };

  const action = Math.random();

  if (action < 0.4) {
    group("list_payments", () => {
      const page = Math.floor(Math.random() * 3);
      const res = http.get(
          `${BASE_URL}/api/v1/payments?page=${page}&size=10`,
          {headers: authHeaders}
      );

      listPaymentsDuration.add(res.timings.duration);

      check(res, {
        "list returned (200)": (r) => r.status === 200,
        "has payments array": (r) => Array.isArray(r.json("payments")),
        "has pagination": (r) => r.json("pagination") !== undefined,
      });
    });
  } else if (action < 0.65) {
    group("list_by_sender", () => {
      const res = http.get(
          `${BASE_URL}/api/v1/payments?senderAccountId=${account}&page=0&size=10`,
          {headers: authHeaders}
      );

      listPaymentsDuration.add(res.timings.duration);

      check(res, {
        "filtered list returned (200)": (r) => r.status === 200,
      });
    });
  } else if (action < 0.85) {
    group("list_by_receiver", () => {
      const res = http.get(
          `${BASE_URL}/api/v1/payments?receiverAccountId=${account}&page=0&size=10`,
          {headers: authHeaders}
      );

      listPaymentsDuration.add(res.timings.duration);

      check(res, {
        "filtered list returned (200)": (r) => r.status === 200,
      });
    });
  } else {
    group("list_by_status", () => {
      const status = pickRandom(["COMPLETED", "REFUNDED"]);
      const res = http.get(
          `${BASE_URL}/api/v1/payments?status=${status}&page=0&size=10`,
          {headers: authHeaders}
      );

      listPaymentsDuration.add(res.timings.duration);

      check(res, {
        "status-filtered list returned (200)": (r) => r.status === 200,
      });
    });
  }

  sleep(Math.random() * 1.5 + 0.5);
}