import React, { useEffect, useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import '../styles/Movies.css';

async function authFetch(url, options = {}) {
  let accessToken = localStorage.getItem('access_token');
  const refreshToken = localStorage.getItem('refresh_token');

  const doFetch = async (token) => {
    return fetch(url, {
      ...options,
      headers: {
        ...(options.headers || {}),
        Authorization: `Bearer ${token}`,
      },
    });
  };

  let res = await doFetch(accessToken);

  if (res.status === 403 && refreshToken) {
    // пробуем обновить токен
    const refreshRes = await fetch('http://localhost:8081/api/auth/token/refresh', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        Authorization: `Bearer ${refreshToken}`,
      },
    });

    if (refreshRes.ok) {
      const { access_token, refresh_token } = await refreshRes.json();

      // сохраняем новые токены
      localStorage.setItem('access_token', access_token);
      localStorage.setItem('refresh_token', refresh_token);

      // повторяем запрос с новым access_token
      res = await doFetch(access_token);
    } else {
      // refresh тоже просрочен — уходим на login
      localStorage.removeItem('access_token');
      localStorage.removeItem('refresh_token');
      window.location.href = '/login';
      return; // чтобы дальше не выполнялось
    }
  }

  return res;
}

const Favorite = () => {
  const [favorites, setFavorites] = useState([]);
  const navigate = useNavigate();

  useEffect(() => {
    fetchFavorites();
  }, []);

  const fetchFavorites = async () => {
    try {
      const response = await authFetch('http://localhost:8081/api/favorites');
      if (!response.ok) throw new Error('Ошибка при получении избранного');

      const data = await response.json();
      setFavorites(data.favorites);
    } catch (error) {
      console.error(error);
      if (error.message.includes('Сессия истекла')) {
        navigate('/login');
      }
    }
  };

  const removeFavorite = async (movieId) => {
    try {
      const response = await authFetch(
        `http://localhost:8081/api/favorites/remove/movies/${movieId}`,
        { method: 'DELETE' }
      );

      if (!response.ok) throw new Error('Ошибка при удалении');

      setFavorites((prev) => prev.filter((fav) => fav.movie.id !== movieId));
    } catch (error) {
      console.error(error);
      if (error.message.includes('Сессия истекла')) {
        navigate('/login');
      }
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
                <div className="no-poster" style={{ display: 'none' }}>
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
                <button
                  className="remove-button"
                  onClick={() => removeFavorite(movie.id)}
                >
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
