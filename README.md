# LifeStyle R Android App

A modern Android application built with Clean Architecture, following Material 3 design principles and best practices.

## 🏗️ Architecture

This project follows **Clean Architecture** principles with the following layers:

### 📁 Project Structure

```
app/src/main/java/com/lifestyler/android/
├── data/                           # Data Layer
│   ├── datasource/                 # Data Sources
│   │   ├── AuthLocalDataSource.kt
│   │   ├── AuthLocalDataSourceImpl.kt
│   │   ├── AuthRemoteDataSource.kt
│   │   └── AuthRemoteDataSourceImpl.kt
│   └── repository/                 # Repository Implementations
│       └── AuthRepositoryImpl.kt
├── domain/                         # Domain Layer
│   ├── entity/                     # Domain Entities
│   │   ├── User.kt
│   │   └── AuthResult.kt
│   ├── repository/                 # Repository Interfaces
│   │   └── AuthRepository.kt
│   └── usecase/                    # Use Cases
│       ├── LoginUseCase.kt
│       └── RegisterUseCase.kt
├── presentation/                   # Presentation Layer
│   ├── auth/                       # Authentication Flow
│   │   ├── AuthActivity.kt
│   │   ├── LoginFragment.kt
│   │   └── viewmodel/
│   │       └── LoginViewModel.kt
│   └── main/                       # Main App Flow
│       ├── MainActivity.kt
│       ├── home/
│       ├── profile/
│       ├── settings/
│       ├── patients/
│       └── appointments/
├── di/                             # Dependency Injection
│   └── AppModule.kt
└── LifeStyleApplication.kt         # Application Class
```

## 🚀 Features Implemented

### ✅ Authentication Flow
- **Login Screen** with email/password validation
- **Register Screen** (placeholder)
- **Forgot Password Screen** (placeholder)
- **Email Verification** (placeholder)
- **MVI Pattern** with StateFlow for state management

### ✅ Navigation
- **Auth Activity** for authentication flow
- **Main Activity** with bottom navigation
- **Navigation Component** for fragment navigation
- **Bottom Navigation** with 5 main sections:
  - Home
  - Patients
  - Appointments
  - Profile
  - Settings

### ✅ Architecture Components
- **Clean Architecture** with proper separation of concerns
- **Repository Pattern** for data management
- **Dependency Injection** with Hilt
- **MVVM** with ViewModels
- **MVI Pattern** for state management
- **Material 3** design system

### ✅ Dependencies
- **Hilt** for dependency injection
- **Navigation Component** for navigation
- **Material 3** for UI components
- **ViewBinding** for view binding
- **Coroutines & Flow** for async operations
- **Room** for local database (configured)
- **Retrofit** for API calls (configured)

## 🛠️ Setup Instructions

### Prerequisites
- Android Studio Arctic Fox or later
- JDK 17
- Android SDK 34
- Minimum SDK: 24

### Build & Run
1. Clone the repository
2. Open the project in Android Studio
3. Sync Gradle files
4. Run the app on an emulator or device

### Test Credentials
For testing the login functionality:
- **Email**: `test@example.com`
- **Password**: `password`

## 📱 App Flow

1. **Launch** → AuthActivity (Login Screen)
2. **Login Success** → MainActivity with bottom navigation
3. **Bottom Navigation**:
   - **Home**: Dashboard with quick actions
   - **Patients**: Patient management (placeholder)
   - **Appointments**: Appointment scheduling (placeholder)
   - **Profile**: User profile management (placeholder)
   - **Settings**: App settings (placeholder)

## 🎨 Design System

- **Material 3** design language
- **Custom color scheme** with green primary color
- **Responsive layouts** using ConstraintLayout
- **Accessibility** features implemented
- **Dark/Light theme** support

## 🔧 Next Steps

### Immediate Tasks
1. **Complete Authentication Flow**:
   - Implement RegisterFragment
   - Implement ForgotPasswordFragment
   - Add proper validation
   - Connect to real API

2. **Implement Core Features**:
   - Patient management (CRUD operations)
   - Appointment scheduling
   - User profile management
   - Settings functionality

3. **Add Data Layer**:
   - Implement Room database
   - Add real API integration
   - Implement caching strategy

### Advanced Features
1. **Testing**:
   - Unit tests for use cases
   - Integration tests for repositories
   - UI tests for fragments

2. **Performance**:
   - Image loading with Glide
   - Pagination for lists
   - Offline support

3. **Security**:
   - Biometric authentication
   - Secure storage
   - API security

## 📋 Code Guidelines

### Kotlin Best Practices
- Use **camelCase** for variables and functions
- Use **PascalCase** for classes
- Use **UPPERCASE** for constants
- Prefer **val** over **var** when possible
- Use **data classes** for entities

### Architecture Guidelines
- Follow **SOLID principles**
- Use **dependency injection**
- Implement **repository pattern**
- Use **MVI pattern** for state management
- Keep functions **small and focused**

### UI Guidelines
- Use **Material 3** components
- Follow **Material Design** principles
- Implement **responsive layouts**
- Add **accessibility** features
- Use **ConstraintLayout** for complex layouts

## 🤝 Contributing

1. Follow the established architecture patterns
2. Write clean, readable code
3. Add proper documentation
4. Include unit tests for new features
5. Follow Material Design guidelines

## 📄 License

This project is licensed under the MIT License. 