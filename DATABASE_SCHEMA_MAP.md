# 🗄️ **Firebase Firestore Database Schema Map**

## 📊 **Database Collections Structure**

### **1. `users` Collection**
**Primary Key**: `registrationCode` (used for authentication)
**Document ID**: Auto-generated or `registrationCode`

```json
{
  "userId": "auto-generated-id",
  "registrationCode": "OFJDH3", // Primary identifier for login
  "name": "Mehul Bhardva",
  "phone": "8980500235",
  "email": "mehul@example.com",
  "status": "active", // active, inactive, pending
  
  // Authentication System
  "password": "8980500235", // Initial password = mobile number
  "isFirstLogin": true, // Flag for first login
  "passwordChanged": false, // Track if password was changed
  "passwordChangeRequired": true, // Force password change on first login
  
  // Timestamps
  "dateCreated": "2025-01-22T11:00:00Z",
  "lastLogin": "2025-01-22T15:30:00Z",
  "passwordChangedAt": null,
  
  "profile": {
    "age": 25,
    "gender": "Male",
    "weight": 70.5,
    "height": 175.0,
    "goal": "Weight Loss",
    "medicalConditions": "None",
    "medications": "None",
    "allergies": "None",
    "emergencyContact": "Emergency Contact",
    "emergencyPhone": "9876543210"
  },
  
  "settings": {
    "notifications": true,
    "whatsappAlerts": true,
    "timezone": "Asia/Kolkata"
  }
}
```

### **2. `fasting_sessions` Collection**
**Primary Key**: Auto-generated ID
**Document ID**: Auto-generated

```json
{
  "sessionId": "auto-generated-id",
  "userId": "user-id-reference",
  "registrationCode": "OFJDH3", // For quick lookup
  "userName": "Mehul Bhardva", // For display without join
  
  "fastingStarted": "2025-01-22T11:00:00Z",
  "fastingEnded": "2025-01-23T03:01:00Z",
  "fastingHours": 16,
  
  "status": "completed", // active, completed, interrupted
  "notes": "Felt good during fasting",
  
  "createdAt": "2025-01-22T11:00:00Z",
  "updatedAt": "2025-01-23T03:01:00Z"
}
```

### **3. `form_submissions` Collection**
**Primary Key**: Auto-generated ID
**Document ID**: Auto-generated

```json
{
  "submissionId": "auto-generated-id",
  "registrationCode": "OFJDH3",
  "formData": {
    "name": "Mehul Bhardva",
    "phone": "8980500235",
    "email": "mehul@example.com",
    "age": 25,
    "gender": "Male",
    "weight": 70.5,
    "height": 175.0,
    "goal": "Weight Loss",
    "medicalConditions": "None",
    "medications": "None",
    "allergies": "None",
    "emergencyContact": "Emergency Contact",
    "emergencyPhone": "9876543210"
  },
  
  "status": "pending", // pending, processed, rejected
  "processedAt": null,
  "processedBy": null,
  
  "submittedAt": "2025-01-22T10:30:00Z",
  "formLink": "https://docs.google.com/forms/..."
}
```

## 🔐 **Authentication Flow**

### **Step 1: Initial Login**
1. **User enters registration code**: `OFJDH3`
2. **System checks**: User exists and is active
3. **First login check**: `isFirstLogin = true`
4. **Login successful**: But password change required

### **Step 2: Password Change (First Login)**
1. **System forces password change**: `passwordChangeRequired = true`
2. **User enters new password**: Min 6 chars, max 10 chars
3. **Password validation**: Check length and complexity
4. **Update user record**:
   - `password = "newPassword123"`
   - `isFirstLogin = false`
   - `passwordChanged = true`
   - `passwordChangeRequired = false`
   - `passwordChangedAt = "2025-01-22T15:35:00Z"`

### **Step 3: Subsequent Logins**
1. **User enters registration code**: `OFJDH3`
2. **User enters custom password**: `newPassword123`
3. **Authentication successful**: Access granted
4. **Update last login**: `lastLogin = "2025-01-22T16:00:00Z"`

## 📱 **Authentication API Endpoints**

### **Login Endpoint**
```javascript
// POST /auth/login
{
  "registrationCode": "OFJDH3",
  "password": "8980500235" // Initial password = mobile number
}

// Response
{
  "success": true,
  "user": {
    "userId": "user-id",
    "name": "Mehul Bhardva",
    "registrationCode": "OFJDH3",
    "isFirstLogin": true,
    "passwordChangeRequired": true
  },
  "token": "jwt-token"
}
```

### **Change Password Endpoint**
```javascript
// POST /auth/change-password
{
  "registrationCode": "OFJDH3",
  "oldPassword": "8980500235",
  "newPassword": "MyPass123" // 6-10 characters
}

// Response
{
  "success": true,
  "message": "Password changed successfully",
  "user": {
    "isFirstLogin": false,
    "passwordChangeRequired": false
  }
}
```

