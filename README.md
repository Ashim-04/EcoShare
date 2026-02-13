# EcoShare - Sustainable Item Sharing Platform

EcoShare is an Android mobile application that enables users to donate, exchange, or auction unwanted items through a clean, modern interface. Built with Java and Firebase, the app promotes sustainability by facilitating item reuse and tracking environmental impact.

## Features

### Core Functionality
- **User Authentication**: Secure registration and login via Firebase Authentication
- **Item Sharing**: Donate, exchange, or auction items with image uploads
- **Live Auctions**: Real-time bidding system with countdown timers
- **Home Feed**: Browse all shared items with category filtering
- **User Profiles**: Display stats, badges, and contribution history

### Gamification & Impact
- **Badge System**: Earn badges for donation milestones and achievements
- **Leaderboard**: Top contributors ranked by donations
- **Eco-Impact Dashboard**: Track items reused and CO₂ saved

### Communication
- **Push Notifications**: Auction alerts, outbid notifications, badge achievements
- **In-App Chat**: Direct messaging for item inquiries

## Tech Stack

- **Language**: Java
- **IDE**: Android Studio
- **Backend**: Firebase
  - Authentication
  - Cloud Firestore
  - Realtime Database (for auctions)
  - Cloud Storage (for images)
  - Cloud Messaging (FCM)
- **UI**: Material Design 3
- **Image Loading**: Glide
- **Min SDK**: 24 (Android 7.0)
- **Target SDK**: 34 (Android 14)

## Project Structure

```
EcoShare/
├── app/
│   ├── src/
│   │   └── main/
│   │       ├── java/com/ecoshare/app/
│   │       │   ├── activities/     # All Activity classes
│   │       │   ├── adapters/       # RecyclerView adapters
│   │       │   ├── models/         # Data models
│   │       │   ├── services/       # Firebase services
│   │       │   └── utils/          # Utility classes
│   │       ├── res/
│   │       │   ├── layout/         # XML layouts
│   │       │   ├── values/         # Colors, strings, themes
│   │       │   ├── drawable/       # Icons and drawables
│   │       │   └── xml/            # Backup rules
│   │       └── AndroidManifest.xml
│   ├── build.gradle
│   └── google-services.json        # (Download from Firebase)
├── build.gradle
├── settings.gradle
└── FIREBASE_SETUP.md               # Detailed Firebase setup guide
```

## Getting Started

### Prerequisites

1. **Android Studio**: Arctic Fox or later
2. **Java Development Kit (JDK)**: Version 8 or higher
3. **Android SDK**: Level 24 or higher
4. **Firebase Account**: Free tier is sufficient

### Setup Instructions

1. **Clone the repository**
   ```bash
   git clone <repository-url>
   cd EcoShare
   ```

2. **Configure Firebase**
   - Follow the comprehensive guide in [FIREBASE_SETUP.md](FIREBASE_SETUP.md)
   - Download `google-services.json` and place it in the `app/` directory
   - Enable Firebase services: Authentication, Firestore, Realtime Database, Storage, FCM

3. **Open in Android Studio**
   - Open Android Studio
   - Select "Open an existing project"
   - Navigate to the project directory
   - Wait for Gradle sync to complete

4. **Build and Run**
   - Connect an Android device or start an emulator
   - Click the "Run" button or press `Shift+F10`

### Build from Command Line

```bash
# Windows
gradlew.bat build

# Linux/Mac
./gradlew build
```

## Firebase Configuration

The app requires the following Firebase services to be enabled:

- ✅ **Firebase Authentication** (Email/Password)
- ✅ **Cloud Firestore** (User data, items, notifications)
- ✅ **Realtime Database** (Live auction data)
- ✅ **Firebase Storage** (Profile images, item images)
- ✅ **Cloud Messaging** (Push notifications)

Refer to [FIREBASE_SETUP.md](FIREBASE_SETUP.md) for detailed setup instructions including security rules.

## Key Screens

1. **Splash Screen**: App launch and authentication check
2. **Login/Register**: User authentication
3. **Home Feed**: Browse all shared items
4. **Add Item**: Create new item listings
5. **Item Details**: View item information and actions
6. **Live Auction**: Real-time bidding interface
7. **Profile**: User stats and badges
8. **Leaderboard**: Top contributors
9. **Eco-Impact**: Environmental impact metrics
10. **Notifications**: Activity alerts
11. **Settings**: App preferences
12. **Chat**: Direct messaging (optional)

