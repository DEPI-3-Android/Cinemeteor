import "./styles.css";

const downloadLink =
  "https://play.google.com/store/apps/details?id=com.acms.cinemeteor";

const posters = [
  "Top Rated",
  "Trending",
  "Now Playing",
  "Search",
  "Favorites",
  "Cloud Sync"
];

export function DownloadApp() {
  return (
    <div className="page">
      <header>
        <div className="logo">
          <img className="logo-mark" src="/cinemeteor.png" alt="Cinemeteor logo" />
          <span>Cinemeteor</span>
        </div>
        <div className="tagline">Movies, reimagined.</div>
      </header>

      <main>
        <section className="hero">
          <h1>Discover. Save. Rewatch.</h1>
          <p className="lead">
            Cinemeteor brings you trending, popular, and top-rated movies with
            beautiful visuals, smart search, and a smooth experience that adapts
            to light and dark themes.
          </p>
          <p className="lead subtle">
            Sync your favorites, explore details powered by TMDB, and jump back in
            where you left off.
          </p>
          <a href={downloadLink}>
            <button className="cta">Download App</button>
          </a>
        </section>

        <section className="media-card">
          <div className="mockup">
            <div className="glow" />
            <div className="mockup-content">
              <div className="pill">Material 3 · Jetpack Compose</div>
              <div className="poster-grid">
                {posters.map((title) => (
                  <div key={title} className="poster">
                    {title}
                  </div>
                ))}
              </div>
            </div>
          </div>
        </section>
      </main>

      <footer className="footer">
        Made with TMDB, Jetpack Compose, and a bold red accent.
      </footer>
    </div>
  );
}

