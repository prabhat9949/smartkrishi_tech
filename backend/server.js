"use strict";

require("dotenv").config();

const express = require("express");
const admin = require("firebase-admin");
const twilio = require("twilio");
const fs = require("fs");

// ======================================================
// ENV CHECK
// ======================================================
const requiredEnv = [
  "FIREBASE_DATABASE_URL",
  "TWILIO_ACCOUNT_SID",
  "TWILIO_AUTH_TOKEN",
  "TWILIO_WHATSAPP_FROM",
  "PORT"
];

for (const key of requiredEnv) {
  if (!process.env[key]) {
    console.error(`❌ Missing environment variable: ${key}`);
    process.exit(1);
  }
}

// ======================================================
// FIREBASE INIT
// ======================================================
let firebaseCredential;

if (process.env.FIREBASE_SERVICE_ACCOUNT_JSON) {
  firebaseCredential = admin.credential.cert(
    JSON.parse(process.env.FIREBASE_SERVICE_ACCOUNT_JSON)
  );
} else if (process.env.FIREBASE_SERVICE_ACCOUNT_PATH) {
  const serviceAccountPath = process.env.FIREBASE_SERVICE_ACCOUNT_PATH;
  if (!fs.existsSync(serviceAccountPath)) {
    console.error(`❌ Firebase service account file not found: ${serviceAccountPath}`);
    process.exit(1);
  }
  firebaseCredential = admin.credential.cert(require(serviceAccountPath));
} else {
  console.error("❌ Set FIREBASE_SERVICE_ACCOUNT_JSON or FIREBASE_SERVICE_ACCOUNT_PATH");
  process.exit(1);
}

admin.initializeApp({
  credential: firebaseCredential,
  databaseURL: process.env.FIREBASE_DATABASE_URL
});

const db = admin.database();

// ======================================================
// TWILIO INIT
// ======================================================
const client = twilio(
  process.env.TWILIO_ACCOUNT_SID,
  process.env.TWILIO_AUTH_TOKEN
);

// ======================================================
// CONFIG
// ======================================================
const app = express();
app.use(express.urlencoded({ extended: false }));
app.use(express.json());

const PORT = Number(process.env.PORT || 3000);

const ROOT_PATH =
  process.env.FIREBASE_LIVE_PATH ||
  "/dashboard/farmer_SK001/farm_alpha01/live";
const USER_PHONE =
  process.env.USER_WHATSAPP_TO || "whatsapp:+919199121678";
const DEFAULT_LANGUAGE = (process.env.DEFAULT_LANGUAGE || "en").toLowerCase();

const THRESHOLDS = {
  moistureLow: Number(process.env.MOISTURE_LOW || 30),
  temperatureHigh: Number(process.env.TEMP_HIGH || 40),
  phLow: Number(process.env.PH_LOW || 5.5),
  phHigh: Number(process.env.PH_HIGH || 8.5),
  tankLow: Number(process.env.TANK_LOW || 0),
  tdsLow: Number(process.env.TDS_LOW || 0),
  tdsHigh: Number(process.env.TDS_HIGH || 1500),
  humidityLow: Number(process.env.HUMIDITY_LOW || 20),
  humidityHigh: Number(process.env.HUMIDITY_HIGH || 90),
  ecLow: Number(process.env.EC_LOW || 0),
  ecHigh: Number(process.env.EC_HIGH || 3)
};

const SETTINGS_PATH = "/serverConfig/whatsapp";
const LAST_ALERTS_PATH = `${SETTINGS_PATH}/lastAlerts`;
const COMMAND_LOG_PATH = `${SETTINGS_PATH}/commands`;

// IMPORTANT FIELDS WE CARE ABOUT
const WATCH_FIELDS = [
  "irrigation.pump",
  "irrigation.rain",
  "irrigation.tank",
  "irrigation.tds",
  "nodes.zone_1.moisture",
  "nodes.zone_1.temperature",
  "nodes.zone_1.ph",
  "nodes.zone_1.humidity",
  "nodes.zone_1.ec"
];

let lastData = null;
let currentLanguage = DEFAULT_LANGUAGE;
let isInitialLoadComplete = false;

