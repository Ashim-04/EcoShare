# Firebase Setup Instructions for EcoShare

## Prerequisites
- Android Studio installed
- Google account
- Internet connection

## Step 1: Create Firebase Project

1. Go to [Firebase Console](https://console.firebase.google.com/)
2. Click "Add project" or "Create a project"
3. Enter project name: `EcoShare` (or your preferred name)
4. Enable/disable Google Analytics (recommended: enable)
5. Choose Analytics account or create new one
6. Click "Create project"

## Step 2: Add Android App to Firebase Project

1. In Firebase Console, click the Android icon to add an Android app
2. Enter the following details:
   - **Android package name**: `com.ecoshare.app`
   - **App nickname** (optional): `EcoShare`
   - **Debug signing certificate SHA-1** (optional, but recommended for Auth):
     - Open terminal in your project directory
     - Run: `keytool -list -v -keystore ~/.android/debug.keystore -alias androiddebugkey -storepass android -keypass android` (Mac/Linux)
     - Or: `keytool -list -v -keystore "%USERPROFILE%\.android\debug.keystore" -alias androiddebugkey -storepass android -keypass android` (Windows)
     - Copy the SHA-1 certificate fingerprint
3. Click "Register app"

## Step 3: Download google-services.json

1. Download the `google-services.json` file
2. Move it to the `app/` directory of your project:
   ```
   EcoShare/
   └── app/
       └── google-services.json  ← Place here
   ```
3. **IMPORTANT**: This file contains sensitive information and is already added to `.gitignore`

## Step 4: Enable Firebase Services

### 4.1 Enable Firebase Authentication

1. In Firebase Console, go to **Build** → **Authentication**
2. Click "Get started"
3. Go to **Sign-in method** tab
4. Enable **Email/Password** provider:
   - Click on "Email/Password"
   - Toggle "Enable" to ON
   - Click "Save"

### 4.2 Set up Cloud Firestore

1. In Firebase Console, go to **Build** → **Firestore Database**
2. Click "Create database"
3. Choose **Start in test mode** (for development)
   - **IMPORTANT**: Change to production rules before launching
4. Select Cloud Firestore location (choose closest to your users)
5. Click "Enable"

### 4.3 Set up Realtime Database

1. In Firebase Console, go to **Build** → **Realtime Database**
2. Click "Create Database"
3. Choose database location
4. Start in **test mode** (for development)
   - **IMPORTANT**: Update rules before production
5. Click "Enable"

### 4.4 Set up Firebase Storage

1. In Firebase Console, go to **Build** → **Storage**
2. Click "Get started"
3. Start in **test mode** (for development)
4. Choose storage location
5. Click "Done"

### 4.5 Set up Firebase Cloud Messaging (FCM)

1. In Firebase Console, go to **Build** → **Cloud Messaging**
2. No additional setup needed if you completed Step 2
3. The app will automatically register for FCM when it runs

## Step 5: Configure Security Rules (IMPORTANT)

### Firestore Security Rules

1. Go to **Firestore Database** → **Rules** tab
2. Replace with the following rules:

```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    
    function isAuthenticated() {
      return request.auth != null;
    }
    
    function isOwner(userId) {
      return isAuthenticated() && request.auth.uid == userId;
    }
    
    match /users/{userId} {
      allow read: if isAuthenticated();
      allow create: if isAuthenticated();
      allow update, delete: if isOwner(userId);
    }
    
    match /items/{itemId} {
      allow read: if isAuthenticated();
      allow create: if isAuthenticated();
      allow update, delete: if isAuthenticated() && 
        resource.data.userId == request.auth.uid;
    }
    
    match /notifications/{notificationId} {
      allow read: if isAuthenticated() && 
        resource.data.userId == request.auth.uid;
      allow write: if isAuthenticated();
    }
    
    match /chats/{chatId} {
      allow read, write: if isAuthenticated() && 
        (request.auth.uid in resource.data.participants);
    }
    
    match /chats/{chatId}/messages/{messageId} {
      allow read: if isAuthenticated();
      allow create: if isAuthenticated();
    }
  }
}
```

3. Click "Publish"

### Realtime Database Security Rules

1. Go to **Realtime Database** → **Rules** tab
2. Replace with the following rules:

```json
{
  "rules": {
    "auctions": {
      "$auctionId": {
        ".read": true,
        ".write": "auth != null"
      }
    }
  }
}
```

3. Click "Publish"

### Storage Security Rules

1. Go to **Storage** → **Rules** tab
2. Replace with the following rules:

```javascript
rules_version = '2';
service firebase.storage {
  match /b/{bucket}/o {
    match /profile_images/{userId}/{allPaths=**} {
      allow read: if true;
      allow write: if request.auth != null && request.auth.uid == userId;
    }
    
    match /item_images/{itemId}/{allPaths=**} {
      allow read: if true;
      allow write: if request.auth != null;
    }
  }
}
```

3. Click "Publish"

## Step 6: Create Firestore Indexes (Optional but Recommended)

For better query performance, create composite indexes:

1. Go to **Firestore Database** → **Indexes** tab
2. Add these composite indexes:

   **Index 1 - Items by category and timestamp**:
   - Collection: `items`
   - Fields:
     - `category` (Ascending)
     - `timestamp` (Descending)
   - Query scope: Collection

   **Index 2 - Items by type and timestamp**:
   - Collection: `items`
   - Fields:
     - `type` (Ascending)
     - `timestamp` (Descending)
   - Query scope: Collection

   **Index 3 - User notifications**:
   - Collection: `notifications`
   - Fields:
     - `userId` (Ascending)
     - `timestamp` (Descending)
   - Query scope: Collection

## Step 7: Build and Run the App

1. Open the project in Android Studio
2. Wait for Gradle sync to complete
3. Ensure `google-services.json` is in the `app/` directory
4. Connect an Android device or start an emulator
5. Click the "Run" button or press Shift+F10

## Troubleshooting

### Google Services Plugin Error
- **Error**: "File google-services.json is missing"
- **Solution**: Ensure `google-services.json` is in the `app/` folder

### Authentication Failed
- **Error**: Users can't sign up or log in
- **Solution**: 
  - Check that Email/Password is enabled in Firebase Console
  - Verify `google-services.json` is correct
  - Check internet connection

### Firestore Permission Denied
- **Error**: "Missing or insufficient permissions"
- **Solution**: 
  - Verify security rules are published
  - Check user is authenticated
  - Ensure rules match your data structure

### Storage Upload Failed
- **Error**: "Upload failed" or permission errors
- **Solution**:
  - Check Storage rules are correct
  - Verify user is authenticated
  - Check file size (Firebase has limits)

### FCM Notifications Not Received
- **Solution**:
  - Ensure FCM is enabled in Firebase Console
  - Check notification permissions on device (Android 13+)
  - Verify app is in foreground/background as expected
  - Check FCM token is being saved

## Production Checklist

Before deploying to production:

- [ ] Update Firestore security rules to production mode
- [ ] Update Realtime Database rules to production mode
- [ ] Update Storage rules to production mode
- [ ] Set up proper authentication flows
- [ ] Enable Firebase App Check (recommended)
- [ ] Set up Firebase Analytics events
- [ ] Configure proper budget alerts in Firebase Console
- [ ] Test all features thoroughly
- [ ] Generate release SHA-1 and add to Firebase project
- [ ] Update ProGuard rules if using code obfuscation

## Additional Resources

- [Firebase Documentation](https://firebase.google.com/docs)
- [Firebase Android Setup](https://firebase.google.com/docs/android/setup)
- [Firestore Security Rules](https://firebase.google.com/docs/firestore/security/get-started)
- [Firebase Authentication](https://firebase.google.com/docs/auth)
- [Firebase Cloud Messaging](https://firebase.google.com/docs/cloud-messaging)
