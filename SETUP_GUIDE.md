# FinApp - Finance Monitoring App Setup Guide

## 🎉 Implementation Complete!

All features have been successfully implemented:
- ✅ Phone authentication with OTP
- ✅ Client loan application system
- ✅ UPI payment integration
- ✅ Admin dashboard with loan management
- ✅ Daily payment notifications
- ✅ Firebase Firestore database
- ✅ Complete MVVM architecture

---

## 📱 Firebase Console Setup Instructions

### Step 1: Create Firebase Project

1. Go to [Firebase Console](https://console.firebase.google.com/)
2. Click **"Add project"**
3. Enter project name: **"FinApp"**
4. Disable Google Analytics (optional)
5. Click **"Create project"**

### Step 2: Add Android App to Firebase

1. In Firebase Console, click the **Android icon** to add an Android app
2. Enter the following details:
   - **Android package name:** `com.example.finapp`
   - **App nickname:** FinApp (optional)
   - **Debug signing certificate SHA-1:** (optional, but recommended for phone auth)
3. Click **"Register app"**
4. **Download `google-services.json`**
5. **Important:** Place the downloaded `google-services.json` file in the `app/` directory of your project

### Step 3: Enable Phone Authentication

1. In Firebase Console, go to **Authentication** → **Sign-in method**
2. Click on **"Phone"**
3. Toggle to **Enable**
4. Click **"Save"**

**Note:** Phone authentication works in the emulator, but for production:
- You may need to add test phone numbers in Firebase Console
- You'll need to configure reCAPTCHA for production apps
- Add SHA-1 fingerprint for better security

### Step 4: Setup Cloud Firestore Database

1. In Firebase Console, go to **Firestore Database**
2. Click **"Create database"**
3. Choose **"Start in production mode"** (we'll update rules next)
4. Select your preferred location (closest to your users)
5. Click **"Enable"**

### Step 5: Configure Firestore Security Rules

1. Go to **Firestore Database** → **Rules**
2. Replace the default rules with:

```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    
    // Users collection
    match /users/{userId} {
      allow read, write: if request.auth != null && request.auth.uid == userId;
      allow read: if request.auth != null && 
                    get(/databases/$(database)/documents/users/$(request.auth.uid)).data.role == 'ADMIN';
    }
    
    // Loans collection
    match /loans/{loanId} {
      allow read: if request.auth != null;
      allow create: if request.auth != null;
      allow update: if request.auth != null && 
                      get(/databases/$(database)/documents/users/$(request.auth.uid)).data.role == 'ADMIN';
    }
    
    // Payments collection
    match /payments/{paymentId} {
      allow read: if request.auth != null;
      allow create: if request.auth != null;
    }
  }
}
```

3. Click **"Publish"**

### Step 6: Enable Cloud Messaging (for Notifications)

1. In Firebase Console, go to **Cloud Messaging**
2. No additional setup required - it's automatically enabled!
3. Note: The app will automatically handle FCM tokens

### Step 7: (Optional) Setup Test Phone Numbers

For testing without real SMS:
1. Go to **Authentication** → **Sign-in method** → **Phone**
2. Scroll down to **"Phone numbers for testing"**
3. Add test phone numbers with their OTP codes:
   - Example: `+919876543210` → OTP: `123456`

---

## ⚙️ App Configuration

### Update Admin UPI ID

Open `app/src/main/java/com/example/finapp/utils/Constants.kt` and update:

```kotlin
const val ADMIN_UPI_ID = "your-actual-upi@bank"  // Replace with your UPI ID
const val PAYMENT_RECEIVER_NAME = "Your Business Name"
```

### Build and Run

1. Place `google-services.json` in the `app/` directory
2. Sync Gradle files
3. Build and run the app

```bash
./gradlew clean build
```

---

## 🎯 App Features

### CLIENT SIDE:
1. **Phone Authentication**
   - Sign up with phone number and OTP
   - Auto-fill profile information

2. **Loan Application**
   - Select loan type (Education, Personal, Home, Car)
   - Auto-calculated interest rates (5%, 5.5%, 6%, 6.5%)
   - Enter loan amount and duration
   - View calculated total amount and daily payment
   - Submit loan request

3. **Loan Dashboard**
   - View all loans (pending, approved, completed)
   - See payment progress
   - Quick access to make payments

4. **Payment System**
   - View today's due amount
   - Manual UPI payment confirmation
   - Enter UPI ID and Transaction ID
   - Payment history tracking

5. **Notifications**
   - Daily reminders at 9:00 AM
   - In-app notifications for pending payments

### ADMIN SIDE:
1. **Admin Dashboard**
   - View today's payment summary
   - Total collected amount
   - Number of customers paid

2. **Loan Management**
   - View pending loan requests
   - Approve or reject loans
   - View active loans with payment progress

3. **Payment Tracking**
   - Monitor all payments
   - Track customer payment history
   - View detailed payment information

---

## 📊 Database Structure

### Collections:

**users/**
- id, phoneNumber, name, role, createdAt, fcmToken

**loans/**
- id, userId, userName, phoneNumber
- loanType, interestRate, principalAmount
- durationMonths, totalAmount, dailyAmount, totalDays
- status, requestedAt, approvedAt, startDate, endDate
- paidAmount, remainingAmount

**payments/**
- id, loanId, userId, userName
- amount, paymentDate, status
- transactionId, upiId, dayNumber, paymentMethod

---

## 🔧 Troubleshooting

### Issue: Phone Authentication not working
**Solution:**
- Add SHA-1 fingerprint in Firebase Console
- Enable Phone authentication in Firebase
- Check if test phone numbers are configured correctly

### Issue: Firestore permission denied
**Solution:**
- Verify Firestore Security Rules are correctly set
- Ensure user is authenticated before accessing data
- Check if user role is correctly set in database

### Issue: Notifications not showing
**Solution:**
- Grant notification permission in Android settings
- Verify WorkManager is scheduled correctly
- Check if app has battery optimization disabled

### Issue: App crashes on startup
**Solution:**
- Ensure `google-services.json` is in the correct location
- Sync Gradle files
- Clean and rebuild project

---

## 🚀 How to Use the App

### As a Client:
1. Open app → Select **CLIENT**
2. Enter phone number → Verify OTP
3. Complete profile (enter name)
4. Click **+** button to apply for loan
5. Fill loan details → Calculate → Submit
6. Wait for admin approval
7. Once approved, click **Pay Now** on loan card
8. **Two payment options:**
   - **Option A (Recommended):** Click **"Pay with UPI"** → Select your UPI app (GPay/PhonePe/etc.) → Complete payment
   - **Option B (Manual):** If already paid externally, enter Transaction ID → Click **"Confirm Manual Payment"**
9. Payment automatically recorded and loan updated

### As an Admin:
1. Open app → Select **ADMIN**
2. Enter phone number → Verify OTP
3. Complete profile
4. View dashboard with today's summary
5. Go to **Pending Loans** tab
6. Review loan requests
7. Click **Approve** or **Reject**
8. Switch to **Active Loans** tab to view approved loans
9. Monitor payment progress

---

## 🎨 App Architecture

- **Architecture Pattern:** MVVM (Model-View-ViewModel)
- **Dependency Injection:** Hilt/Dagger
- **Database:** Firebase Firestore
- **Authentication:** Firebase Phone Auth
- **Notifications:** WorkManager + Firebase Cloud Messaging
- **UI:** Material Design 3, ViewBinding
- **Async Operations:** Kotlin Coroutines + LiveData

---

## 💳 UPI Payment System

### Direct UPI Payment (NEW!) 🎉

The app now supports **DIRECT UPI PAYMENT** from within the app!

**How it works:**
1. User clicks **"Pay with UPI"** button in the app
2. App opens installed UPI apps (GPay, PhonePe, Paytm, etc.)
3. Payment details are pre-filled (amount, UPI ID, reference)
4. User completes payment using PIN/fingerprint
5. App automatically records payment upon success
6. Loan balance updates in real-time

**Features:**
- ✅ Works with ALL UPI apps (GPay, PhonePe, Paytm, BHIM, etc.)
- ✅ No payment gateway needed - completely FREE
- ✅ Automatic transaction ID capture
- ✅ Instant payment confirmation
- ✅ Fallback manual entry option

**Technical Details:**
- Uses Android UPI Intent (standard UPI deep linking)
- No third-party SDK required
- No business account needed
- Works on all Android devices with UPI apps installed

### Manual Payment Entry

For users who have already paid externally or prefer manual entry:
- Enter Transaction ID
- Optionally add UPI ID
- Click "Confirm Manual Payment"

---

## 📝 Important Notes

1. **UPI Payment:** The app now supports direct UPI payment using Android's native UPI Intent. Users can pay directly from the app without leaving it. No payment gateway or subscription needed!

2. **Notifications:** Daily reminders are scheduled at 9:00 AM. Make sure the app has permission to run in the background.

3. **Phone Numbers:** Use format `+91xxxxxxxxxx` for Indian numbers or appropriate country code.

4. **Security:** Never commit `google-services.json` to public repositories. Add it to `.gitignore`.

5. **Testing:** Use Firebase Test Lab or physical devices for thorough testing of phone authentication.

---

## 🔐 Security Best Practices

1. Keep `google-services.json` secure
2. Use Firestore Security Rules properly
3. Validate all inputs on both client and server side
4. Don't store sensitive data in logs
5. Implement proper error handling
6. Use ProGuard/R8 for production builds
7. Implement rate limiting for API calls

---

## 📞 Support

If you encounter any issues:
1. Check Firebase Console logs
2. Review Logcat for error messages
3. Verify all Firebase services are enabled
4. Ensure internet connection is stable
5. Check if all dependencies are properly synced

---

## ✅ Next Steps

1. Place `google-services.json` in `app/` directory
2. Update `Constants.kt` with your UPI ID
3. Sync and build project
4. Test on emulator or device
5. Deploy to Play Store when ready!

---

**Your Finance Monitoring App is ready to use! 🎉**