// ======================================================
// LABELS / MESSAGES
// ======================================================
const TEXT = {
  en: {
    appTitle: "SmartKrishi Alert",
    at: "Time",
    langSet: "WhatsApp language updated to English.",
    help:
      "SmartKrishi commands:\n" +
      "- english / en\n" +
      "- hindi / hi / हिंदी / हिन्दी\n" +
      "- status\n" +
      "- pump on\n" +
      "- pump off\n" +
      "- help\n\n" +
      "Note: Sensor values and thresholds cannot be changed via WhatsApp.",
    commandFailed: "Command could not be completed.",
    invalidCommand: "Invalid command. Reply 'help' to see all commands.",
    pumpOn: "Pump turned ON.",
    pumpOff: "Pump turned OFF.",
    changedOnly: "Changed values",
    alerts: "Alerts",
    none: "None",
    statusActionRequired: "Status: Action required",
    statusNormal: "Status: Normal",
    setNotAllowed:
      "Changing sensor values via WhatsApp is not allowed. Please use the app or dashboard.",
    statusReplyTitle: "Current live status",
    moistureLow: "Soil moisture is LOW",
    temperatureHigh: "Temperature is HIGH",
    phOut: "Soil pH is OUT OF RANGE",
    tankEmpty: "Water tank is EMPTY",
    backNormal: "Back to normal",
    replyLanguagePrompt:
      "Reply with ENGLISH or HINDI to change WhatsApp language only."
  },
  hi: {
    appTitle: "SmartKrishi अलर्ट",
    at: "समय",
    langSet: "WhatsApp भाषा हिंदी में बदल दी गई है।",
    help:
      "SmartKrishi कमांड्स:\n" +
      "- english / en\n" +
      "- hindi / hi / हिंदी / हिन्दी\n" +
      "- status\n" +
      "- pump on\n" +
      "- pump off\n" +
      "- help\n\n" +
      "ध्यान दें: सेंसर वैल्यू और thresholds WhatsApp से नहीं बदले जा सकते।",
    commandFailed: "कमांड पूरी नहीं हो सकी।",
    invalidCommand: "अमान्य कमांड। सभी कमांड देखने के लिए 'help' भेजें।",
    pumpOn: "पंप चालू किया गया।",
    pumpOff: "पंप बंद किया गया।",
    changedOnly: "बदली हुई वैल्यू",
    alerts: "अलर्ट",
    none: "कोई नहीं",
    statusActionRequired: "स्थिति: कार्रवाई आवश्यक",
    statusNormal: "स्थिति: सामान्य",
    setNotAllowed:
      "WhatsApp से सेंसर वैल्यू बदलना संभव नहीं है। कृपया ऐप या डैशबोर्ड का उपयोग करें।",
    statusReplyTitle: "वर्तमान लाइव स्थिति",
    moistureLow: "मिट्टी की नमी कम है",
    temperatureHigh: "तापमान अधिक है",
    phOut: "मिट्टी का pH सीमा से बाहर है",
    tankEmpty: "पानी की टंकी खाली है",
    backNormal: "फिर से सामान्य",
    replyLanguagePrompt:
      "भाषा बदलने के लिए ENGLISH या HINDI भेजें। यह बदलाव केवल WhatsApp के लिए होगा।"
  }
};

function t(lang, key) {
  const language = TEXT[lang] ? lang : "en";
  return TEXT[language][key] || TEXT.en[key] || key;
}

// ======================================================
// HELPERS
// ======================================================
function normalizeLanguage(value) {
  const v = String(value || "").trim().toLowerCase();
  if (["hi", "hindi", "हिंदी", "हिन्दी"].includes(v)) return "hi";
  if (["en", "english"].includes(v)) return "en";
  return "en";
}

function isHindiCommand(cmd) {
  return ["hi", "hindi", "हिंदी", "हिन्दी"].includes(cmd);
}

function isEnglishCommand(cmd) {
  return ["en", "english"].includes(cmd);
}

async function loadLanguage() {
  currentLanguage = normalizeLanguage(currentLanguage || DEFAULT_LANGUAGE);
}

async function saveLanguage(lang) {
  currentLanguage = normalizeLanguage(lang);
}

function deepClone(obj) {
  return JSON.parse(JSON.stringify(obj || {}));
}

function isNumeric(val) {
  return typeof val === "number" && !Number.isNaN(val);
}

function boolToWord(value, type, lang) {
  if (type === "pump") {
    if (lang === "hi") return value == 1 ? "चालू" : "बंद";
    return value == 1 ? "ON" : "OFF";
  }
  if (type === "rain") {
    if (lang === "hi") return value == 1 ? "बारिश हो रही है" : "बारिश नहीं";
    return value == 1 ? "RAINING" : "NO RAIN";
  }
  if (type === "tank") {
    if (lang === "hi") return value == 1 ? "भरी" : "खाली";
    return value == 1 ? "FULL" : "EMPTY";
  }
  return String(value);
}

function getValueAtPath(obj, path) {
  return path.split(".").reduce((acc, key) => (acc ? acc[key] : undefined), obj);
}

