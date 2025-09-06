import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import '../styles/Recommendations.css';
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


const Recommendations = () => {
  const [activeTab, setActiveTab] = useState('received');
  const [recommendations, setRecommendations] = useState([]);
  const [movies, setMovies] = useState({});
  const [users, setUsers] = useState({});
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    const fetchData = async () => {
      setIsLoading(true);
      try {
        // 1. Рекомендации
        const endpoint = activeTab === 'received' ? 'received' : 'sent';
        const res = await authFetch(`http://localhost:8081/api/recommendations/${endpoint}`);

        if (!res.ok) throw new Error(`Ошибка загрузки рекомендаций: ${res.status}`);
        const recommendationsData = await res.json();
        setRecommendations(recommendationsData);

        if (!recommendationsData.length) return;

        // 2. Уникальные ID
        const movieIds = [...new Set(recommendationsData.map(r => r.movieId))];
        const userIds = [...new Set([
          ...recommendationsData.map(r => r.senderId),
          ...recommendationsData.map(r => r.receiverId),
        ])];

        // 3. Фильмы
        const moviesRes = await Promise.all(movieIds.map(id =>
          authFetch(`http://localhost:8081/api/movies/${id}`).then(async res => {
            if (!res.ok) return null;
            return res.json();
          })
        ));
        const moviesMap = {};
        moviesRes.forEach(movie => {
          if (movie && movie.id) moviesMap[movie.id] = movie;
        });
        setMovies(moviesMap);

        // 4. Пользователи
        const usersRes = await Promise.all(userIds.map(id =>
          authFetch(`http://localhost:8081/api/users/${id}/info`).then(async res => {
            if (!res.ok) return null;
            return res.json();
          })
        ));
        const usersMap = {};
        usersRes.forEach(user => {
          if (user && user.id) usersMap[user.id] = user;
        });
        setUsers(usersMap);

      } catch (error) {
        console.error('Ошибка при загрузке:', error);
      } finally {
        setIsLoading(false);
      }
    };

    fetchData();
  }, [activeTab]);

  const cancelRecommendation = async (receiverId, movieId) => {
    try {
      await authFetch(`http://localhost:8081/api/recommendations?receiverId=${receiverId}&movieId=${movieId}`, {
        method: 'DELETE',
      });

      setRecommendations(prev =>
        prev.filter(rec => !(rec.receiverId === receiverId && rec.movieId === movieId))
      );
    } catch (error) {
      console.error('Ошибка отмены рекомендации:', error);
    }
  };

  return (
    <div className="recommendations-container">
      <h2>Мои рекомендации</h2>

      <div className="recommendations-tabs">
        <button
          className={activeTab === 'received' ? 'active' : ''}
          onClick={() => setActiveTab('received')}
        >
          Полученные
        </button>
        <button
          className={activeTab === 'sent' ? 'active' : ''}
          onClick={() => setActiveTab('sent')}
        >
          Отправленные
        </button>
      </div>

      {isLoading ? (
        <p>Загрузка рекомендаций...</p>
      ) : recommendations.length === 0 ? (
        <p>{activeTab === 'received'
          ? 'У вас нет полученных рекомендаций'
          : 'Вы не отправляли рекомендаций'}</p>
      ) : (
        <div className="movie-list">
          {recommendations.map((rec) => {
            const movie = movies[rec.movieId];
            const sender = users[rec.senderId];
            const receiver = users[rec.receiverId];

            return (
              <div key={rec.id} className="recommendation-card">
                <div className="recommendation-header">
                  {activeTab === 'received' ? (
                    <p>От: <strong>{sender?.username || 'Неизвестный'}</strong></p>
                  ) : (
                    <p>Для: <strong>{receiver?.username || 'Неизвестный'}</strong></p>
                  )}
                </div>

                <div className="movie-poster-container">
                  {movie ? (
                    <img
                      className="movie-poster"
                      src={`http://localhost:8081/api/movies/${movie.id}/poster`}
                      alt={`Постер ${movie.title}`}
                      onError={(e) => {
                        e.target.onerror = null;
                        e.target.style.display = 'none';
                        const noPoster = e.target.parentElement.querySelector('.no-poster');
                        if (noPoster) noPoster.style.display = 'flex';
                      }}
                    />
                  ) : null}
                  <div className="no-poster" style={{ display: movie ? 'none' : 'flex' }}>
                    Постер отсутствует
                  </div>
                </div>

                <div className="movie-info">
                  {movie ? (
                    <Link to={`/movies/${movie.id}`} className="movie-link">
                      <h3>{movie.title}</h3>
                      {movie.release_date && (
                        <p>Дата выхода: {new Date(movie.release_date).toLocaleDateString()}</p>
                      )}
                      {Array.isArray(movie.genres) && movie.genres.length > 0 && (
                        <p>Жанры: {movie.genres.join(', ')}</p>
                      )}
                      {typeof movie.rating === 'number' && (
                        <p>Рейтинг: {movie.rating.toFixed(1)}</p>
                      )}
                    </Link>
                  ) : (
                    <p>Информация о фильме не найдена</p>
                  )}

                  {activeTab === 'sent' && (
                    <button
                      onClick={() => cancelRecommendation(rec.receiverId, rec.movieId)}
                      style={{ backgroundColor: '#dc3545' }}
                    >
                      Отменить рекомендацию
                    </button>
                  )}
                </div>
              </div>
            );
          })}
        </div>
      )}
    </div>
  );
};

export default Recommendations;
