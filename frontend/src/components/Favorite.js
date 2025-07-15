import React, { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import '../styles/Movies.css';

const Favorite = () => {
  const [favorites, setFavorites] = useState([]);

  useEffect(() => {
    fetchFavorites();
  }, []);

  const fetchFavorites = async () => {
    const token = localStorage.getItem('token');
    if (!token) return;

    try {
      const response = await fetch('http://localhost:8081/api/favorites', {
        headers: {
          Authorization: `Bearer ${token}`,
        },
      });

      if (!response.ok) throw new Error('Ошибка при получении избранного');

      const data = await response.json();
      setFavorites(data.favorites);
    } catch (error) {
      console.error(error);
    }
  };

  const removeFavorite = async (movieId) => {
    const token = localStorage.getItem('token');
    if (!token) return;

    try {
      const response = await fetch(`http://localhost:8081/api/favorites/remove/movies/${movieId}`, {
        method: 'DELETE',
        headers: {
          Authorization: `Bearer ${token}`,
        },
      });

      if (!response.ok) throw new Error('Ошибка при удалении');

      setFavorites((prev) => prev.filter((fav) => fav.movie.id !== movieId));
    } catch (error) {
      console.error(error);
    }
  };

  return (
    <div className="movies-container">
      <h2>Избранные фильмы</h2>

      {favorites.length === 0 ? (
        <p className="search-info">У вас пока нет избранных фильмов.</p>
      ) : (
        <div className="movie-list">
          {favorites.map(({ movie }) => (
            
            <div key={movie.id} className="movie-card">
            <div className="movie-poster-container">
              <img
                className="movie-poster"
                src={`http://localhost:8081/api/movies/${movie.id}/poster`}
                alt={`Постер к фильму ${movie.title}`}
                onError={(e) => {
                  e.target.onerror = null;
                  e.target.style.display = 'none';
                  const noPoster = e.target.parentElement.querySelector('.no-poster');
                  if (noPoster) noPoster.style.display = 'flex';
                }}
              />
              <div className="no-poster" style={{display: 'none'}}>
                Постер отсутствует
              </div>
            </div>
            <div className="movie-info">
              <Link to={`/movies/${movie.id}`} className="movie-link">
                <h3>{movie.title}</h3>
                <p>Дата выхода: {new Date(movie.release_date).toLocaleDateString()}</p>
                <p>Жанры: {movie.genres.join(', ')}</p>
                <p>Рейтинг: {movie.rating.toFixed(1)}</p>
              </Link>
              <button className="remove-button" onClick={() => removeFavorite(movie.id)}>
                  Удалить из избранного
                </button>
            </div>
          </div>
          ))}
        </div>
      )}
    </div>
  );
};

export default Favorite;