function humanLabel(path, lang) {
  const isHi = lang === "hi";
  const map = {
    "irrigation.pump": isHi ? "पंप" : "Pump",
    "irrigation.rain": isHi ? "बारिश सेंसर" : "Rain sensor",
    "irrigation.tank": isHi ? "टंकी" : "Tank",
    "irrigation.tds": "TDS",
    "nodes.zone_1.moisture": isHi ? "मिट्टी की नमी" : "Soil moisture",
    "nodes.zone_1.temperature": isHi ? "तापमान" : "Temperature",
    "nodes.zone_1.ph": "pH",
    "nodes.zone_1.humidity": isHi ? "आर्द्रता" : "Humidity",
    "nodes.zone_1.ec": "EC"
  };
  return map[path] || path;
}

function formatValue(path, value, lang) {
  if (value === undefined || value === null) return "N/A";
  if (path === "irrigation.pump") return boolToWord(value, "pump", lang);
  if (path === "irrigation.rain") return boolToWord(value, "rain", lang);
  if (path === "irrigation.tank") return boolToWord(value, "tank", lang);
  if (path.endsWith("temperature")) return `${value}°C`;
  if (path.endsWith("moisture")) return `${value}%`;
  if (path.endsWith("humidity")) return `${value}%`;
  if (path.endsWith("ec")) return `${value} mS/cm`;
  if (path.endsWith("tds")) return `${value} ppm`;
  return String(value);
}

function diffChangedFields(before, after) {
  const changes = [];
  for (const path of WATCH_FIELDS) {
    const oldVal = getValueAtPath(before, path);
    const newVal = getValueAtPath(after, path);
    if (JSON.stringify(oldVal) !== JSON.stringify(newVal)) {
      changes.push({ path, before: oldVal, after: newVal });
    }
  }
  return changes;
}

function encodeKey(key) {
  return String(key).replace(/\./g, ",");
}

function decodeKey(key) {
  return String(key).replace(/,/g, ".");
}

async function getLastAlertState() {
  try {
    const snap = await db.ref(LAST_ALERTS_PATH).get();
    if (!snap.exists()) return {};
    const raw = snap.val() || {};
    const decoded = {};
    Object.keys(raw).forEach(encodedKey => {
      decoded[decodeKey(encodedKey)] = !!raw[encodedKey];
    });
    return decoded;
  } catch {
    return {};
  }
}

async function setLastAlertState(state) {
  try {
    const encoded = {};
    Object.keys(state || {}).forEach(originalKey => {
      encoded[encodeKey(originalKey)] = !!state[originalKey];
    });
    await db.ref(LAST_ALERTS_PATH).set(encoded);
  } catch {
    // ignore
  }
}

function evaluateThresholds(data, lang, previousAlertState = {}) {
  const alerts = [];
  const newAlertState = { ...previousAlertState };

  const checks = [
    {
      key: "nodes.zone_1.moisture.low",
      path: "nodes.zone_1.moisture",
      isActive:
        isNumeric(getValueAtPath(data, "nodes.zone_1.moisture")) &&
        getValueAtPath(data, "nodes.zone_1.moisture") < THRESHOLDS.moistureLow,
      msgLow: t(lang, "moistureLow")
    },
    {
      key: "nodes.zone_1.temperature.high",
      path: "nodes.zone_1.temperature",
      isActive:
        isNumeric(getValueAtPath(data, "nodes.zone_1.temperature")) &&
        getValueAtPath(data, "nodes.zone_1.temperature") >
          THRESHOLDS.temperatureHigh,
      msgLow: t(lang, "temperatureHigh")
    },
    {
      key: "nodes.zone_1.ph.out",
      path: "nodes.zone_1.ph",
      isActive:
        isNumeric(getValueAtPath(data, "nodes.zone_1.ph")) &&
        (getValueAtPath(data, "nodes.zone_1.ph") < THRESHOLDS.phLow ||
          getValueAtPath(data, "nodes.zone_1.ph") > THRESHOLDS.phHigh),
      msgLow: t(lang, "phOut")
    },
    {
      key: "irrigation.tank.empty",
      path: "irrigation.tank",
      isActive: Number(getValueAtPath(data, "irrigation.tank")) === 0,
      msgLow: t(lang, "tankEmpty")
    }
  ];

  for (const check of checks) {
    const was = !!previousAlertState[check.key];
    const is = !!check.isActive;

    if (!was && is) {
      alerts.push({
        type: "alert_on",
        path: check.path,
        line: `${check.msgLow} (${humanLabel(check.path, lang)}: ${formatValue(
          check.path,
          getValueAtPath(data, check.path),
          lang
        )})`
      });
    } else if (was && !is) {
      alerts.push({
        type: "alert_off",
        path: check.path,
        line: `${humanLabel(check.path, lang)}: ${t(lang, "backNormal")} (${formatValue(
          check.path,
          getValueAtPath(data, check.path),
          lang
        )})`
      });
    }

    newAlertState[check.key] = is;
  }

  return { alerts, newAlertState };
}

