import React, { useState, useEffect } from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import Login from './components/Login';
import Register from './components/Register';
import Movies from './components/Movies';
import MovieDetails from './components/MovieDetails';
import Header from './components/Header';
import UserProfile from './components/UserProfile';
import Friends from './components/Friends';
import Favorite from './components/Favorite';
import Recommendations from './components/Recommendations';

function App() {
  const [userAvatar, setUserAvatar] = useState(null);

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

  useEffect(() => {
    const fetchCurrentUser = async () => {
      try {
        const res = await authFetch('http://localhost:8081/api/users/me/info');
        if (res?.ok) {
          const data = await res.json();
          setUserAvatar(data.avatar || null);
        }
      } catch (err) {
        console.error('Ошибка загрузки текущего пользователя:', err);
      }
    };

    fetchCurrentUser();
  }, []);

  return (
    <Router>
      <Header userAvatarUrl={userAvatar} />
      <Routes>
        <Route path="/" element={<Navigate to="/login" />} />
        <Route path="/login" element={<Login />} />
        <Route path="/register" element={<Register />} />
        <Route path="/movies" element={<Movies />} />
        <Route path="/movies/:id" element={<MovieDetails />} />
        <Route path="/profile/:id" element={<UserProfile />} />
        <Route path="/friends" element={<Friends />} />
        <Route path="/favorites" element={<Favorite />} />
        <Route path="/recommendations" element={<Recommendations />} />
      </Routes>
    </Router>
  );
}

export default App;

