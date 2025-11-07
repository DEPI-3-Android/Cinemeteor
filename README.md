# Cinemeteor (Android – Jetpack Compose)

## Project Idea

The **Cinemeteor** is a modern Android application that allows users to explore movies using **The Movie Database (TMDB) API**. Users can browse trending and popular movies, search for specific titles, and view detailed information like synopsis, rating, release date, and more. The app is built using **Kotlin**, **Jetpack Compose**, **Retrofit**, and **Room**, following modern Android development practices and clean architecture principles.

---

## Team Members

* Marwan Amr Saad
* Ahmed Mohamed Ismail
* Ahmed Mostafa Anwar
* Sara Amgad Abdelalim
* Christine Medhat Mounir
* Ahmed Reda

---

## Work Plan


---

## Features

✅ Browse **Popular** Movies
✅ Browse **Trending** Movies
✅ **Search** Movies by Title
✅ View **Movie Ratings** and Details
✅ **Modern UI** using Jetpack Compose
✅ **TMDB API Integration** with Retrofit
✅ Clean **MVVM Architecture**
✅ Secure API key management
✅ Fast and responsive performance
⏳ **Offline Support** using Room Database (Planned)
⏳ **Movie Details Screen** (Planned)

---

## Roles & Responsibilities

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
| Local Storage   | Room Database                  |
| API             | TMDB REST API                  |
| Testing         | JUnit, Mockito                 |
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

2. **Add TMDB API Key**
   - Get your API key from [TMDB](https://www.themoviedb.org/settings/api)
   - Add it to `local.properties`:
     ```
     TMDB_API_KEY=your_api_key_here
     ```

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
│   └── ImageUtils.kt      # Image URL helpers
└── ui/                     # UI components
    └── theme/             # App theming
```

---

## API Integration

The app uses the TMDB API v3 with the following endpoints:
- `GET /movie/popular` - Popular movies
- `GET /trending/movie/day` - Trending movies
- `GET /search/movie` - Search movies
- `GET /movie/now_playing` - Now playing movies
- `GET /movie/top_rated` - Top rated movies

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