function buildStatusSummary(data, lang) {
  const irrigation = data.irrigation || {};
  const zone1 = data.nodes?.zone_1 || {};

  const lines = [
    `• ${humanLabel("irrigation.pump", lang)}: ${formatValue(
      "irrigation.pump",
      irrigation.pump,
      lang
    )}`,
    `• ${humanLabel("irrigation.tank", lang)}: ${formatValue(
      "irrigation.tank",
      irrigation.tank,
      lang
    )}`,
    `• ${humanLabel("nodes.zone_1.moisture", lang)}: ${formatValue(
      "nodes.zone_1.moisture",
      zone1.moisture,
      lang
    )}`,
    `• ${humanLabel("nodes.zone_1.temperature", lang)}: ${formatValue(
      "nodes.zone_1.temperature",
      zone1.temperature,
      lang
    )}`,
    `• ${humanLabel("nodes.zone_1.ph", lang)}: ${formatValue(
      "nodes.zone_1.ph",
      zone1.ph,
      lang
    )}`
  ];

  if (zone1.humidity !== undefined) {
    lines.push(
      `• ${humanLabel("nodes.zone_1.humidity", lang)}: ${formatValue(
        "nodes.zone_1.humidity",
        zone1.humidity,
        lang
      )}`
    );
  }
  if (zone1.ec !== undefined) {
    lines.push(
      `• ${humanLabel("nodes.zone_1.ec", lang)}: ${formatValue(
        "nodes.zone_1.ec",
        zone1.ec,
        lang
      )}`
    );
  }
  if (irrigation.tds !== undefined) {
    lines.push(
      `• ${humanLabel("irrigation.tds", lang)}: ${formatValue(
        "irrigation.tds",
        irrigation.tds,
        lang
      )}`
    );
  }

  return lines.join("\n");
}

function buildUnifiedMessage({ changes, alerts, currentData, lang }) {
  const title =
    lang === "hi"
      ? "🚜 SmartKrishi खेत अलर्ट"
      : "🚜 SmartKrishi Farm Alert";

  const statusLabel =
    alerts.length > 0
      ? t(lang, "statusActionRequired")
      : t(lang, "statusNormal");

  const changedLines = changes.map(change => {
    const label = humanLabel(change.path, lang);
    const prev = formatValue(change.path, change.before, lang);
    const curr = formatValue(change.path, change.after, lang);
    return `• ${label}: ${prev} → ${curr}`;
  });

  const changeSection = changedLines.length
    ? [`${t(lang, "changedOnly")}:`, ...changedLines].join("\n")
    : `${t(lang, "changedOnly")}: ${t(lang, "none")}`;

  const alertLines = alerts.map(a => `• ${a.line}`);
  const alertSection = alertLines.length
    ? [`${t(lang, "alerts")}:`, ...alertLines].join("\n")
    : `${t(lang, "alerts")}: ${t(lang, "none")}`;

  return `${title}

${t(lang, "at")}: ${new Date().toLocaleString()}

${changeSection}

${alertSection}

${t(lang, "statusReplyTitle")}:
${buildStatusSummary(currentData, lang)}

${statusLabel}`;
}

async function sendWhatsApp(to, body) {
  await client.messages.create({
    from: process.env.TWILIO_WHATSAPP_FROM,
    to,
    body
  });
}

async function sendUpdateMessage(body) {
  try {
    await sendWhatsApp(USER_PHONE, body);
    console.log("✅ WhatsApp alert sent");
  } catch (err) {
    console.error("❌ WhatsApp send failed:", err.message);
  }
}

async function writeCommandLog(data) {
  try {
    await db.ref(COMMAND_LOG_PATH).push({
      ...data,
      createdAt: new Date().toISOString()
    });
  } catch {
    // ignore
  }
}

