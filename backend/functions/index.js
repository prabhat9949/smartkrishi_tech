const {setGlobalOptions} = require("firebase-functions/v2");
const functions = require("firebase-functions");
const admin = require("firebase-admin");
const twilio = require("twilio");

admin.initializeApp();

setGlobalOptions({ maxInstances: 10 });

const accountSid = functions.config().twilio.sid;
const authToken = functions.config().twilio.token;
const client = twilio(accountSid, authToken);

// 🧠 Thresholds (CUSTOMIZE THESE)
const MOISTURE_LOW = 30;
const TEMP_HIGH = 40;
const PH_LOW = 5.5;
const PH_HIGH = 8.5;

// 🔥 MAIN FUNCTION
exports.smartKrishiWhatsApp = functions.database
  .ref("/dashboard/farmer_SK001/farm_alpha01/live")
  .onUpdate(async (change, context) => {

    const before = change.before.val();
    const after = change.after.val();

    const irrigation = after.irrigation || {};
    const zone1 = after.nodes?.zone_1 || {};

    let alerts = [];
    let important = false;

    // ✅ PUMP CHANGE ALERT
    if (before.irrigation?.pump !== irrigation.pump) {
      important = true;
      alerts.push(`🚀 Pump turned ${irrigation.pump == 1 ? "ON" : "OFF"}`);
    }

    // ⚠️ MOISTURE ALERT
    if (zone1.moisture < MOISTURE_LOW) {
      important = true;
      alerts.push(`🌱 Low Moisture: ${zone1.moisture}%`);
    }

    // ⚠️ TEMPERATURE ALERT
    if (zone1.temperature > TEMP_HIGH) {
      important = true;
      alerts.push(`🌡 High Temperature: ${zone1.temperature}°C`);
    }

    // ⚠️ pH ALERT
    if (zone1.ph < PH_LOW || zone1.ph > PH_HIGH) {
      important = true;
      alerts.push(`🧪 Abnormal pH: ${zone1.ph}`);
    }

    // ⚠️ TANK EMPTY ALERT
    if (irrigation.tank == 0) {
      important = true;
      alerts.push(`🛢 Tank EMPTY`);
    }

    // ❌ If nothing important → skip message
    if (!important) {
      console.log("No important update, skipping...");
      return null;
    }

    // 📊 FULL STATUS (for context)
    const message =
`🚜 SmartKrishi Alert

${alerts.join("\n")}

📊 Current Status:
💧 Pump: ${irrigation.pump == 1 ? "ON" : "OFF"}
🌧 Rain: ${irrigation.rain == 1 ? "YES" : "NO"}
🛢 Tank: ${irrigation.tank == 1 ? "FULL" : "EMPTY"}
⚡ TDS: ${irrigation.tds}

🌱 Zone 1:
Moisture: ${zone1.moisture}%
Temp: ${zone1.temperature}°C
pH: ${zone1.ph}

⏱ ${new Date().toLocaleString()}`;

    return client.messages.create({
      from: "whatsapp:+14155238886",
      to: "whatsapp:+9199121678",
      body: message
    });
});