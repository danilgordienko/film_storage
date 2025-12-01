import React, { useEffect, useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import '../styles/Movies.css';

let refreshPromise = null;

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
    if (!refreshPromise) {
      refreshPromise = (async () => {
        const refreshRes = await fetch('http://localhost:8081/api/auth/token/refresh', {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json',
            Authorization: `Bearer ${refreshToken}`,
          },
        });

        if (!refreshRes.ok) {
          localStorage.removeItem('access_token');
          localStorage.removeItem('refresh_token');
          window.location.href = '/login';
          throw new Error('Сессия истекла');
        }

        const { access_token, refresh_token } = await refreshRes.json();
        localStorage.setItem('access_token', access_token);
        localStorage.setItem('refresh_token', refresh_token);
        refreshPromise = null;
        return access_token;
      })();
    }

    const newAccessToken = await refreshPromise;
    res = await doFetch(newAccessToken);
  }

  return res;
}

const Movies = () => {
  const [movies, setMovies] = useState([]);
  const [query, setQuery] = useState('');
  const [searchResults, setSearchResults] = useState([]);
  const [isSearching, setIsSearching] = useState(false);
  const [currentPage, setCurrentPage] = useState(0);
  const [totalElements, setTotalElements] = useState(0);
  const [searchPage, setSearchPage] = useState(0);
  const [searchTotalElements, setSearchTotalElements] = useState(0);

  const [showModal, setShowModal] = useState(false);
  const [friends, setFriends] = useState([]);
  const [selectedFriend, setSelectedFriend] = useState(null);
  const [selectedMovieId, setSelectedMovieId] = useState(null);

  const pageSize = 20;
  const navigate = useNavigate();

  useEffect(() => {
    if (isSearching && query.trim()) {
      fetchSearchResults(query, searchPage);
    } else if (!isSearching) {
      fetchMovies(currentPage);
    }
  }, [currentPage, searchPage, isSearching, query]);

  useEffect(() => {
    if (query.trim() === '') {
      setIsSearching(false);
      setSearchResults([]);
      setSearchPage(0);
    }
  }, [query]);

  const fetchMovies = async (page) => {
    try {
      const response = await authFetch(`http://localhost:8081/api/movies?page=${page}`);
      if (!response.ok) throw new Error('Не удалось загрузить фильмы');
      const data = await response.json();
      setMovies(data.content);
      setTotalElements(data.totalElements);
    } catch (error) {
      alert(error.message);
      if (error.message.includes('Сессия истекла')) navigate('/login');
    }
  };

  const fetchSearchResults = async (searchQuery, page) => {
    try {
      const response = await authFetch(
          `http://localhost:8081/api/movies/search?query=${encodeURIComponent(searchQuery)}&page=${page}`
      );

      if (response.status === 204) {
        setSearchResults([]);
        setSearchTotalElements(0);
        return;
      }

      if (!response.ok) throw new Error('Ошибка при поиске фильмов');
      const data = await response.json();
      setSearchResults(data.content);
      setSearchTotalElements(data.totalElements);
    } catch (error) {
      alert(error.message);
      if (error.message.includes('Сессия истекла')) navigate('/login');
    }
  };

  const handleSearch = (e) => {
    e.preventDefault();
    const trimmed = query.trim();
    if (!trimmed) {
      setIsSearching(false);
      setSearchResults([]);
      setSearchPage(0);
      return;
    }

    setIsSearching(true);
    setSearchPage(0);
  };

  const addToFavorites = async (movieId) => {
    try {
      const response = await authFetch(
          `http://localhost:8081/api/favorites/add/movies/${movieId}`,
          {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
          }
      );
      if (!response.ok) throw new Error('Не удалось добавить фильм в избранное');
      alert('Фильм добавлен в избранное');
    } catch (error) {
      alert(error.message);
      if (error.message.includes('Сессия истекла')) navigate('/login');
    }
  };

  const openRecommendModal = async (movieId) => {
    try {
      const res = await authFetch('http://localhost:8081/api/friends');
      if (!res.ok) throw new Error('Не удалось получить список друзей');
      const data = await res.json();
      setFriends(data.friends);
      setSelectedMovieId(movieId);
      setShowModal(true);
    } catch (error) {
      alert(error.message);
    }
  };

  const sendRecommendation = async () => {
    if (!selectedFriend) {
      alert('Выберите друга для рекомендации');
      return;
    }
    try {
      const res = await authFetch(
          `http://localhost:8081/api/recommendations?receiverId=${selectedFriend}&movieId=${selectedMovieId}`,
          { method: 'POST' }
      );
      if (!res.ok) throw new Error('Не удалось отправить рекомендацию');
      alert('Рекомендация отправлена');
      setShowModal(false);
      setSelectedFriend(null);
      setSelectedMovieId(null);
    } catch (error) {
      alert(error.message);
    }
  };

  const getTotalPages = () =>
      isSearching
          ? Math.ceil(searchTotalElements / pageSize)
          : Math.ceil(totalElements / pageSize);

  const getActivePage = () => (isSearching ? searchPage : currentPage);

  const handlePageChange = (page) => {
    if (isSearching) setSearchPage(page);
    else setCurrentPage(page);
  };

  const renderPagination = () => {
    const totalPages = getTotalPages();
    const activePage = getActivePage();
    if (totalPages <= 1) return null;

    const visiblePages = 5;
    const half = Math.floor(visiblePages / 2);
    let start = Math.max(activePage - half, 0);
    let end = Math.min(start + visiblePages, totalPages);

    if (end - start < visiblePages) start = Math.max(end - visiblePages, 0);

    const pageNumbers = [];
    for (let i = start; i < end; i++) pageNumbers.push(i);

    return (
        <div className="pagination">
          {pageNumbers.map((page) => (
              <button
                  key={page}
                  className={page === activePage ? 'active' : ''}
                  onClick={() => handlePageChange(page)}
              >
                {page + 1}
              </button>
          ))}
        </div>
    );
  };

  const displayedMovies = isSearching ? searchResults : movies;

  return (
      <div className="movies-container">
        <h2>Список фильмов</h2>

        <form onSubmit={handleSearch} className="search-form">
          <input
              type="text"
              placeholder="Поиск по названию..."
              value={query}
              onChange={(e) => setQuery(e.target.value)}
          />
          <button type="submit">Найти</button>
        </form>

        {isSearching && query && (
            <p className="search-info">
              Результаты поиска по запросу: <strong>{query}</strong>
            </p>
        )}

        <div className="movie-list">
          {displayedMovies.length === 0 ? (
              <p>Фильмы не найдены</p>
          ) : (
              displayedMovies.map((movie) => (
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
                      <button onClick={() => addToFavorites(movie.id)}>Добавить в избранное</button>
                      <button onClick={() => openRecommendModal(movie.id)}>Рекомендовать</button>
                    </div>
                  </div>
              ))
          )}
        </div>

        {renderPagination()}

        {showModal && (
            <div className="modal-overlay">
              <div className="modal">
                <h3>Выберите друга для рекомендации</h3>

                {friends.length === 0 ? (
                    <p>У вас пока нет друзей</p>
                ) : (
                    <ul className="friends-list">
                      {friends.map((friend) => (
                          <li
                              key={friend.id}
                              className={`friend-item ${selectedFriend === friend.id ? 'selected' : ''}`}
                              onClick={() => setSelectedFriend(friend.id)}
                          >
                            <div className="friend-info">
                              {friend.avatar ? (
                                  <img
                                      src={`data:image/jpeg;base64,${friend.avatar}`}
                                      alt={friend.username}
                                      className="friend-avatar"
                                  />
                              ) : (
                                  <div className="friend-avatar"></div>
                              )}
                              <span>{friend.username}</span>
                            </div>
                          </li>
                      ))}
                    </ul>
                )}

                <div className="modal-buttons">
                  <button className="friend-action-btn recommend" onClick={sendRecommendation}>Отправить</button>
                  <button className="friend-action-btn decline" onClick={() => setShowModal(false)}>Отмена</button>
                </div>
              </div>
            </div>
        )}
      </div>
  );
};

export default Movies;