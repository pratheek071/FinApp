# ✅ FCM Setup Verification Checklist

Use this checklist to verify your FCM with Cloud Functions setup is complete and working.

---

## 📋 Pre-Deployment Checks

### 1. Node.js & Firebase CLI
```bash
# Run these commands:
node --version
# Should show: v18.x.x or v20.x.x

npm --version
# Should show: 9.x.x or higher

firebase --version
# Should show: 13.x.x or similar
```

- [ ] Node.js installed and version is correct
- [ ] npm is working
- [ ] Firebase CLI is installed
- [ ] Successfully logged in with `firebase login`

### 2. Project Files
- [ ] `functions/index.js` exists with Cloud Functions code
- [ ] `functions/package.json` exists
- [ ] `.firebaserc` exists with project ID: `lendflow-880a8`
- [ ] `firebase.json` exists

### 3. Android Code
- [ ] `FCMHelper.kt` exists in `utils/` folder
- [ ] `MyFirebaseMessagingService.kt` exists in `services/` folder
- [ ] `AuthViewModel.kt` imports and uses `FCMHelper`
- [ ] `AndroidManifest.xml` has `MyFirebaseMessagingService` declaration
- [ ] `User.kt` data model has `fcmToken` field

---

## 🚀 Deployment Checks

### 1. Install Dependencies
```bash
cd functions
npm install
```
- [ ] No errors during `npm install`
- [ ] `node_modules/` folder created in `functions/`

### 2. Deploy Functions
```bash
firebase deploy --only functions
```
- [ ] Deployment completes successfully
- [ ] See: `✔ functions[onNewLoanCreated]: Deployed`
- [ ] See: `✔ functions[onLoanStatusUpdated]: Deployed`
- [ ] See: `✔ functions[sendTestNotification]: Deployed`

### 3. Verify in Firebase Console
Go to: https://console.firebase.google.com/

1. Select project: `lendflow-880a8`
2. Click **Functions** in left menu
3. Verify:
   - [ ] `onNewLoanCreated` status is **Active** (green)
   - [ ] `onLoanStatusUpdated` status is **Active** (green)
   - [ ] `sendTestNotification` status is **Active** (green)

---

## 📱 Android App Checks

### 1. Build App
- [ ] **Build → Clean Project** completes
- [ ] **Build → Rebuild Project** completes with no errors
- [ ] No linter errors in FCM-related files

### 2. Run App
- [ ] App installs on device/emulator successfully
- [ ] App launches without crashes
- [ ] Can see login screen

---

## 🧪 Functional Testing

### Test 1: FCM Token Generation

1. **Login to app** (as CLIENT or ADMIN)
2. **Check Logcat** in Android Studio:
   - Filter for: `FCM`
   - [ ] See: "FCM Token obtained: dXe3K..."
   - [ ] See: "FCM Token saved to Firestore"

3. **Check Firestore**:
   - Go to Firebase Console → Firestore Database
   - Open `users` collection
   - Find your user document
   - [ ] `fcmToken` field exists
   - [ ] `fcmToken` has a long string value (not empty)

### Test 2: Client → Admin Notification

**Setup:**
- [ ] At least one ADMIN user exists in Firestore
- [ ] Admin has logged into the app (has FCM token)

**Steps:**
1. Login as **CLIENT**
2. Go to loan application
3. Fill form and submit loan
4. **Expected:**
   - [ ] Loan saved to Firestore
   - [ ] Admin receives push notification
   - [ ] Notification shows: "🔔 New Loan Request"

**If notification doesn't appear:**
1. Check Cloud Functions logs:
   - Firebase Console → Functions → onNewLoanCreated → Logs
   - [ ] See: "New loan created: loan123"
   - [ ] See: "Sending notification to X admin(s)"
   - [ ] See: "Successfully sent X notifications"

2. Check admin FCM token:
   - [ ] Admin user in Firestore has non-empty `fcmToken`

### Test 3: Admin → Client Notification

**Setup:**
- [ ] At least one pending loan exists
- [ ] Client has logged into the app (has FCM token)

