# FinApp - Finance Monitoring Mobile Application

A comprehensive loan management and payment tracking system for Android, built with Kotlin, Firebase, and modern Android architecture.

## 📱 Features

### Client Features
- ✅ Phone number authentication with OTP
- ✅ Loan application with auto-calculated interest rates
- ✅ Support for 4 loan types: Education, Personal, Home, Car
- ✅ Real-time loan status tracking
- ✅ Daily payment reminders (9:00 AM)
- ✅ UPI payment integration
- ✅ Payment history tracking
- ✅ In-app notifications

### Admin Features
- ✅ Admin dashboard with daily summary
- ✅ Loan approval/rejection system
- ✅ Active loan monitoring
- ✅ Payment tracking across all customers
- ✅ Real-time updates

## 🛠 Tech Stack

- **Language:** Kotlin
- **Architecture:** MVVM (Model-View-ViewModel)
- **DI:** Hilt/Dagger
- **Backend:** Firebase (Auth, Firestore, Cloud Messaging)
- **UI:** Material Design 3, ViewBinding
- **Async:** Kotlin Coroutines, LiveData
- **Background Tasks:** WorkManager
- **Navigation:** Android Navigation Component

## 📋 Prerequisites

- Android Studio (latest version)
- Firebase Account
- Minimum SDK: 24 (Android 7.0)
- Target SDK: 36

## 🚀 Setup Instructions

### 1. Clone the Repository
```bash
git clone <your-repo-url>
cd FinApp
```

### 2. Firebase Setup
Follow the detailed instructions in [SETUP_GUIDE.md](SETUP_GUIDE.md)

Quick steps:
1. Create Firebase project
2. Download `google-services.json`
3. Place it in `app/` directory
4. Enable Phone Authentication
5. Create Firestore Database
6. Configure Security Rules

### 3. Configure Constants
Update `app/src/main/java/com/example/finapp/utils/Constants.kt`:
```kotlin
const val ADMIN_UPI_ID = "your-upi@bank"
const val PAYMENT_RECEIVER_NAME = "Your Business Name"
```

### 4. Build and Run
```bash
./gradlew clean build
./gradlew installDebug
```

## 📂 Project Structure

```
app/
├── data/
│   ├── model/           # Data classes (User, Loan, Payment)
│   └── repository/      # Repository implementations
├── di/                  # Dependency Injection modules
├── presentation/
│   ├── auth/           # Authentication screens
│   ├── client/         # Client-side features
│   └── admin/          # Admin-side features
├── services/           # Firebase services
├── workers/            # Background workers
└── utils/              # Utility classes

res/
├── layout/             # XML layouts
├── navigation/         # Navigation graph
├── menu/               # Menu resources
└── values/             # Strings, colors, etc.
```

## 🎯 How It Works

### Client Flow
1. Sign up with phone number + OTP
2. Apply for loan (select type, amount, duration)
3. System calculates interest and daily payment
4. Submit loan request
5. Wait for admin approval
6. Make daily payments via UPI
7. Track payment progress

### Admin Flow
1. Sign up as admin
2. View pending loan requests
3. Approve or reject loans
4. Monitor active loans
5. Track daily payment collection
6. View customer payment history

## 🔒 Security

- Firebase Authentication for user management
- Firestore Security Rules for data protection
- Role-based access control (CLIENT/ADMIN)
- Input validation on all forms
- Secure token management

## 📊 Database Schema

### Users Collection
```
{
  id: string
  phoneNumber: string
  name: string
  role: "CLIENT" | "ADMIN"
  createdAt: timestamp
}
```

### Loans Collection
```
{
  id: string
  userId: string
  loanType: string
  principalAmount: number
  interestRate: number
  totalAmount: number
  dailyAmount: number
  status: "PENDING" | "APPROVED" | "REJECTED" | "COMPLETED"
  paidAmount: number
  remainingAmount: number
}
```

### Payments Collection
```
{
  id: string
  loanId: string
  userId: string
  amount: number
  transactionId: string
  upiId: string
  paymentDate: timestamp
  status: "SUCCESS" | "PENDING" | "FAILED"
}
```

## 🔔 Notifications

- Daily reminders at 9:00 AM for pending payments
- In-app notifications for loan status updates
- Firebase Cloud Messaging for push notifications

## 💳 Payment System

The app supports **DIRECT UPI PAYMENT** from within the app! 🎉

### Direct UPI Payment:
1. Client clicks **"Pay with UPI"** button
2. App opens installed UPI apps (GPay, PhonePe, Paytm, etc.)
3. Payment details are pre-filled automatically
4. Client completes payment using PIN/fingerprint
5. Payment is automatically recorded on success
6. Loan balance updates in real-time

### Manual Payment Entry (Fallback):
1. If already paid externally
2. Client enters Transaction ID
3. Optionally adds UPI ID
4. Clicks "Confirm Manual Payment"

**Features:**
- ✅ Works with ALL UPI apps
- ✅ No payment gateway needed - completely FREE
- ✅ Uses Android native UPI Intent
- ✅ Instant transaction confirmation
- ✅ No third-party SDK required

**Note:** This uses the standard Android UPI deep linking system. No business account or payment gateway subscription required!

## 🧪 Testing

### Test Phone Authentication
Add test phone numbers in Firebase Console:
```
Phone: +919876543210
OTP: 123456
```

### Test Accounts
- Create one admin account
- Create multiple client accounts
- Test loan workflow end-to-end

## 📱 Screenshots

(Add screenshots here after running the app)

## 🐛 Known Issues

- Phone authentication requires internet connection
- Notifications require battery optimization to be disabled
- UPI payment is manual (requires payment gateway for automation)

## 🚀 Future Enhancements

- [ ] Automated UPI payment verification
- [ ] WhatsApp notifications integration
- [ ] PDF receipt generation
- [ ] Loan EMI calculator
- [ ] Credit score tracking
- [ ] Multi-language support
- [ ] Dark mode
- [ ] Analytics dashboard

## 📄 License

This project is for educational purposes.

## 👥 Contributors

Your Name

## 📞 Contact

For support or queries, contact: your.email@example.com

---

**Happy Coding! 🎉**

