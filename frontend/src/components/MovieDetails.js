import React, { useEffect, useState } from 'react';
import { useParams } from 'react-router-dom';
import '../styles/MovieDetails.css';

const MovieDetails = () => {
  const { id } = useParams();
  const [movie, setMovie] = useState(null);
  const [rating, setRating] = useState(1);
  const [hoverRating, setHoverRating] = useState(0);
  const [comment, setComment] = useState('');
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    fetchMovie();
  }, [id]);

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

  const fetchMovie = async () => {
    try {
      const response = await authFetch(`http://localhost:8081/api/movies/${id}`);
      if (!response.ok) {
        throw new Error('Не удалось загрузить информацию о фильме');
      }
      const data = await response.json();
      setMovie(data);
    } catch (error) {
      alert(error.message);
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    try {
      setLoading(true);
      const response = await authFetch(`http://localhost:8081/api/ratings/add/movies/${id}`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          rating,
          comment,
        }),
      });

      if (!response.ok) {
        const errorText = await response.text();
        throw new Error(errorText || 'Ошибка при отправке рейтинга');
      }

      setComment('');
      setRating(1);
      await fetchMovie(); // обновить данные после добавления отзыва
    } catch (error) {
      alert(error.message);
    } finally {
      setLoading(false);
    }
  };

  if (!movie) {
    return <p>Загрузка...</p>;
  }

  return (
    <div className="movie-details">
      <div className="movie-header">
        <img className="movie-poster" src={`http://localhost:8081/api/movies/${id}/poster`} alt={movie.title} />
        <div className="movie-info">
          <h2>{movie.title}</h2>
          <p><strong>Описание:</strong> {movie.description}</p>
          <p><strong>Дата выхода:</strong> {new Date(movie.release_date).toLocaleDateString()}</p>
          <p><strong>Жанры:</strong> {movie.genres.join(', ')}</p>
        </div>
      </div>

      <hr />

      <section className="movie-ratings">
        <h3>Оставить отзыв</h3>
        <form onSubmit={handleSubmit} className="rating-form">
          <label>
            Рейтинг:
            <div className="rating-stars">
              {Array.from({ length: 10 }, (_, i) => {
                const starNum = i + 1;
                const isFilled = starNum <= (hoverRating || rating);

                return (
                    <span
                        key={starNum}
                        className={`star ${isFilled ? 'filled' : ''}`}
                        onClick={() => setRating(starNum)}
                        onMouseEnter={() => setHoverRating(starNum)}
                        onMouseLeave={() => setHoverRating(0)}
                        title={`${starNum} из 10`}
                    >
        ★
      </span>
                );
              })}
            </div>


          </label>

          <label>
            Комментарий:
            <textarea
                value={comment}
                onChange={(e) => setComment(e.target.value)}
                rows="3"
                required
            />
          </label>

          <button type="submit" disabled={loading}>
            {loading ? 'Отправка...' : 'Оставить отзыв'}
          </button>
        </form>

        <h3>Отзывы</h3>
        {movie.ratings.length === 0 ? (
            <p>Отзывов пока нет</p>
        ) : (
            <div className="ratings-list">
              {movie.ratings.map((r, idx) => (
                  <div key={idx} className="rating-card">
                    <div className="rating-header">
                      <strong>{r.username}</strong>
                      <span className="rating-score">{'★'.repeat(r.rating)}{'☆'.repeat(10 - r.rating)}</span>
                    </div>
                    <p className="rating-comment">{r.comment}</p>
                    <small className="rating-date">{new Date(r.createdAt).toLocaleString()}</small>
                  </div>
              ))}
            </div>
        )}
      </section>

    </div>
  );
};

export default MovieDetails;
