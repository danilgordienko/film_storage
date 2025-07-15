import React, { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import '../styles/Movies.css';

const Movies = () => {
  const [movies, setMovies] = useState([]);
  const [query, setQuery] = useState('');
  const [searchResults, setSearchResults] = useState([]);
  const [isSearching, setIsSearching] = useState(false);

  const [currentPage, setCurrentPage] = useState(0);
  const [totalElements, setTotalElements] = useState(0);

  const [searchPage, setSearchPage] = useState(0);
  const [searchTotalElements, setSearchTotalElements] = useState(0);

  const pageSize = 20;

  useEffect(() => {
    if (isSearching && query.trim()) {
      fetchSearchResults(query, searchPage);
    } else {
      fetchMovies(currentPage);
    }
  }, [currentPage, searchPage]);

  useEffect(() => {
    if (query.trim() === '') {
      setIsSearching(false);
      setSearchResults([]);
      setSearchPage(0);
    }
  }, [query]);

  const fetchMovies = async (page) => {
    const token = localStorage.getItem('token');
    try {
      const response = await fetch(`http://localhost:8081/api/movies?page=${page}`, {
        headers: { 'Authorization': `Bearer ${token}` },
      });
      if (!response.ok) throw new Error('Не удалось загрузить фильмы');
      const data = await response.json();
      setMovies(data.content);
      setTotalElements(data.totalElements);
    } catch (error) {
      alert(error.message);
    }
  };

  const fetchSearchResults = async (searchQuery, page) => {
    const token = localStorage.getItem('token');
    try {
      const response = await fetch(`http://localhost:8081/api/movies/search?query=${encodeURIComponent(searchQuery)}&page=${page}`, {
        headers: { 'Authorization': `Bearer ${token}` },
      });

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
    }
  };

  const handleSearch = (e) => {
    e.preventDefault();
    const trimmed = query.trim();
    if (!trimmed) {
      setIsSearching(false);
      setSearchResults([]);
      return;
    }

    setIsSearching(true);
    setSearchPage(0); // сброс к первой странице поиска
    fetchSearchResults(trimmed, 0);
  };

  const addToFavorites = async (movieId) => {
    const token = localStorage.getItem('token');
    try {
      const response = await fetch(`http://localhost:8081/api/favorites/add/movies/${movieId}`, {
        method: 'POST',
        headers: {
          'Authorization': `Bearer ${token}`,
          'Content-Type': 'application/json',
        },
      });

      if (!response.ok) throw new Error('Не удалось добавить фильм в избранное');
      alert('Фильм добавлен в избранное');
    } catch (error) {
      alert(error.message);
    }
  };

  const getTotalPages = () =>
    isSearching
      ? Math.ceil(searchTotalElements / pageSize)
      : Math.ceil(totalElements / pageSize);

  const getActivePage = () =>
    isSearching ? searchPage : currentPage;

  const handlePageChange = (page) => {
    if (isSearching) {
      setSearchPage(page);
    } else {
      setCurrentPage(page);
    }
  };

  const renderPagination = () => {
    const totalPages = getTotalPages();
    const activePage = getActivePage();
    if (totalPages <= 1) return null;

    const visiblePages = 5;
    const half = Math.floor(visiblePages / 2);
    let start = Math.max(activePage - half, 0);
    let end = Math.min(start + visiblePages, totalPages);

    if (end - start < visiblePages) {
      start = Math.max(end - visiblePages, 0);
    }

    const pageNumbers = [];
    for (let i = start; i < end; i++) {
      pageNumbers.push(i);
    }

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
        <p className="search-info">Результаты поиска по запросу: <strong>{query}</strong></p>
      )}

      <div className="movie-list">
        {displayedMovies.length === 0 ? (
          <p>Фильмы не найдены</p>
        ) : (
          displayedMovies.map((movie, index) => (
            <div key={index} className="movie-card">
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
    <button onClick={() => addToFavorites(movie.id)}>Добавить в избранное</button>
  </div>
</div>
          ))
        )}
      </div>

      {renderPagination()}
    </div>
  );
};

export default Movies;