// ======================================================
// LISTENER
// ======================================================
async function startRealtimeListener() {
  await loadLanguage();
  console.log(`👂 Listening on Firebase path: ${ROOT_PATH}`);

  db.ref(ROOT_PATH).on("value", async snapshot => {
    try {
      const data = snapshot.val();
      if (!data) return;

      if (!isInitialLoadComplete) {
        lastData = deepClone(data);
        isInitialLoadComplete = true;
        console.log("✅ Initial snapshot loaded. Future changes will trigger alerts.");
        return;
      }

      const previous = lastData || {};
      const current = deepClone(data);

      const changes = diffChangedFields(previous, current);
      const lastAlertState = await getLastAlertState();
      const { alerts, newAlertState } = evaluateThresholds(
        current,
        currentLanguage,
        lastAlertState
      );

      lastData = deepClone(current);
      await setLastAlertState(newAlertState);

      if (changes.length > 0 || alerts.length > 0) {
        const message = buildUnifiedMessage({
          changes,
          alerts,
          currentData: current,
          lang: currentLanguage
        });
        await sendUpdateMessage(message);
      } else {
        console.log("ℹ️ No important change detected.");
      }
    } catch (err) {
      console.error("❌ Listener error:", err.message);
    }
  });
}

// ======================================================
// COMMAND HANDLING
// ======================================================
async function updatePumpField(value) {
  await db.ref(`${ROOT_PATH}/irrigation/pump`).set(value);
}

async function getCurrentLiveData() {
  const snap = await db.ref(ROOT_PATH).get();
  return snap.exists() ? snap.val() : {};
}

function normalizeCommand(text) {
  return String(text || "")
    .trim()
    .toLowerCase()
    .replace(/\s+/g, " ");
}

async function handleCommand(rawText, from) {
  const cmd = normalizeCommand(rawText);

  if (!cmd) {
    return { success: false, message: t(currentLanguage, "invalidCommand") };
  }

  if (isEnglishCommand(cmd)) {
    await saveLanguage("en");
    return {
      success: true,
      message: `${t("en", "langSet")}\n\n${t("en", "replyLanguagePrompt")}`
    };
  }

  if (isHindiCommand(cmd)) {
    await saveLanguage("hi");
    return {
      success: true,
      message: `${t("hi", "langSet")}\n\n${t("hi", "replyLanguagePrompt")}`
    };
  }

  if (cmd === "help") {
    return { success: true, message: t(currentLanguage, "help") };
  }

  if (cmd === "status") {
    const data = await getCurrentLiveData();
    return {
      success: true,
      message:
`${currentLanguage === "hi" ? "📋 SmartKrishi स्थिति" : "📋 SmartKrishi Status"}

${t(currentLanguage, "statusReplyTitle")}:
${buildStatusSummary(data, currentLanguage)}

${t(currentLanguage, "at")}: ${new Date().toLocaleString()}`
    };
  }

  if (cmd === "pump on") {
    await updatePumpField(1);
    return {
      success: true,
      message: `✅ ${t(currentLanguage, "pumpOn")}`
    };
  }

  if (cmd === "pump off") {
    await updatePumpField(0);
    return {
      success: true,
      message: `✅ ${t(currentLanguage, "pumpOff")}`
    };
  }

  if (/^set\s+/i.test(cmd)) {
    return {
      success: false,
      message: t(currentLanguage, "setNotAllowed")
    };
  }

  return { success: false, message: t(currentLanguage, "invalidCommand") };
}

// ======================================================
// TWILIO WEBHOOK
// ======================================================
app.post("/whatsapp", async (req, res) => {
  const incomingText = req.body.Body || "";
  const from = req.body.From || "";

  try {
    const result = await handleCommand(incomingText, from);

    await writeCommandLog({
      from,
      text: incomingText,
      success: result.success
    });

    const twiml = new twilio.twiml.MessagingResponse();
    twiml.message(result.message);

    res.type("text/xml").send(twiml.toString());
  } catch (err) {
    console.error("❌ Webhook error:", err.message);

    const twiml = new twilio.twiml.MessagingResponse();
    twiml.message(`❌ ${t(currentLanguage, "commandFailed")}\n${err.message}`);
    res.type("text/xml").send(twiml.toString());
  }
});

// ======================================================
// HEALTH ROUTES
// ======================================================
app.get("/", (req, res) => {
  res.json({
    ok: true,
    service: "smartkrishi-whatsapp-server",
    monitoredPath: ROOT_PATH,
    language: currentLanguage
  });
});

app.get("/health", (req, res) => {
  res.json({
    ok: true,
    uptimeSeconds: process.uptime(),
    monitoredPath: ROOT_PATH,
    language: currentLanguage,
    timestamp: new Date().toISOString()
  });
});

// ======================================================
// START
// ======================================================
app.listen(PORT, async () => {
  console.log(`🚀 Server running on port ${PORT}`);
  await startRealtimeListener();
});