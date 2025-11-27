# 🚀 Deploy Monthly Notifications - Quick Guide

## ✅ What Was Updated

Your notification system now works like this:

### **Days 1-9:** 
- 1 reminder at 9:00 AM each day

### **Day 10 (Last Day):**
- 3 reminders: 9:00 AM, 12:00 PM, 3:00 PM

### **After Payment:**
- "Payment Received!" notification
- All reminders stop for that month
- Auto-resume next month

---

## 🎯 Deploy in 3 Steps

### **Step 1: Deploy Cloud Functions**

Open Command Prompt and run:

```bash
cd C:\Users\PratheekRaj(G10XIND)\AndroidStudioProjects\FinApp

firebase deploy --only functions --token "1//0giEisIBpOFFPCgYIARAAGBASNgF-L9IriAlLd5r0K_vyaFlSRjW1FUmbVMST7Gg4ElsTKj0nosYENWB9yJupsoEUlsHXrhcALg"
```

**Wait for (2-3 minutes):**
```
✔  functions[monthlyPaymentReminder9AM]: Successful create operation.
✔  functions[monthlyPaymentReminder12PM]: Successful create operation.
✔  functions[monthlyPaymentReminder3PM]: Successful create operation.
✔  functions[onPaymentCreated]: Successful create operation.
✔  Deploy complete!
```

---

### **Step 2: Enable Cloud Scheduler API**

1. Go to: https://console.cloud.google.com/cloudscheduler?project=lendflow-880a8
2. Click **"Enable"** if prompted
3. Wait 10-20 seconds

---

### **Step 3: Verify It Works**

1. Go to: https://console.firebase.google.com/project/lendflow-880a8/functions
2. Check these NEW functions exist:
   - ✅ `monthlyPaymentReminder9AM`
   - ✅ `monthlyPaymentReminder12PM`
   - ✅ `monthlyPaymentReminder3PM`
   - ✅ `onPaymentCreated`

---

## 🧪 Test It

### **Quick Test:**

1. **Make a payment in the app** (as CLIENT)
2. **You should get:** "✅ Payment Received!" notification
3. **Check logs:**
   ```bash
   firebase functions:log --only onPaymentCreated --token "YOUR_TOKEN"
   ```

---

## 📅 Notification Schedule

```
Month Calendar:
┌─────────────────────────────────────┐
│ Day 1-9:  9:00 AM (1 reminder/day) │
│                                      │
│ Day 10:                              │
│   • 9:00 AM  - Final Reminder       │
│   • 12:00 PM - Urgent Warning       │
│   • 3:00 PM  - FINAL NOTICE         │
│                                      │
│ After Payment:                       │
│   • "Payment Received!" ✅          │
│   • No more reminders this month    │
│                                      │
│ Day 11-31: No reminders             │
│                                      │
│ Next Month: Auto-restart            │
└─────────────────────────────────────┘
```

---

## 💰 Cost

**Cloud Scheduler:**
- First 3 jobs: **FREE**
- You have exactly 3 jobs ✅

**Cloud Functions:**
- ~13 invocations per loan per month
- Free tier: 2M invocations/month
- **Cost: $0** ✅

---

## 🔍 Check Logs

```bash
# View all logs
firebase functions:log --token "YOUR_TOKEN"

# View specific function
firebase functions:log --only monthlyPaymentReminder9AM --token "YOUR_TOKEN"

# Recent logs (last 50)
firebase functions:log --limit 50 --token "YOUR_TOKEN"
```

---

## ⚙️ Change Timezone

**Current:** Asia/Kolkata (Indian Standard Time)

**To change:**
1. Edit `functions/index.js`
2. Find all: `.timeZone('Asia/Kolkata')`
3. Change to your timezone:
   - `America/New_York`
   - `Europe/London`
   - `Asia/Tokyo`
4. Redeploy functions

---

## 🐛 Troubleshooting

### **Notifications not working?**

1. Check Cloud Scheduler is enabled
2. Verify functions deployed successfully
3. Check user has FCM token in Firestore
4. View logs: `firebase functions:log`

### **Still getting reminders after payment?**

1. Check payment status is "SUCCESS"
2. Check payment date is current month
3. View logs: `firebase functions:log --only onPaymentCreated`

---

## ✅ Success Checklist

After deployment, verify:

- [ ] `firebase deploy` completed successfully
- [ ] Cloud Scheduler API is enabled
- [ ] 7 functions visible in Firebase Console
- [ ] 3 scheduled jobs in Cloud Scheduler
- [ ] Test payment shows "Payment Received!" notification
- [ ] Check logs show no errors

---

## 📞 Quick Commands

```bash
# Deploy
firebase deploy --only functions --token "YOUR_TOKEN"

# View logs
firebase functions:log --token "YOUR_TOKEN"

# List functions
firebase functions:list --token "YOUR_TOKEN"

# Delete a function (if needed)
firebase functions:delete FUNCTION_NAME --token "YOUR_TOKEN"
```

---

## 🎉 That's It!

**Deploy now:**

```bash
firebase deploy --only functions --token "1//0giEisIBpOFFPCgYIARAAGBASNgF-L9IriAlLd5r0K_vyaFlSRjW1FUmbVMST7Gg4ElsTKj0nosYENWB9yJupsoEUlsHXrhcALg"
```

**Your monthly notification system will be LIVE!** 🚀

---

**For detailed info, see:** `MONTHLY_NOTIFICATIONS_GUIDE.md`