## Data Models

### Firestore Collections

- **users**: User profiles, stats, badges
- **items**: Item listings (donation/exchange/auction)
- **notifications**: User notifications
- **chats/messages**: Chat conversations

### Realtime Database

- **auctions**: Live auction data with real-time bidding

## Development Status

✅ **All features complete and ready for production**

- [x] Project Setup & Configuration
- [x] Core Models & Utilities
- [x] Authentication System
- [x] User Profile System
- [x] Item Listing & Home Feed
- [x] Live Auction System
- [x] Gamification & Leaderboard
- [x] Notifications System
- [x] Settings & Navigation
- [x] Optional Chat Feature
- [x] UI Polish & Testing

## Testing Procedures

### Manual Testing

**Authentication Flow**:
1. Launch app → should show splash screen
2. Register new account with email/password
3. Verify user document created in Firestore
4. Logout and login again
5. Verify session persistence after app restart

**Item Listing Flow**:
1. Tap FAB button on home screen
2. Select category (Electronics, Furniture, Clothing, Books, Other)
3. Upload 1-5 images from camera or gallery
4. Fill in title, description, condition
5. Choose type: Donation, Exchange, or Auction
6. For auction: set starting price and duration
7. Submit and verify item appears in feed

**Live Auction Flow**:
1. Navigate to auction item details
2. Start auction (if owner) or join existing auction
3. Place bids above current price
4. Verify real-time updates for all participants
5. Monitor countdown timer
6. Verify automatic winner selection when timer ends
7. Check notifications sent to outbid users

**Gamification Testing**:
1. Mark items as donated in item details
2. Verify badges awarded at milestones (1, 5, 10, 25, 50 donations)
3. Check leaderboard updates with new donations
4. View eco-impact dashboard for CO₂ calculations

**Chat Testing**:
1. Initiate chat from item details page
2. Send and receive messages in real-time
3. Verify unread badge updates
4. Check message persistence

**Notifications Testing**:
1. Get outbid in auction → verify notification received
2. Win auction → verify winner notification
3. Earn badge → verify badge notification
4. Check notification permissions on Android 13+

### Build Commands

```bash
# Clean build
./gradlew clean

# Debug build
./gradlew assembleDebug

# Release build (requires signing)
./gradlew assembleRelease

# Install on device
./gradlew installDebug

# Run tests
./gradlew test
```

### Device Requirements

- **Minimum**: Android 7.0 (API 24)
- **Target**: Android 14 (API 34)
- **RAM**: 2GB minimum
- **Storage**: 100MB for app + space for images
- **Internet**: Required for all features

## Troubleshooting

### Common Issues

**Build Errors**:
- Ensure `google-services.json` is in `app/` folder
- Sync Gradle files (File → Sync Project with Gradle Files)
- Clean and rebuild project
- Check internet connection for dependency downloads

**Authentication Issues**:
- Verify Email/Password provider is enabled in Firebase Console
- Check SHA-1 fingerprint is added to Firebase project
- Clear app data and retry

**Image Upload Failures**:
- Verify Storage permissions granted
- Check Firebase Storage rules are correct
- Ensure images are under 10MB
- Verify internet connection

**Auction Not Updating**:
- Check Realtime Database rules allow read/write
- Verify internet connection is stable
- Ensure user is authenticated

**Notifications Not Working**:
- Grant notification permission (Settings → Apps → EcoShare → Notifications)
- Verify FCM is enabled in Firebase Console
- Check device has Play Services installed
- Restart app after granting permissions

## Contributing

This is a learning project. Contributions are welcome!

## License

This project is licensed under the MIT License.

## Support

For issues or questions:
1. Check [FIREBASE_SETUP.md](FIREBASE_SETUP.md) for configuration help
2. Review Firebase Console for service status
3. Check Android Studio logs for build errors
4. See [UI_POLISH_SUMMARY.md](UI_POLISH_SUMMARY.md) for testing details

## Additional Documentation

- **[FIREBASE_SETUP.md](FIREBASE_SETUP.md)**: Complete Firebase configuration guide
- **[UI_POLISH_SUMMARY.md](UI_POLISH_SUMMARY.md)**: UI/UX improvements and testing
- **[report.md](.zenflow/tasks/new-task-e9c3/report.md)**: Implementation summary

---

**Built with ❤️ for a sustainable future**
