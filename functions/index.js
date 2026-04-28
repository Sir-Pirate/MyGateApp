const functions = require("firebase-functions");
const admin = require("firebase-admin");
admin.initializeApp();

exports.sendNotification = functions.firestore
  .document("notificationRequests/{docId}")
  .onCreate(async (snap, context) => {
    const data = snap.data();
    if (data.sent) return;

    const message = {
      notification: { title: data.title, body: data.message },
      data: { title: data.title, message: data.message, type: data.type },
      topic: "all",
    };

    await admin.messaging().send(message);
    await snap.ref.update({ sent: true });
  });