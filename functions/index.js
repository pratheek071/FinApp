/**
 * Cloud Functions for FinApp
 * 
 * These functions automatically send FCM notifications when:
 * 1. A new loan is created (notify admins)
 * 2. A loan status is updated (notify client)
 * 3. Monthly payment reminders (days 1-10)
 * 4. Payment is received (notify client)
 */

const functions = require('firebase-functions');
const admin = require('firebase-admin');

// Initialize Firebase Admin
admin.initializeApp();

// Helper function to get current day of month
function getCurrentDayOfMonth() {
    const now = new Date();
    return now.getDate();
}

// Helper function to get current month and year
function getCurrentMonthYear() {
    const now = new Date();
    return `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, '0')}`;
}

// Helper function to check if user has paid this month
async function hasUserPaidThisMonth(userId, loanId) {
    try {
        const currentMonthYear = getCurrentMonthYear();
        
        // Check if there's a payment for this month
        const paymentsSnapshot = await admin.firestore()
            .collection('payments')
            .where('userId', '==', userId)
            .where('loanId', '==', loanId)
            .get();
        
        if (paymentsSnapshot.empty) {
            return false;
        }
        
        // Check if any payment is from this month
        for (const doc of paymentsSnapshot.docs) {
            const payment = doc.data();
            const paymentDate = new Date(payment.paymentDate);
            const paymentMonthYear = `${paymentDate.getFullYear()}-${String(paymentDate.getMonth() + 1).padStart(2, '0')}`;
            
            if (paymentMonthYear === currentMonthYear && payment.status === 'SUCCESS') {
                return true;
            }
        }
        
        return false;
    } catch (error) {
        console.error('Error checking payment status:', error);
        return false;
    }
}

/**
 * Trigger: When a new loan is created
 * Action: Send notification to all admins
 */
exports.onNewLoanCreated = functions.firestore
    .document('loans/{loanId}')
    .onCreate(async (snap, context) => {
        try {
            const loan = snap.data();
            const loanId = context.params.loanId;
            
            console.log(`New loan created: ${loanId}`, loan);
            
            // Get all admin users
            const adminsSnapshot = await admin.firestore()
                .collection('users')
                .where('role', '==', 'ADMIN')
                .get();
            
            if (adminsSnapshot.empty) {
                console.log('No admin users found');
                return null;
            }
            
            // Extract FCM tokens
            const adminTokens = adminsSnapshot.docs
                .map(doc => doc.data().fcmToken)
                .filter(token => token && token.length > 0);
            
            if (adminTokens.length === 0) {
                console.log('No admin FCM tokens found');
                return null;
            }
            
            console.log(`Sending notification to ${adminTokens.length} admin(s)`);
            
            // Create notification message
            const message = {
                notification: {
                    title: '🔔 New Loan Request',
                    body: `${loan.userName} requested ${loan.loanType} loan of ₹${loan.principalAmount.toLocaleString()}`
                },
                data: {
                    type: 'NEW_LOAN',
                    loanId: loanId,
                    userId: loan.userId,
                    amount: loan.principalAmount.toString()
                }
            };
            
            // Send to all admins
            const response = await admin.messaging().sendEachForMulticast({
                tokens: adminTokens,
                ...message
            });
            
            console.log(`Successfully sent ${response.successCount} notifications`);
            if (response.failureCount > 0) {
                console.log(`Failed to send ${response.failureCount} notifications`);
                response.responses.forEach((resp, idx) => {
                    if (!resp.success) {
                        console.error(`Failed to send to token ${idx}:`, resp.error);
                    }
                });
            }
            
            return null;
        } catch (error) {
            console.error('Error in onNewLoanCreated:', error);
            return null;
        }
    });

/**
 * Trigger: When a loan document is updated
 * Action: If status changed, notify the client
 */
