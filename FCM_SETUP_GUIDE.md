# FCM with Cloud Functions - Setup Guide

This guide will help you set up Firebase Cloud Messaging with Cloud Functions for your FinApp.

---

## 📋 What Was Implemented

### Android App Changes:
1. ✅ **FCMHelper.kt** - Utility class to manage FCM tokens
2. ✅ **MyFirebaseMessagingService.kt** - Handles incoming push notifications
3. ✅ **AuthViewModel.kt** - Initializes FCM token when user logs in
4. ✅ **User.kt** - Already has `fcmToken` field
5. ✅ **AndroidManifest.xml** - Service declaration updated

### Cloud Functions:
1. ✅ **onNewLoanCreated** - Sends notification to admins when client submits loan
2. ✅ **onLoanStatusUpdated** - Sends notification to client when admin approves/rejects
3. ✅ **sendTestNotification** - Optional test function

---

## 🚀 Step-by-Step Setup Instructions

### Step 1: Install Node.js (Required for Cloud Functions)

1. **Download Node.js**:
   - Go to: https://nodejs.org/
   - Download **LTS version** (recommended: v18 or v20)
   - Install it on your computer

2. **Verify Installation**:
   ```bash
   node --version
   # Should show: v18.x.x or v20.x.x
   
   npm --version
   # Should show: 9.x.x or 10.x.x
   ```

---

### Step 2: Install Firebase CLI

1. **Open Command Prompt** (or PowerShell)

2. **Install Firebase Tools**:
   ```bash
   npm install -g firebase-tools
   ```

3. **Verify Installation**:
   ```bash
   firebase --version
   # Should show: 13.x.x or similar
   ```

4. **Login to Firebase**:
   ```bash
   firebase login
   ```
   - This will open a browser
   - Sign in with your Google account
   - Allow Firebase CLI access

---

### Step 3: Initialize Your Project

1. **Navigate to your project folder**:
   ```bash
   cd C:\Users\PratheekRaj(G10XIND)\AndroidStudioProjects\FinApp
   ```

2. **The project is already initialized!**
   - Files created: `.firebaserc`, `firebase.json`, `functions/` folder
   - Your project ID is set to: `lendflow-880a8`

---

### Step 4: Install Cloud Functions Dependencies

1. **Navigate to functions folder**:
   ```bash
   cd functions
   ```

2. **Install dependencies**:
   ```bash
   npm install
   ```
   This will install:
   - `firebase-admin` - For server-side Firebase operations
   - `firebase-functions` - For creating Cloud Functions

3. **Go back to root**:
   ```bash
   cd ..
   ```

---

### Step 5: Deploy Cloud Functions

1. **Deploy to Firebase**:
   ```bash
   firebase deploy --only functions
   ```

2. **What happens**:
   - Firebase uploads your functions to Google Cloud
   - Functions are automatically activated
   - You'll see output like:
     ```
     ✔  functions[onNewLoanCreated(us-central1)]: Successful create operation.
     ✔  functions[onLoanStatusUpdated(us-central1)]: Successful create operation.
     ✔  functions[sendTestNotification(us-central1)]: Successful create operation.
     ```

3. **Deployment complete!** 🎉

---

### Step 6: Build and Run Your Android App

1. **Clean and Rebuild**:
   - In Android Studio: `Build → Clean Project`
   - Then: `Build → Rebuild Project`

2. **Run the App**:
   - Click the green ▶️ Run button
   - Or press `Shift + F10`

---

## 🧪 Testing the Notifications

### Test 1: Client Submits Loan → Admin Gets Notified

1. **On Client Device/Emulator**:
   - Login as CLIENT
   - Fill loan application form
   - Click "Submit Loan Request"

2. **On Admin Device/Emulator**:
   - **You should receive a notification!** 🔔
   - Notification: "🔔 New Loan Request - John requested Education loan of ₹50,000"

3. **If notification doesn't appear**:
   - Check Android Studio Logcat for "FCM" logs
   - Verify admin has FCM token in Firestore
   - Check Cloud Functions logs in Firebase Console

### Test 2: Admin Approves/Rejects → Client Gets Notified

1. **On Admin Device/Emulator**:
   - Login as ADMIN
   - See pending loan request
   - Click "Approve" (or "Reject")

2. **On Client Device/Emulator**:
   - **You should receive a notification!** 🔔
   - If approved: "✅ Loan Approved! - Your Education loan has been approved!"
   - If rejected: "❌ Loan Request Rejected"

---

## 🔍 Verify Everything Is Working

### 1. Check FCM Tokens in Firestore

1. Go to Firebase Console: https://console.firebase.google.com/
2. Select project: `lendflow-880a8`
3. Go to **Firestore Database**
4. Open `users` collection
5. Click on any user document
6. Check if `fcmToken` field exists and has a long string value
   - Example: `dXe3K9L2m4Pq2fH8gR5nT7vY9...`
   - If empty, the app didn't save the token

### 2. Check Cloud Functions Are Deployed

