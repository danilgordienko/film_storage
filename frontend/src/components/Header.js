import React, { useEffect, useRef, useState } from 'react';
import { useLocation, NavLink } from 'react-router-dom';
import SockJS from 'sockjs-client';
import { Client } from '@stomp/stompjs';
import '../styles/Header.css';

const Header = ({ userAvatarUrl }) => {
  const location = useLocation();
  const hideOnPaths = ['/login', '/register'];
  const [notificationCount, setNotificationCount] = useState(0);
  const [notifications, setNotifications] = useState([]);
  const [showDropdown, setShowDropdown] = useState(false);
  const [stompClient, setStompClient] = useState(null);

  const dropdownRef = useRef(null);
  const userId = localStorage.getItem('user_id');

  // Получение количества уведомлений при загрузке
  useEffect(() => {
    if (!userId) return;

    fetch(`http://localhost:8083/api/notifications/info?id=${userId}`)
        .then(res => res.json())
        .then(data => setNotificationCount(data.count))
        .catch(err => console.error('Failed to fetch notifications info', err));
  }, [userId]);

  // Подключение к WebSocket
  useEffect(() => {
    if (!userId) return;

    const client = new Client({
      webSocketFactory: () => new SockJS("http://localhost:8083/ws"),
      reconnectDelay: 5000,
      onConnect: () => {
        console.log("Connected to WebSocket");
        client.subscribe(`/topic/notifications/${userId}`, (message) => {
          console.log("📩 New message:", message);
          if (message.body) {
            const newNotif = JSON.parse(message.body);
            setNotifications(prev => [newNotif, ...prev]); // новые уведомления сверху
            setNotificationCount(prev => prev + 1);
          }
        });
      },
    });

    client.activate();
    setStompClient(client);
    return () => client.deactivate();
  }, [userId]);

  // Закрытие окна при клике вне
  useEffect(() => {
    const handleClickOutside = (event) => {
      if (dropdownRef.current && !dropdownRef.current.contains(event.target)) {
        setShowDropdown(false);
      }
    };

    if (showDropdown) {
      document.addEventListener('mousedown', handleClickOutside);
    } else {
      document.removeEventListener('mousedown', handleClickOutside);
    }

    return () => document.removeEventListener('mousedown', handleClickOutside);
  }, [showDropdown]);

  // Клик по колокольчику
  const handleNotificationsClick = async () => {
    if (!userId) return;

    try {
      // 1️⃣ Получаем все уведомления
      const response = await fetch(`http://localhost:8083/api/notifications?id=${userId}`);
      const data = await response.json();
      const sortedNotifications = (data.notifications || []).sort(
          (a, b) => new Date(b.createdAt) - new Date(a.createdAt)
      );
      setNotifications(sortedNotifications);
      setShowDropdown(prev => !prev);

      // 2️⃣ Если окно открывается — помечаем все как прочитанные
      if (!showDropdown) {
        await fetch(`http://localhost:8083/api/notifications/read?id=${userId}`, {
          method: 'POST'
        });
        setNotificationCount(0);
      }
    } catch (err) {
      console.error('Failed to fetch notifications', err);
    }
  };

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
          <NavLink to="/recommendations" className={({ isActive }) => isActive ? 'nav-link active' : 'nav-link'}>
            Рекомендации
          </NavLink>
        </nav>

        <nav className="nav-right">
          <div
              className="notification-wrapper"
              ref={dropdownRef}
              onClick={handleNotificationsClick}
              role="button"
              tabIndex={0}
              onKeyPress={(e) => { if (e.key === 'Enter') handleNotificationsClick(); }}
          >
            <span className="notification-icon">🔔</span>
            {notificationCount > 0 && (
                <span className="notification-badge">{notificationCount}</span>
            )}
            {showDropdown && (
                <div className="notification-dropdown">
                  {notifications.length > 0 ? (
                      notifications.map((notif, idx) => (
                          <div key={idx} className="notification-item">
                            <span className="notification-sender">{notif.sender}</span>
                            {notif.message && <span className="notification-message">{notif.message}</span>}
                            <span className="notification-date">
                      {new Date(notif.createdAt).toLocaleString()}
                    </span>
                          </div>
                      ))
                  ) : (
                      <div className="notification-item">Нет уведомлений</div>
                  )}
                </div>
            )}
          </div>

          <NavLink to="/profile/me" className={({ isActive }) => isActive ? 'nav-link active profile-link' : 'nav-link profile-link'}>
            Профиль
          </NavLink>
          <NavLink to="/profile/me" className="avatar-link">
            {userAvatarUrl ? (
                <img
                    src={`data:image/png;base64,${userAvatarUrl}`}
                    alt="Аватар пользователя"
                    className="avatar"
                    onError={(e) => {
                      e.target.onerror = null;
                      e.target.src = '/default-avatar.png';
                    }}
                />
            ) : (
                <div className="avatar"></div>
            )}
          </NavLink>
        </nav>
      </header>
  );
};

export default Header;