### **Login After Password Change**
```javascript
// POST /auth/login
{
  "registrationCode": "OFJDH3",
  "password": "MyPass123" // Custom password
}

// Response
{
  "success": true,
  "user": {
    "userId": "user-id",
    "name": "Mehul Bhardva",
    "registrationCode": "OFJDH3",
    "isFirstLogin": false,
    "passwordChangeRequired": false
  },
  "token": "jwt-token"
}
```

## 🔄 **Data Flow Process**

### **Step 1: New User Registration**
1. **Customer fills Google Form** → Data goes to `form_submissions`
2. **Admin processes form** → Creates user in `users` collection
3. **Initial password set** → `password = mobile_number`
4. **First login flag set** → `isFirstLogin = true`

### **Step 2: User First Login**
1. **User enters registration code + mobile number** → Authenticates
2. **System detects first login** → Forces password change
3. **User sets custom password** → 6-10 characters
4. **Password updated** → `isFirstLogin = false`

### **Step 3: Subsequent Logins**
1. **User enters registration code + custom password** → Authenticates
2. **Login successful** → Access to fasting tracking

### **Step 4: Fasting Tracking**
1. **User starts fasting** → Creates record in `fasting_sessions`
2. **User ends fasting** → Updates record with end time and hours
3. **WhatsApp notification** → Generated from fasting session data

## 🔐 **Security Rules**

### **Users Collection**
```javascript
// Users can only read their own data
match /users/{userId} {
  allow read: if request.auth != null && 
    (resource.data.registrationCode == request.auth.token.registrationCode ||
     request.auth.token.isAdmin == true);
  allow write: if request.auth != null && 
    request.auth.token.isAdmin == true;
  
  // Allow password change for own account
  allow update: if request.auth != null && 
    resource.data.registrationCode == request.auth.token.registrationCode &&
    request.resource.data.diff(resource.data).affectedKeys()
      .hasOnly(['password', 'isFirstLogin', 'passwordChanged', 'passwordChangeRequired', 'passwordChangedAt', 'lastLogin']);
}
```

### **Fasting Sessions Collection**
```javascript
// Users can read/write their own fasting sessions
match /fasting_sessions/{sessionId} {
  allow read, write: if request.auth != null && 
    resource.data.registrationCode == request.auth.token.registrationCode;
}
```

### **Form Submissions Collection**
```javascript
// Only admins can access form submissions
match /form_submissions/{submissionId} {
  allow read, write: if request.auth != null && 
    request.auth.token.isAdmin == true;
}
```

## 📊 **Migration from Google Sheets**

### **Current Sheet Data Structure**
- **Column A**: Client Name
- **Column B**: Mobile No.
- **Column C**: Registration Code
- **Column D**: Status
- **Column E**: Pre-filled Form Link
- **Column F**: Date Created
- **Column G**: WhatsApp Link

### **Migration Mapping**
```javascript
// Google Sheets → Firebase Users
{
  name: row[0],                    // Client Name
  phone: row[1],                   // Mobile No.
  registrationCode: row[2],        // Registration Code
  status: row[3],                  // Status
  formLink: row[4],               // Pre-filled Form Link
  dateCreated: row[5],            // Date Created
  whatsappLink: row[6],           // WhatsApp Link
  
  // New authentication fields
  password: row[1],               // Initial password = mobile number
  isFirstLogin: true,             // First login flag
  passwordChanged: false,         // Password change tracking
  passwordChangeRequired: true,   // Force password change
  passwordChangedAt: null,        // Password change timestamp
  lastLogin: null                 // Last login timestamp
}
```

## 🚀 **Benefits of This Structure**

### **For Users**
- ✅ **Simple initial login** with mobile number
- ✅ **Secure custom password** after first login
- ✅ **Track fasting sessions** easily
- ✅ **View history** of all fasting periods
- ✅ **Real-time updates** across devices

### **For Admins**
- ✅ **Process form submissions** efficiently
- ✅ **Manage user profiles** centrally
- ✅ **Generate reports** on fasting patterns
- ✅ **Send notifications** automatically
- ✅ **Track password changes** for security

### **For System**
- ✅ **Scalable** for thousands of users
- ✅ **Real-time sync** between devices
- ✅ **Offline support** for mobile app
- ✅ **Automatic backups** in Firebase
- ✅ **Secure authentication** with password requirements

## 📈 **Storage Estimates**

### **Free Tier Limits**
- **1GB storage** = ~50,000 users + 500,000 fasting sessions
- **50K reads/day** = ~1,600 daily active users
- **20K writes/day** = ~650 new registrations + 1,300 fasting sessions

### **Data Size Per Record**
- **User record**: ~2.5KB (includes auth fields)
- **Fasting session**: ~1KB
- **Form submission**: ~3KB

## 🎯 **Next Steps**

1. **Review this schema** - Does this match your requirements?
2. **Confirm authentication flow** - Is this the process you want?
3. **Approve structure** - Should I implement this in Firebase?
4. **Migration plan** - How to move existing data?

**Do you want me to proceed with implementing this database structure?** 