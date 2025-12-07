# TMDB API Key Setup

To use the TMDB API integration in this app, you need to obtain an API key from The Movie Database (TMDB).

## Steps to Get TMDB API Key

1. **Create a TMDB Account**
   - Go to [https://www.themoviedb.org/](https://www.themoviedb.org/)
   - Sign up for a free account

2. **Request API Key**
   - Once logged in, go to [https://www.themoviedb.org/settings/api](https://www.themoviedb.org/settings/api)
   - Fill out the API request form
   - You'll receive your API key via email (usually within a few hours)

3. **Add API Key to Project**
   - Open the `local.properties` file in the root of the project
   - Add the following line:
     ```
     TMDB_API_KEY=your_api_key_here
     ```
   - Replace `your_api_key_here` with your actual API key

4. **Sync Project**
   - Sync your Gradle files (File → Sync Project with Gradle Files)
   - The API key will be automatically loaded into `BuildConfig.TMDB_API_KEY`

## Important Notes

- **Never commit `local.properties`** - This file is already in `.gitignore`
- **Never commit your API key** to version control
- The API key is loaded at build time and stored in `BuildConfig`
- If the API key is not set, the app will display an error message

## Testing Without API Key

If you want to test the app structure without an API key, the app will still compile and run, but you'll see an error message indicating that the API key is not configured.

## API Rate Limits

TMDB API has rate limits:
- 40 requests per 10 seconds
- Keep this in mind when testing

For more information, visit: [https://developers.themoviedb.org/3/getting-started](https://developers.themoviedb.org/3/getting-started)

