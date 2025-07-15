import React from 'react';
import { useLocation, NavLink } from 'react-router-dom';
import '../styles/Header.css';

const Header = ({ userAvatarUrl }) => {
  const location = useLocation();
  const hideOnPaths = ['/login', '/register'];

  if (hideOnPaths.includes(location.pathname)) return null;

  return (
    <header className="app-header">
      <nav className="nav-left">
        <NavLink to="/movies" className={({ isActive }) => isActive ? 'nav-link active' : 'nav-link'}>
          Главная
        </NavLink>
        <NavLink to="/friends" className={({ isActive }) => isActive ? 'nav-link active' : 'nav-link'}>
          Друзья
        </NavLink>
        <NavLink to="/favorites" className={({ isActive }) => isActive ? 'nav-link active' : 'nav-link'}>
          Избранное
        </NavLink>
      </nav>

      <nav className="nav-right">
        <NavLink to="/profile/me" className={({ isActive }) => isActive ? 'nav-link active profile-link' : 'nav-link profile-link'}>
          Профиль
        </NavLink>
        <NavLink to="/profile/me" className="avatar-link">
          <img src={userAvatarUrl} alt="Аватар пользователя" className="avatar" />
        </NavLink>
      </nav>
    </header>
  );
};

export default Header;