**Steps:**
1. Login as **ADMIN**
2. See pending loan
3. Click **Approve** (or **Reject**)
4. **Expected:**
   - [ ] Loan status updated in Firestore
   - [ ] Client receives push notification
   - [ ] If approved: "✅ Loan Approved!"
   - [ ] If rejected: "❌ Loan Request Rejected"

**If notification doesn't appear:**
1. Check Cloud Functions logs:
   - Firebase Console → Functions → onLoanStatusUpdated → Logs
   - [ ] See: "Loan status changed: PENDING -> APPROVED"
   - [ ] See: "Sending notification to user: user123"
   - [ ] See: "Successfully sent notification"

2. Check client FCM token:
   - [ ] Client user in Firestore has non-empty `fcmToken`

---

## 🔍 Advanced Verification

### Check Function Execution Count
1. Firebase Console → Functions
2. Click on `onNewLoanCreated`
3. Check **Metrics** tab
   - [ ] See invocations increasing after submitting loans

### Check Function Logs
1. Firebase Console → Functions
2. Click on any function
3. Click **Logs** tab
   - [ ] See log entries when function executes
   - [ ] No error messages in red

### Test Notification on App Closed
1. Close the app completely (swipe away from recent apps)
2. From another device, trigger an action (submit loan / approve loan)
3. **Expected:**
   - [ ] Notification appears even though app was closed
   - [ ] This proves FCM is working correctly!

---

## 🐛 Troubleshooting Guide

### Issue: "No admin users found" in logs
**Check:**
- [ ] At least one user in Firestore has `role: "ADMIN"`
- [ ] Role field is exactly "ADMIN" (uppercase, case-sensitive)

### Issue: "No admin FCM tokens found"
**Check:**
- [ ] Admin logged into app at least once
- [ ] Admin user document has `fcmToken` field
- [ ] `fcmToken` is not empty

### Issue: "Permission denied" during deployment
**Check:**
- [ ] Logged in with `firebase login`
- [ ] Using correct Google account (has access to project)
- [ ] Project ID in `.firebaserc` matches Firebase Console

### Issue: Notification not showing
**Check:**
- [ ] Notification permission granted in Android settings
- [ ] Google Play Services installed on device/emulator
- [ ] FCM token exists in Firestore for target user
- [ ] Cloud Functions executed successfully (check logs)
- [ ] No errors in Android Logcat

### Issue: App crashes after adding FCM code
**Check:**
- [ ] Clean and rebuild project
- [ ] All imports are correct
- [ ] `google-services.json` is in `app/` folder
- [ ] Gradle sync completed successfully

---

## 💯 Final Success Criteria

You can consider the setup **100% successful** if:

- [x] Cloud Functions are deployed and Active
- [x] Android app builds and runs without errors
- [x] FCM tokens are saved to Firestore for all users
- [x] Client submits loan → Admin gets notification ✅
- [x] Admin approves loan → Client gets notification ✅
- [x] Notifications work even when app is closed ✅
- [x] No errors in Cloud Functions logs ✅
- [x] No errors in Android Logcat ✅

---

## 📊 Success Metrics

After successful setup, you should see:

### In Firestore:
- All users have `fcmToken` field populated
- Loans are being created with correct data
- Loan statuses are updating correctly

### In Cloud Functions Logs:
- "New loan created" messages
- "Sending notification to X admin(s)"
- "Successfully sent notification"
- "Loan status changed: PENDING -> APPROVED"

### On Devices:
- Notifications appear in notification tray
- Tapping notification opens the app
- Notifications work when app is closed

---

## 🎉 Congratulations!

If all checks pass, your FCM with Cloud Functions setup is **complete and working perfectly!**

**Next Steps:**
- Test with multiple devices
- Monitor Cloud Functions usage in Firebase Console
- Check notification delivery rates
- Customize notification messages as needed

---

**Need Help?**
- Check `FCM_SETUP_GUIDE.md` for detailed instructions
- Check Cloud Functions logs for error messages
- Check Android Studio Logcat for FCM logs