exports.onLoanStatusUpdated = functions.firestore
    .document('loans/{loanId}')
    .onUpdate(async (change, context) => {
        try {
            const before = change.before.data();
            const after = change.after.data();
            const loanId = context.params.loanId;
            
            // Check if status changed
            if (before.status === after.status) {
                console.log('Status not changed, skipping notification');
                return null;
            }
            
            console.log(`Loan ${loanId} status changed: ${before.status} -> ${after.status}`);
            
            // Get the client user document
            const userDoc = await admin.firestore()
                .collection('users')
                .doc(after.userId)
                .get();
            
            if (!userDoc.exists) {
                console.log('User not found:', after.userId);
                return null;
            }
            
            const userData = userDoc.data();
            const fcmToken = userData.fcmToken;
            
            if (!fcmToken || fcmToken.length === 0) {
                console.log('User FCM token not found');
                return null;
            }
            
            // Prepare notification based on status
            let title = '';
            let body = '';
            
            if (after.status === 'APPROVED') {
                title = '✅ Loan Approved!';
                body = `Congratulations! Your ${after.loanType} loan of ₹${after.principalAmount.toLocaleString()} has been approved. Monthly payment: ₹${after.monthlyAmount.toLocaleString()}`;
            } else if (after.status === 'REJECTED') {
                title = '❌ Loan Request Rejected';
                body = `Your ${after.loanType} loan request of ₹${after.principalAmount.toLocaleString()} has been rejected. Please contact support for more details.`;
            } else {
                console.log('Status changed to:', after.status, '- no notification needed');
                return null;
            }
            
            console.log(`Sending notification to user: ${after.userId}`);
            
            // Send notification
            const message = {
                notification: {
                    title: title,
                    body: body
                },
                data: {
                    type: 'LOAN_STATUS',
                    loanId: loanId,
                    status: after.status,
                    amount: after.principalAmount.toString()
                },
                token: fcmToken
            };
            
            const response = await admin.messaging().send(message);
            console.log('Successfully sent notification:', response);
            
            return null;
        } catch (error) {
            console.error('Error in onLoanStatusUpdated:', error);
            return null;
        }
    });

/**
 * Scheduled Function: Monthly Payment Reminder at 9:00 AM (Days 1-9)
 * Triggers: Every day at 9:00 AM
 * Action: Send reminder if day is 1-9 and payment not made
 */
exports.monthlyPaymentReminder9AM = functions.pubsub
    .schedule('0 9 * * *')
    .timeZone('Asia/Kolkata') // Change to your timezone
    .onRun(async (context) => {
        try {
            const dayOfMonth = getCurrentDayOfMonth();
            
            console.log(`Running 9 AM reminder check for day ${dayOfMonth}`);
            
            // Only run on days 1-10
            if (dayOfMonth < 1 || dayOfMonth > 10) {
                console.log('Not a payment reminder day (1-10), skipping');
                return null;
            }
            
            // Get all approved loans
            const loansSnapshot = await admin.firestore()
                .collection('loans')
                .where('status', '==', 'APPROVED')
                .get();
            
            if (loansSnapshot.empty) {
                console.log('No approved loans found');
                return null;
            }
            
            let notificationsSent = 0;
            let paymentsAlreadyMade = 0;
            
            // Process each loan
            for (const loanDoc of loansSnapshot.docs) {
                const loan = loanDoc.data();
                
                // Check if user has paid this month
                const hasPaid = await hasUserPaidThisMonth(loan.userId, loanDoc.id);
                
                if (hasPaid) {
                    console.log(`User ${loan.userId} already paid for loan ${loanDoc.id} this month`);
                    paymentsAlreadyMade++;
                    continue;
                }
                
                // Get user FCM token
                const userDoc = await admin.firestore()
                    .collection('users')
                    .doc(loan.userId)
                    .get();
                
                if (!userDoc.exists || !userDoc.data().fcmToken) {
                    console.log(`No FCM token for user ${loan.userId}`);
                    continue;
                }
                
                const fcmToken = userDoc.data().fcmToken;
                
                // Determine notification message based on day
                let title = '';
                let body = '';
                
                if (dayOfMonth === 10) {
                    title = '⚠️ Final Reminder - Payment Due Today!';
                    body = `This is your LAST DAY to pay ₹${loan.monthlyAmount.toLocaleString()} for your ${loan.loanType} loan. Please pay before 11:59 PM to avoid penalties.`;
                } else if (dayOfMonth >= 8) {
                    title = '⏰ Payment Reminder';
                    body = `Your monthly payment of ₹${loan.monthlyAmount.toLocaleString()} is due by 10th. Only ${10 - dayOfMonth + 1} days remaining!`;
                } else {
                    title = '💰 Monthly Payment Reminder';
                    body = `Hi ${loan.userName}, your monthly payment of ₹${loan.monthlyAmount.toLocaleString()} is due by the 10th of this month.`;
                }
                
                // Send notification
                try {
                    await admin.messaging().send({
                        notification: { title, body },
                        data: {
                            type: 'PAYMENT_REMINDER',
                            loanId: loanDoc.id,
                            amount: loan.monthlyAmount.toString(),
                            dayOfMonth: dayOfMonth.toString()
                        },
                        token: fcmToken
                    });
                    
                    notificationsSent++;
                    console.log(`Sent reminder to user ${loan.userId} for loan ${loanDoc.id}`);
                } catch (error) {
                    console.error(`Failed to send notification to user ${loan.userId}:`, error);
                }
            }
            
            console.log(`Summary: Sent ${notificationsSent} reminders, ${paymentsAlreadyMade} already paid`);
            return null;
        } catch (error) {
            console.error('Error in monthlyPaymentReminder9AM:', error);
            return null;
        }
    });

