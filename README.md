# Cinemeteor (Android – Jetpack Compose)

## Project Idea

The **Cinemeteor** is a modern Android application that allows users to explore movies using **The Movie Database (TMDB) API**. Users can browse trending and popular movies, search for specific titles, and view detailed information like synopsis, rating, release date, trailers, similar movies, and reviews. The app features user authentication with Firebase, profile management, wishlist functionality with cloud sync, light/dark mode, and multi-language support (English/Arabic). The app is built using **Kotlin**, **Jetpack Compose**, **Retrofit**, **Firebase**, and **SharedPreferences**, following modern Android development practices and clean architecture principles.

---

## Team Members

* Marwan Amr Saad
* Ahmed Mohamed Ismail
* Ahmed Mostafa Anwar
* Sara Amgad Abdelalim
* Christine Medhat Mounir
* Ahmed Reda

---

## Features

✅ Browse **Popular** Movies
✅ Browse **Trending** Movies
✅ Browse **Top Rated** Movies
✅ Browse **Now Playing** Movies
✅ **Search** Movies by Title
✅ **Movie Details Screen** with trailers, similar movies, and reviews
✅ View **Movie Ratings** and Details
✅ **User Authentication** (Email/Password & Google Sign-in)
✅ **User Profile** with edit functionality
✅ **Wishlist/Favorites** (Local & Cloud Sync with Firebase)
✅ **Light/Dark Mode** support
✅ **Multi-language** support (English & Arabic)
✅ **Splash Screen** with authentication check
✅ **Onboarding Screen** for new users
✅ **About Us** page with team information
✅ **Notifications** system
✅ **Download App** web page
✅ **Modern UI** using Jetpack Compose
✅ **TMDB API Integration** with Retrofit
✅ Clean **MVVM Architecture**
✅ Secure API key management
✅ Fast and responsive performance
✅ **Firebase Integration** (Authentication, Firestore, Analytics)

---

## Roles & Responsibilities

| Team Member | Responsibilities |
|------------|-------------------|
| **Ahmed Esmail** | User's profile page, About Us page, Light mode and Dark mode, Translation (English/Arabic) |
| **Ahmed Mostafa** | TMDB API handling and testing, Similar movies feature, Reviews section in FilmActivity, Download app web page, swipe to refresh and loading screen |
| **Ahmed Reda** | Notifications system, Splash screens, Onboarding Screen, Navigation Bar, Home Activity Design |
| **Marwan Amr** | Login and Signup functionality, Login with Google, Firebase handling |
| **Christine Medhat** | Firebase handling, Wishlist/Favorites feature, Film details page |
| **Sara Amgad** | Firebase handling, Wishlist/Favorites feature, Film details page |

---

## KPIs (Key Performance Indicators)

* Fast loading time **(<2 seconds)** for popular movies
* **Zero critical crashes** during testing
* **Responsive UI** across Android screen sizes
* **Successful TMDB API communication** (95% success rate)
* **Clean modular code** following MVVM
* **Complete documentation + README**

---

## Technologies Used

| Category        | Tools                          |
| --------------- | ------------------------------ |
| Language        | Kotlin                         |
| UI              | Jetpack Compose, Material UI 3 |
| Networking      | Retrofit + OkHttp              |
| Architecture    | MVVM + Repository Pattern      |
| Local Storage   | SharedPreferences, Firebase Firestore |
| Backend         | Firebase (Auth, Firestore, Analytics) |
| API             | TMDB REST API, IMGBB API      |
| Image Loading   | Coil                           |
| Testing         | JUnit, Mockito, Espresso       |
| Web             | React, TypeScript, Vite        |
| Version Control | Git & GitHub                   |
| Build           | Gradle                         |

---

## Setup Instructions

### Prerequisites
- Android Studio
- TMDB API Key (See [API_KEY_SETUP.md](API_KEY_SETUP.md) for detailed instructions)

### Getting Started

1. **Clone the repository**
   ```bash
   git clone https://github.com/DEPI-3-Android/Cinemeteor.git
   ```

2. **Add API Keys**
   - Get your TMDB API key from [TMDB](https://www.themoviedb.org/settings/api)
   - Get your IMGBB API key from [IMGBB](https://api.imgbb.com/)
   - Add them to `local.properties`:
     ```
     TMDB_API_KEY=your_tmdb_api_key_here
     IMGBB_API_KEY=your_imgbb_api_key_here
     ```
   - Add your Firebase configuration file (`google-services.json`) to the `app/` directory

3. **Sync and Run**
   - Sync Gradle files
   - Run the app on an emulator or device

For detailed API key setup instructions, see [API_KEY_SETUP.md](API_KEY_SETUP.md).

---

## Project Structure

```
app/src/main/java/com/acms/cinemeteor/
├── api/                    # API service interfaces
│   ├── TMDBApiService.kt  # TMDB API endpoints
│   └── RetrofitClient.kt  # Retrofit configuration
├── models/                 # Data models
│   └── Movie.kt           # Movie data class
├── repository/             # Data layer
│   └── MovieRepository.kt # Repository for API calls
├── viewmodel/              # ViewModel layer
│   └── MovieViewModel.kt  # UI state management
├── utils/                  # Utility classes
│   ├── ImageUtils.kt      # Image URL helpers
│   └── LanguageUtils.kt  # Language management
├── ui/                     # UI components
│   ├── components/        # Reusable UI components
│   │   └── LoadingScreen.kt
│   └── theme/             # App theming
├── OnBoardingScreen/      # Onboarding flow
│   ├── OnBoarding.kt
│   ├── OnBoardingData.kt
│   └── OnBoardingScreen.kt
├── MainActivity.kt         # Main entry point
├── LoginActivity.kt        # User login
├── SignupActivity.kt       # User registration
├── ProfileActivity.kt      # User profile
├── EditProfileActivity.kt  # Edit profile
├── FilmActivity.kt         # Movie details screen
├── FavoriteActivity.kt     # Local favorites
├── CloudSavedActivity.kt   # Cloud favorites
├── AboutActivity.kt        # About us page
├── SplashScreen.kt         # Splash screen
├── HomeScreen.kt           # Home screen
├── CreateAccountActivity.kt # Account creation
└── NavigationBar.kt        # Bottom navigation

web/promo/                 # Download app web page
├── src/
│   ├── download_app.tsx
│   └── main.tsx
└── index.html
```

---

## API Integration

The app uses the TMDB API v3 with the following endpoints:
- `GET /movie/popular` - Popular movies
- `GET /trending/movie/day` - Trending movies
- `GET /search/movie` - Search movies
- `GET /movie/now_playing` - Now playing movies
- `GET /movie/top_rated` - Top rated movies
- `GET /movie/{id}` - Movie details
- `GET /movie/{id}/similar` - Similar movies
- `GET /movie/{id}/reviews` - Movie reviews
- `GET /movie/{id}/videos` - Movie trailers/videos

The app also integrates with:
- **Firebase Authentication** - User authentication and Google Sign-in
- **Firebase Firestore** - Cloud storage for user favorites
- **IMGBB API** - Profile image uploads

---

## Instructor

**Ahmed Atef**

---

## Project Files

You can find the full project here:
👉 **https://github.com/DEPI-3-Android/Cinemeteor**

---

## License

This project is licensed under the **MIT License**.
