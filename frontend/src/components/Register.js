import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import '../styles/Auth.css';


const Register = () => {
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const navigate = useNavigate();

  const handleRegister = async (e) => {
    e.preventDefault();

    try {
      const response = await fetch('http://localhost:8081/api/auth/register', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({ username, password }),
      });

      if (!response.ok) {
        const error = await response.text();
        alert('Ошибка: ' + error);
        return;
      }

       const { access_token, refresh_token } = await response.json();

       localStorage.setItem('access_token', access_token);
       localStorage.setItem('refresh_token', refresh_token);

       const res = await fetch('http://localhost:8081/api/users/me/info', {
        headers: { Authorization: `Bearer ${access_token}` },
      });
      if (res.ok) {
        const userInfo = await res.json();
        localStorage.setItem('user_id', userInfo.id);
      }
 
       navigate('/movies');
    } catch (err) {
      alert('Ошибка соединения с сервером');
    }
  };

  return (
    <div className="auth-container">
      <h2>Регистрация</h2>
      <form onSubmit={handleRegister}>
        <input
          type="text"
          placeholder="Имя пользователя"
          value={username}
          onChange={(e) => setUsername(e.target.value)}
          required
        />
        <input
          type="password"
          placeholder="Пароль"
          value={password}
          onChange={(e) => setPassword(e.target.value)}
          required
        />
        <button type="submit">Зарегистрироваться</button>
      </form>
      <p>Уже есть аккаунт? <Link to="/login">Войдите</Link></p>
    </div>
  );
};

export default Register;
