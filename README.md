# Namma Yantra

An Android application for agricultural machinery rental and booking, built with Kotlin and Firebase.

## Features

### For Farmers/Renters
- **Browse Machines**: View available agricultural machinery in your area
- **Search & Filter**: Find machines by type, availability, and location
- **Book Machines**: Reserve machinery for your farming needs with hourly pricing
- **Booking History**: Track your past rentals and upcoming bookings
- **Map Integration**: View machine locations and delivery areas using OpenStreetMap

### For Machine Owners
- **Add Machinery**: List your agricultural equipment for rent
- **Manage Requests**: Handle booking requests from renters
- **Location Services**: Set delivery radius and pickup locations

## Technology Stack

- **Language**: Kotlin
- **Platform**: Android (Min SDK 24, Target SDK 36)
- **Backend**: Firebase (Authentication & Firestore)
- **Maps**: OSMDroid (OpenStreetMap)
- **UI**: Material Design Components, View Binding
- **Architecture**: MVVM with Firebase Repository pattern

## Getting Started

### Prerequisites
- Android Studio Arctic Fox or later
- Android device or emulator with API level 24+
- Firebase project with Authentication and Firestore enabled

### Installation

1. Clone the repository:
   ```bash
   git clone <repository-url>
   cd Namma_yantra2/app
   ```

2. Open the project in Android Studio

3. Configure Firebase:
   - Copy your `google-services.json` to the `app/` directory
   - Enable Authentication and Firestore in your Firebase console

4. Build and run the app on your device/emulator

## Project Structure

```
app/src/main/java/com/example/namma_yantra/
├── MainActivity.kt          # Main dashboard with machine listings
├── AuthActivity.kt          # User authentication
├── AddMachineActivity.kt    # Add new machinery (owners)
├── BookingActivity.kt       # Book machines
├── HistoryActivity.kt       # View booking history
├── OwnerRequestsActivity.kt # Manage booking requests (owners)
├── DeliveryMapActivity.kt   # Map view for delivery locations
├── Machine.kt               # Data model for machinery
├── Booking.kt               # Data model for bookings
├── UserProfile.kt           # User profile data model
├── FirebaseRepository.kt    # Firebase data operations
└── Adapters/                 # RecyclerView adapters
```

## Key Dependencies

- Firebase BOM: Platform for Auth and Firestore
- OSMDroid: OpenStreetMap integration for maps
- Material Components: Modern Android UI components
- View Binding: Type-safe view access
- RecyclerView & CardView: List and card layouts

## Permissions

- `INTERNET`: For Firebase and map services
- `ACCESS_NETWORK_STATE`: Network connectivity checks

## Contributing

1. Fork the repository
2. Create a feature branch
3. Commit your changes
4. Push to the branch
5. Create a Pull Request

## Images
### Home Page
<img width="500" height="700" alt="image" src="https://github.com/user-attachments/assets/cca35a6c-1780-457e-af4f-9c7ba234a507" />


## License

This project is licensed under the MIT License - see the LICENSE file for details.
