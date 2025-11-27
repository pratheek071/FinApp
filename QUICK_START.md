# 🚀 Quick Start - FCM Setup (5 Minutes)

Follow these steps to enable push notifications in your FinApp:

---

## 📦 What You Need

- ✅ Node.js installed (get it from https://nodejs.org/)
- ✅ Internet connection
- ✅ 5 minutes of your time

---

## 🎯 3 Simple Steps

### Step 1: Install Firebase CLI (One-time setup)

Open Command Prompt or PowerShell:

```bash
npm install -g firebase-tools
firebase login
```

A browser will open - sign in with your Google account.

---

### Step 2: Install Dependencies

In your project folder:

```bash
cd C:\Users\PratheekRaj(G10XIND)\AndroidStudioProjects\FinApp

cd functions
npm install
cd ..
```

This installs the required packages for Cloud Functions.

---

### Step 3: Deploy Cloud Functions

```bash
firebase deploy --only functions
```

Wait 1-2 minutes. You'll see:
```
✔  functions[onNewLoanCreated]: Deployed
✔  functions[onLoanStatusUpdated]: Deployed
✔  functions[sendTestNotification]: Deployed
```

**Done!** 🎉

---

## 🏃 Run Your App

1. In Android Studio: **Build → Clean Project**
2. Then: **Build → Rebuild Project**
3. Click **Run** ▶️

---

## ✅ Test It

### Test 1: Client → Admin Notification
1. Login as **CLIENT**
2. Submit a loan request
3. Admin should get notification: **"🔔 New Loan Request"**

### Test 2: Admin → Client Notification
1. Login as **ADMIN**
2. Approve a loan
3. Client should get notification: **"✅ Loan Approved!"**

---

## 🐛 Issues?

**Notifications not working?**
1. Check Android notification permissions are ON
2. Make sure both users logged in at least once (to save FCM tokens)
3. Check Cloud Functions logs in Firebase Console

**Detailed troubleshooting:** See `FCM_SETUP_GUIDE.md`

---

## 📁 Files Created

```
FinApp/
├── functions/
│   ├── index.js          ← Cloud Functions code
│   ├── package.json      ← Dependencies
│   └── .gitignore
├── .firebaserc           ← Firebase project config
├── firebase.json         ← Firebase settings
├── FCM_SETUP_GUIDE.md   ← Detailed setup guide
└── QUICK_START.md       ← This file
```

---

## 🎯 What Changed in Your App

### Android Code:
- ✅ `FCMHelper.kt` - Manages FCM tokens
- ✅ `MyFirebaseMessagingService.kt` - Handles notifications
- ✅ `AuthViewModel.kt` - Initializes FCM on login
- ✅ `AndroidManifest.xml` - Service declaration

### Cloud Functions (Automatic):
- ✅ Sends notification to admins when loan is created
- ✅ Sends notification to client when loan is approved/rejected

---

## 💡 How It Works

```
Client submits loan
    ↓
Saved to Firestore
    ↓
Cloud Function automatically triggered
    ↓
Notification sent to all admins
    ↓
Admin receives push notification! 🔔
```

---

**That's it!** Your app now has professional push notifications. 🎉