1. In Firebase Console
2. Click **Functions** (in left menu under "Build")
3. You should see:
   - `onNewLoanCreated`
   - `onLoanStatusUpdated`
   - `sendTestNotification`
4. Status should be **Active** (green)

### 3. Check Cloud Functions Logs

1. In Firebase Console → **Functions**
2. Click on any function
3. Click **Logs** tab
4. You should see logs when functions execute
   - Example: "New loan created: loan123"
   - Example: "Sending notification to 2 admin(s)"

---

## 🐛 Troubleshooting

### Problem 1: "No admin users found" in logs
**Solution**: 
- Verify at least one user in Firestore has `role: "ADMIN"`
- Check the role field is exactly "ADMIN" (case-sensitive)

### Problem 2: "No admin FCM tokens found"
**Solution**:
- Admin needs to login to the app at least once
- FCM token is saved on login
- Check Firestore if admin user has `fcmToken` field

### Problem 3: Notification not appearing on device
**Solution**:
1. Check Android notification permission is granted
2. Check notification channel is created
3. Check device/emulator has Google Play Services
4. Check FCM token is valid in Firestore
5. Check Cloud Functions logs for errors

### Problem 4: "Permission denied" when deploying
**Solution**:
1. Make sure you're logged in: `firebase login`
2. Check you have Owner/Editor role in Firebase project
3. Verify project ID in `.firebaserc` matches your Firebase project

### Problem 5: Build errors after adding FCM code
**Solution**:
1. Sync Gradle files
2. Clean and rebuild project
3. Check all imports are correct
4. Verify `google-services.json` is in `app/` folder

---

## 📊 How It Works

### Flow Diagram

```
CLIENT SUBMITS LOAN:
Client App
    ↓
  [Saves loan to Firestore]
    ↓
Firestore (loans collection)
    ↓
  [AUTOMATIC TRIGGER]
    ↓
Cloud Function: onNewLoanCreated
    ↓
  [Gets all admin FCM tokens]
    ↓
  [Sends notification via FCM]
    ↓
FCM Server (Google)
    ↓
  [Delivers to all admin devices]
    ↓
Admin receives notification! 🔔


ADMIN APPROVES LOAN:
Admin App
    ↓
  [Updates loan status to "APPROVED"]
    ↓
Firestore (loans/{loanId})
    ↓
  [AUTOMATIC TRIGGER]
    ↓
Cloud Function: onLoanStatusUpdated
    ↓
  [Gets client FCM token]
    ↓
  [Sends notification via FCM]
    ↓
FCM Server (Google)
    ↓
  [Delivers to client device]
    ↓
Client receives notification! 🔔
```

---

## 💰 Cost Information

### Free Tier (Generous Limits):
- **Cloud Functions**: 2 million invocations/month
- **FCM**: Unlimited messages
- **For your app**: Probably 100% free unless you have thousands of users

### What counts as an invocation:
- Each loan submit = 1 invocation
- Each loan approval/rejection = 1 invocation
- Example: 1000 loans/month = 2000 invocations (well within free tier)

---

## 🔐 Security Notes

1. **Server keys are secure**: FCM server keys are on Google's servers, not in your app
2. **Functions run server-side**: More secure than sending from client app
3. **Firestore security rules**: Make sure your rules are properly set

---

## 📱 Testing on Multiple Devices

### To test admin and client notifications:

**Option 1: Two Physical Devices**
- Install app on both
- One logs in as ADMIN
- One logs in as CLIENT
- Test the flow

**Option 2: One Physical + One Emulator**
- Admin on physical device
- Client on emulator (or vice versa)

**Option 3: Use Test FCM Token**
- Get your FCM token from Logcat
- Manually test using Firebase Console Messaging

---

## 🎯 Next Steps After Setup

1. ✅ Deploy Cloud Functions
2. ✅ Run the Android app
3. ✅ Test loan submission → admin notification
4. ✅ Test loan approval → client notification
5. ✅ Check Firebase Console logs
6. ✅ Verify both user types have FCM tokens in Firestore

---

## 📞 Quick Reference Commands

```bash
# Check if Firebase CLI is installed
firebase --version

# Login to Firebase
firebase login

# Deploy only functions
firebase deploy --only functions

# View function logs
firebase functions:log

# Install dependencies
cd functions
npm install
cd ..

# Re-deploy after code changes
firebase deploy --only functions
```

---

## ✅ Success Checklist

Before you're done, verify:
- [ ] Node.js is installed
- [ ] Firebase CLI is installed
- [ ] You're logged into Firebase (`firebase login`)
- [ ] Dependencies are installed (`cd functions && npm install`)
- [ ] Functions are deployed (`firebase deploy --only functions`)
- [ ] Functions show as "Active" in Firebase Console
- [ ] Android app builds without errors
- [ ] Users have FCM tokens saved in Firestore
- [ ] Test notification works from client to admin
- [ ] Test notification works from admin to client

---

## 🎉 You're Done!

Your app now has real-time push notifications powered by Firebase Cloud Functions and FCM!

**Need help?** Check the Firebase Console logs or Android Studio Logcat for detailed error messages.

