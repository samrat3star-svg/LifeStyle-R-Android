# 🔥 **Firebase Database Setup Instructions**

## 📋 **Overview**

This guide will help you set up the Firebase Firestore database for your LifeStyle-R fasting tracking system with authentication using registration codes and mobile numbers as initial passwords.

## 🚀 **Step 1: Firebase Project Setup**

### **1.1 Create Firebase Project**
1. Go to [Firebase Console](https://console.firebase.google.com/)
2. Click **"Create a project"**
3. Enter project name: **"LifeStyle-R"**
4. Enable Google Analytics (optional)
5. Click **"Create project"**

### **1.2 Enable Firestore Database**
1. In Firebase Console, go to **"Firestore Database"**
2. Click **"Create database"**
3. Choose **"Start in test mode"** (we'll add security rules later)
4. Select location: **"asia-south1 (Mumbai)"**
5. Click **"Done"**

## 🔐 **Step 2: Set Up Security Rules**

### **2.1 Apply Security Rules**
1. In Firestore Database, go to **"Rules"** tab
2. Replace the default rules with the content from `firebase_database_setup.js`
3. Click **"Publish"**

### **2.2 Security Rules Overview**
- **Users**: Can only access their own data, admins can access all
- **Fasting Sessions**: Users can manage their own sessions
- **Form Submissions**: Admin-only access

## 📊 **Step 3: Create Database Collections**

### **3.1 Create Collections**
1. Go to **"Data"** tab in Firestore
2. Click **"Start collection"**
3. Create these collections:
   - `users`
   - `fasting_sessions`
   - `form_submissions`

## 🔄 **Step 4: Migrate Your Data**

### **4.1 Run Migration Script**
1. Open your Google Sheet
2. Go to **Extensions** → **Apps Script**
3. Create new script file
4. Copy content from `firebase_migration_script.js`
5. Run the `migrateToFirebase()` function
6. Check the **"Firebase Migration Summary"** sheet

### **4.2 Import Data to Firebase**
1. Go to Firebase Console → **Firestore Database** → **Data**
2. **Import Users:**
   - Click on `users` collection
   - Click **"Import JSON"**
   - Copy JSON from migration summary sheet
   - Click **"Import"**
3. **Import Form Submissions:**
   - Click on `form_submissions` collection
   - Click **"Import JSON"**
   - Copy JSON from migration summary sheet
   - Click **"Import"**

## 🔐 **Step 5: Test Authentication Flow**

### **5.1 Test Login Flow**
Use the authentication API functions from `firebase_authentication_api.js`:

```javascript
// Test first login
const loginResult = await loginUser('OFJDH3', '8980500235');
// Should return: { success: true, requiresPasswordChange: true }

// Test password change
const changeResult = await changePassword('OFJDH3', '8980500235', 'MyPass123');
// Should return: { success: true, message: 'Password changed successfully' }

// Test subsequent login
const loginResult2 = await loginUser('OFJDH3', 'MyPass123');
// Should return: { success: true, requiresPasswordChange: false }
```

## 📱 **Step 6: Authentication Flow**

### **6.1 First Login Process**
1. **User enters:**
   - Registration Code: `OFJDH3`
   - Password: `8980500235` (mobile number)
2. **System responds:**
   - Login successful
   - Password change required
3. **User changes password:**
   - New password: `MyPass123` (6-10 characters)
4. **System updates:**
   - `isFirstLogin = false`
   - `passwordChanged = true`
   - `passwordChangeRequired = false`

### **6.2 Subsequent Logins**
1. **User enters:**
   - Registration Code: `OFJDH3`
   - Password: `MyPass123` (custom password)
2. **System responds:**
   - Login successful
   - Access granted to fasting tracking

## 🗄️ **Database Structure**

### **Users Collection**
```json
{
  "registrationCode": "OFJDH3",
  "name": "Mehul Bhardva",
  "phone": "8980500235",
  "password": "8980500235", // Initial = mobile number
  "isFirstLogin": true,
  "passwordChanged": false,
  "passwordChangeRequired": true,
  "status": "active",
  "dateCreated": "2025-01-22T11:00:00Z",
  "lastLogin": null,
  "profile": { /* user profile data */ },
  "settings": { /* user settings */ }
}
```

### **Fasting Sessions Collection**
```json
{
  "sessionId": "auto-generated",
  "userId": "user-id",
  "registrationCode": "OFJDH3",
  "userName": "Mehul Bhardva",
  "fastingStarted": "2025-01-22T11:00:00Z",
  "fastingEnded": "2025-01-23T03:01:00Z",
  "fastingHours": 16,
  "status": "completed",
  "notes": "Felt good during fasting",
  "createdAt": "2025-01-22T11:00:00Z",
  "updatedAt": "2025-01-23T03:01:00Z"
}
```

### **Form Submissions Collection**
```json
{
  "submissionId": "auto-generated",
  "registrationCode": "OFJDH3",
  "formData": { /* form submission data */ },
  "status": "pending",
  "processedAt": null,
  "processedBy": null,
  "submittedAt": "2025-01-22T10:30:00Z",
  "formLink": "https://docs.google.com/forms/..."
}
```

## 🔧 **Step 7: API Integration**

### **7.1 Authentication Endpoints**
```javascript
// Login
POST /auth/login
{
  "registrationCode": "OFJDH3",
  "password": "8980500235"
}

// Change Password
POST /auth/change-password
{
  "registrationCode": "OFJDH3",
  "oldPassword": "8980500235",
  "newPassword": "MyPass123"
}

// Get User Profile
GET /auth/profile/{registrationCode}
```

### **7.2 Fasting Sessions Endpoints**
```javascript
// Start Fasting
POST /fasting/start
{
  "registrationCode": "OFJDH3"
}

// End Fasting
POST /fasting/end
{
  "sessionId": "session-id",
  "notes": "Felt good during fasting"
}

// Get Fasting History
GET /fasting/history/{registrationCode}
```

## 📈 **Step 8: Monitoring and Analytics**

### **8.1 Firebase Analytics**
1. Go to **Analytics** in Firebase Console
2. Monitor user engagement
3. Track authentication events
4. Monitor database usage

### **8.2 Usage Limits (Free Tier)**
- **Storage**: 1GB (≈ 50,000 users)
- **Reads**: 50K/day (≈ 1,600 daily users)
- **Writes**: 20K/day (≈ 650 new registrations)
- **Deletes**: 20K/day

## 🚨 **Step 9: Security Best Practices**

### **9.1 Password Security**
- Initial password = mobile number
- Force password change on first login
- Password requirements: 6-10 characters
- Store passwords securely (consider hashing)

### **9.2 Data Protection**
- Users can only access their own data
- Admins have full access for management
- Form submissions are admin-only
- Regular backups enabled

## 🎯 **Step 10: Next Steps**

### **10.1 Android App Integration**
1. Add Firebase SDK to Android project
2. Implement authentication flow
3. Add fasting session tracking
4. Test with real users

### **10.2 Web Dashboard**
1. Create admin dashboard
2. User management interface
3. Form submission processing
4. Analytics and reporting

### **10.3 WhatsApp Integration**
1. Connect with existing WhatsApp script
2. Send notifications from Firebase
3. Track message delivery
4. Generate reports

## 📞 **Support**

If you encounter issues:
1. Check Firebase Console logs
2. Verify security rules
3. Test authentication flow
4. Review migration data

## ✅ **Checklist**

- [ ] Firebase project created
- [ ] Firestore database enabled
- [ ] Security rules applied
- [ ] Collections created
- [ ] Data migrated from Google Sheets
- [ ] Authentication flow tested
- [ ] Password change working
- [ ] User profiles accessible
- [ ] Fasting sessions tracking ready
- [ ] Form submissions processing ready

**Your Firebase database is now ready for the LifeStyle-R fasting tracking system! 🚀** 