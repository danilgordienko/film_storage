import React, { useEffect, useState } from 'react';
import { useParams } from 'react-router-dom';
import '../styles/MovieDetails.css';

const MovieDetails = () => {
  const { id } = useParams();
  const [movie, setMovie] = useState(null);
  const [rating, setRating] = useState(1);
  const [comment, setComment] = useState('');
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    fetchMovie();
  }, [id]);

  const fetchMovie = async () => {
    const token = localStorage.getItem('token');
    try {
      const response = await fetch(`http://localhost:8081/api/movies/${id}`, {
        headers: {
          'Authorization': `Bearer ${token}`,
        },
      });

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
    const token = localStorage.getItem('token');

    try {
      setLoading(true);
      const response = await fetch(`http://localhost:8081/api/ratings/add/movies/${id}`, {
        method: 'POST',
        headers: {
          'Authorization': `Bearer ${token}`,
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
        <h3>Оценки и отзывы</h3>
        {movie.ratings.length === 0 ? (
          <p>Отзывов пока нет</p>
        ) : (
          <div className="ratings-list">
            {movie.ratings.map((r, idx) => (
              <div key={idx} className="rating-card">
                <div className="rating-header">
                  <strong>{r.username}</strong> <span className="rating-score">{r.rating}/10</span>
                </div>
                <p className="rating-comment">{r.comment}</p>
                <small className="rating-date">{new Date(r.createdAt).toLocaleString()}</small>
              </div>
            ))}
          </div>
        )}
      </section>

      <hr />

      <section className="rating-form-section">
        <h3>Оставить отзыв</h3>
        <form onSubmit={handleSubmit} className="rating-form">
          <label>
            Рейтинг:
            <select value={rating} onChange={(e) => setRating(parseInt(e.target.value))}>
              {Array.from({ length: 10 }, (_, i) => i + 1).map(num => (
                <option key={num} value={num}>{num}</option>
              ))}
            </select>
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
      </section>
    </div>
  );
};

export default MovieDetails;
