import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import '../styles/Friends.css';

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

function Friends() {
  const [activeTab, setActiveTab] = useState('search');
  const [searchQuery, setSearchQuery] = useState('');
  const [searchResults, setSearchResults] = useState([]);
  const [incomingRequests, setIncomingRequests] = useState([]);
  const [outgoingRequests, setOutgoingRequests] = useState([]);
  const navigate = useNavigate();

  // Поиск пользователей
  const handleSearch = async () => {
    if (!searchQuery.trim()) return;
    try {
      const res = await authFetch(
        `http://localhost:8081/api/users/search?query=${encodeURIComponent(searchQuery)}`
      );
      if (res.ok) {
        const data = await res.json();
        setSearchResults(data);
      } else {
        setSearchResults([]);
      }
    } catch (err) {
      console.error('Ошибка при поиске:', err);
      if (err.message.includes('Сессия истекла')) navigate('/login');
    }
  };

  // Загрузить входящие заявки
  const fetchIncomingRequests = async () => {
    try {
      const res = await authFetch('http://localhost:8081/api/friends/requests/incoming');
      if (res.ok) {
        const data = await res.json();
        setIncomingRequests(data);
      }
    } catch (err) {
      console.error('Ошибка загрузки входящих:', err);
      if (err.message.includes('Сессия истекла')) navigate('/login');
    }
  };

  // Загрузить исходящие заявки
  const fetchOutgoingRequests = async () => {
    try {
      const res = await authFetch('http://localhost:8081/api/friends/requests/outgoing');
      if (res.ok) {
        const data = await res.json();
        setOutgoingRequests(data);
      }
    } catch (err) {
      console.error('Ошибка загрузки исходящих:', err);
      if (err.message.includes('Сессия истекла')) navigate('/login');
    }
  };

  useEffect(() => {
    if (activeTab === 'incoming') fetchIncomingRequests();
    if (activeTab === 'outgoing') fetchOutgoingRequests();
  }, [activeTab]);

  const handleSendRequest = async (id) => {
    try {
      const response = await authFetch(`http://localhost:8081/api/friends/request/${id}`, {
        method: 'POST',
      });
      if (response.ok) {
        alert('Заявка отправлена');
      } else {
        alert('Ошибка при отправке заявки');
      }
    } catch (err) {
      console.error('Ошибка при отправке заявки:', err);
      if (err.message.includes('Сессия истекла')) navigate('/login');
      alert('Ошибка при отправке заявки');
    }
  };

  const handleAccept = async (id) => {
    try {
      const response = await authFetch(`http://localhost:8081/api/friends/accept/${id}`, {
        method: 'POST',
      });
      if (response.ok) {
        fetchIncomingRequests();
        alert('Заявка принята');
      } else {
        alert('Ошибка при принятии заявки');
      }
    } catch (err) {
      console.error('Ошибка при принятии заявки:', err);
      if (err.message.includes('Сессия истекла')) navigate('/login');
      alert('Ошибка при принятии заявки');
    }
  };

  const handleDecline = async (id) => {
    try {
      const response = await authFetch(`http://localhost:8081/api/friends/decline/${id}`, {
        method: 'POST',
      });
      if (response.ok) {
        fetchIncomingRequests();
        alert('Заявка отклонена');
      } else {
        alert('Ошибка при отклонении заявки');
      }
    } catch (err) {
      console.error('Ошибка при отклонении заявки:', err);
      if (err.message.includes('Сессия истекла')) navigate('/login');
      alert('Ошибка при отклонении заявки');
    }
  };

  const renderUserList = (users, actionButtons) => (
    <ul className="friends-list">
      {users.map((user) => (
        <li key={user.id} className="friend-item">
          <div className="friend-info" onClick={() => navigate(`/profile/${user.id}`)}>
            <div className="friend-avatar"></div>
            <span>{user.username}</span>
          </div>
          <div className="friend-actions">{actionButtons(user)}</div>
        </li>
      ))}
    </ul>
  );

  return (
    <div className="friends-page">
      <h2>Друзья</h2>
      <div className="tabs">
        <button
          className={activeTab === 'search' ? 'active' : ''}
          onClick={() => setActiveTab('search')}
        >
          Найти друзей
        </button>
        <button
          className={activeTab === 'incoming' ? 'active' : ''}
          onClick={() => setActiveTab('incoming')}
        >
          Входящие заявки
        </button>
        <button
          className={activeTab === 'outgoing' ? 'active' : ''}
          onClick={() => setActiveTab('outgoing')}
        >
          Исходящие заявки
        </button>
      </div>

      <div className="tab-content">
        {activeTab === 'search' && (
          <>
            <div className="search-container">
              <input
                type="text"
                placeholder="Введите имя пользователя"
                value={searchQuery}
                onChange={(e) => setSearchQuery(e.target.value)}
              />
              <button onClick={handleSearch}>Найти</button>
            </div>
            {renderUserList(searchResults, (user) => (
              <button
                className="friend-action-btn"
                onClick={(e) => {
                  e.stopPropagation();
                  handleSendRequest(user.id);
                }}
              >
                Отправить запрос
              </button>
            ))}
          </>
        )}

        {activeTab === 'incoming' && (
          <>
            {incomingRequests.length === 0 ? (
              <p>Нет входящих заявок</p>
            ) : (
              renderUserList(incomingRequests, (user) => (
                <>
                  <button
                    className="friend-action-btn accept"
                    onClick={(e) => {
                      e.stopPropagation();
                      handleAccept(user.id);
                    }}
                  >
                    Принять
                  </button>
                  <button
                    className="friend-action-btn decline"
                    onClick={(e) => {
                      e.stopPropagation();
                      handleDecline(user.id);
                    }}
                  >
                    Отклонить
                  </button>
                </>
              ))
            )}
          </>
        )}

        {activeTab === 'outgoing' && (
          <>
            {outgoingRequests.length === 0 ? (
              <p>Нет исходящих заявок</p>
            ) : (
              renderUserList(outgoingRequests, () => null)
            )}
          </>
        )}
      </div>
    </div>
  );
}

export default Friends;