/**
 * Scheduled Function: Additional Reminder at 12:00 PM (Day 10 only)
 * Triggers: Every day at 12:00 PM
 * Action: Send reminder if day is 10 and payment not made
 */
exports.monthlyPaymentReminder12PM = functions.pubsub
    .schedule('0 12 * * *')
    .timeZone('Asia/Kolkata')
    .onRun(async (context) => {
        try {
            const dayOfMonth = getCurrentDayOfMonth();
            
            console.log(`Running 12 PM reminder check for day ${dayOfMonth}`);
            
            // Only run on day 10
            if (dayOfMonth !== 10) {
                console.log('Not day 10, skipping 12 PM reminder');
                return null;
            }
            
            // Get all approved loans
            const loansSnapshot = await admin.firestore()
                .collection('loans')
                .where('status', '==', 'APPROVED')
                .get();
            
            if (loansSnapshot.empty) {
                console.log('No approved loans found');
                return null;
            }
            
            let notificationsSent = 0;
            
            for (const loanDoc of loansSnapshot.docs) {
                const loan = loanDoc.data();
                
                // Check if user has paid
                const hasPaid = await hasUserPaidThisMonth(loan.userId, loanDoc.id);
                if (hasPaid) continue;
                
                const userDoc = await admin.firestore()
                    .collection('users')
                    .doc(loan.userId)
                    .get();
                
                if (!userDoc.exists || !userDoc.data().fcmToken) continue;
                
                const fcmToken = userDoc.data().fcmToken;
                
                try {
                    await admin.messaging().send({
                        notification: {
                            title: '🚨 Urgent: Payment Due Today (12 PM Reminder)',
                            body: `You still need to pay ₹${loan.monthlyAmount.toLocaleString()} today! Only 12 hours left. Pay now to avoid late fees.`
                        },
                        data: {
                            type: 'PAYMENT_REMINDER_URGENT',
                            loanId: loanDoc.id,
                            amount: loan.monthlyAmount.toString(),
                            reminder: '2nd_of_3'
                        },
                        token: fcmToken
                    });
                    
                    notificationsSent++;
                } catch (error) {
                    console.error(`Failed to send 12 PM notification:`, error);
                }
            }
            
            console.log(`Sent ${notificationsSent} urgent reminders at 12 PM`);
            return null;
        } catch (error) {
            console.error('Error in monthlyPaymentReminder12PM:', error);
            return null;
        }
    });

/**
 * Scheduled Function: Final Reminder at 3:00 PM (Day 10 only)
 * Triggers: Every day at 3:00 PM
 * Action: Send final reminder if day is 10 and payment not made
 */
exports.monthlyPaymentReminder3PM = functions.pubsub
    .schedule('0 15 * * *') // 15:00 = 3 PM
    .timeZone('Asia/Kolkata')
    .onRun(async (context) => {
        try {
            const dayOfMonth = getCurrentDayOfMonth();
            
            console.log(`Running 3 PM reminder check for day ${dayOfMonth}`);
            
            // Only run on day 10
            if (dayOfMonth !== 10) {
                console.log('Not day 10, skipping 3 PM reminder');
                return null;
            }
            
            // Get all approved loans
            const loansSnapshot = await admin.firestore()
                .collection('loans')
                .where('status', '==', 'APPROVED')
                .get();
            
            if (loansSnapshot.empty) {
                console.log('No approved loans found');
                return null;
            }
            
            let notificationsSent = 0;
            
            for (const loanDoc of loansSnapshot.docs) {
                const loan = loanDoc.data();
                
                // Check if user has paid
                const hasPaid = await hasUserPaidThisMonth(loan.userId, loanDoc.id);
                if (hasPaid) continue;
                
                const userDoc = await admin.firestore()
                    .collection('users')
                    .doc(loan.userId)
                    .get();
                
                if (!userDoc.exists || !userDoc.data().fcmToken) continue;
                
                const fcmToken = userDoc.data().fcmToken;
                
                try {
                    await admin.messaging().send({
                        notification: {
                            title: '🔴 FINAL NOTICE: Payment Due in 9 Hours!',
                            body: `LAST REMINDER! Pay ₹${loan.monthlyAmount.toLocaleString()} before midnight to avoid penalties and late fees. This is your final warning.`
                        },
                        data: {
                            type: 'PAYMENT_REMINDER_FINAL',
                            loanId: loanDoc.id,
                            amount: loan.monthlyAmount.toString(),
                            reminder: '3rd_of_3'
                        },
                        token: fcmToken,
                        android: {
                            priority: 'high'
                        }
                    });
                    
                    notificationsSent++;
                } catch (error) {
                    console.error(`Failed to send 3 PM notification:`, error);
                }
            }
            
            console.log(`Sent ${notificationsSent} final reminders at 3 PM`);
            return null;
        } catch (error) {
            console.error('Error in monthlyPaymentReminder3PM:', error);
            return null;
        }
    });

/**
 * Trigger: When a payment is created
 * Action: Send payment confirmation and stop reminders for this month
 */
exports.onPaymentCreated = functions.firestore
    .document('payments/{paymentId}')
    .onCreate(async (snap, context) => {
        try {
            const payment = snap.data();
            
            if (payment.status !== 'SUCCESS') {
                console.log('Payment not successful, skipping notification');
                return null;
            }
            
            console.log(`Payment created for loan ${payment.loanId} by user ${payment.userId}`);
            
            // Get loan details
            const loanDoc = await admin.firestore()
                .collection('loans')
                .doc(payment.loanId)
                .get();
            
            if (!loanDoc.exists) {
                console.log('Loan not found');
                return null;
            }
            
            const loan = loanDoc.data();
            
            // Get user FCM token
            const userDoc = await admin.firestore()
                .collection('users')
                .doc(payment.userId)
                .get();
            
            if (!userDoc.exists || !userDoc.data().fcmToken) {
                console.log('User or FCM token not found');
                return null;
            }
            
            const fcmToken = userDoc.data().fcmToken;
            
            // Send payment confirmation
            await admin.messaging().send({
                notification: {
                    title: '✅ Payment Received!',
                    body: `Thank you! We received your payment of ₹${payment.amount.toLocaleString()} for your ${loan.loanType} loan. Your next payment is due on the 10th of next month.`
                },
                data: {
                    type: 'PAYMENT_CONFIRMED',
                    loanId: payment.loanId,
                    paymentId: context.params.paymentId,
                    amount: payment.amount.toString()
                },
                token: fcmToken
            });
            
            console.log(`Sent payment confirmation to user ${payment.userId}`);
            
            return null;
        } catch (error) {
            console.error('Error in onPaymentCreated:', error);
            return null;
        }
    });

/**
 * Optional: Function to manually send a test notification
 * Call this from Firebase Console to test your setup
 */
exports.sendTestNotification = functions.https.onCall(async (data, context) => {
    try {
        const { userId, title, message } = data;
        
        if (!userId || !title || !message) {
            throw new functions.https.HttpsError(
                'invalid-argument',
                'userId, title, and message are required'
            );
        }
        
        // Get user token
        const userDoc = await admin.firestore()
            .collection('users')
            .doc(userId)
            .get();
        
        if (!userDoc.exists) {
            throw new functions.https.HttpsError('not-found', 'User not found');
        }
        
        const fcmToken = userDoc.data().fcmToken;
        
        if (!fcmToken) {
            throw new functions.https.HttpsError('not-found', 'FCM token not found for user');
        }
        
        await admin.messaging().send({
            notification: {
                title: title,
                body: message
            },
            token: fcmToken
        });
        
        return { success: true, message: 'Test notification sent' };
    } catch (error) {
        console.error('Error sending test notification:', error);
        throw error;
    }
});

