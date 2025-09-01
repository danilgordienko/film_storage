import '../styles/UserProfile.css';
import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';

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
    const refreshRes = await fetch('http://localhost:8081/api/auth/token/refresh', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        Authorization: `Bearer ${refreshToken}`,
      },
    });

    if (refreshRes.ok) {
      const { access_token, refresh_token } = await refreshRes.json();
      localStorage.setItem('access_token', access_token);
      localStorage.setItem('refresh_token', refresh_token);
      res = await doFetch(access_token);
    } else {
      localStorage.removeItem('access_token');
      localStorage.removeItem('refresh_token');
      window.location.href = '/login';
      return;
    }
  }

  return res;
}

function UserProfile() {
  const { id } = useParams();
  const navigate = useNavigate();
  const [currentUserId, setCurrentUserId] = useState(null);

  const isCurrentUserProfile = !id || id === 'me';
  const [activeTab, setActiveTab] = useState('info');
  const [userInfo, setUserInfo] = useState(null);
  const [userRatings, setUserRatings] = useState(null);
  const [userFriends, setUserFriends] = useState(null);
  const [ratingVisibility, setRatingVisibility] = useState('ALL');

  const [showUpdateProfile, setShowUpdateProfile] = useState(false);
  const [showChangePassword, setShowChangePassword] = useState(false);

  const [updateProfileData, setUpdateProfileData] = useState({
    username: '',
    email: '',
    avatar: null,
  });

  const [passwordData, setPasswordData] = useState({
    oldPassword: '',
    newPassword: '',
    newPasswordConfirm: '',
  });

  // Получение текущего пользователя
  useEffect(() => {
    const fetchCurrentUser = async () => {
      try {
        const res = await authFetch('http://localhost:8081/api/users/me/info');
        if (res.ok) {
          const data = await res.json();
          setCurrentUserId(data.id);
        }
      } catch (err) {
        console.error('Ошибка при загрузке текущего пользователя:', err);
      }
    };
    fetchCurrentUser();
  }, []);

  // Загрузка информации о пользователе
  useEffect(() => {
    const fetchInfo = async () => {
      try {
        const baseUrl = 'http://localhost:8081/api/users';
        const url = isCurrentUserProfile ? `${baseUrl}/me/info` : `${baseUrl}/${id}/info`;
        const res = await authFetch(url);
        if (res.ok) {
          const data = await res.json();
          setUserInfo(data);
          setUpdateProfileData({ username: data.username, email: data.email, avatar: null });
        }
      } catch (err) {
        console.error('Ошибка при загрузке информации:', err);
      }
    };
    fetchInfo();
  }, [id]);

  const [ratingsError, setRatingsError] = useState(null);

  // Сброс данных отзывов при смене пользователя
  useEffect(() => {
    setUserRatings(null);
    setRatingsError(null);
  }, [id]);
  
  // Загрузка отзывов
  useEffect(() => {
    if (activeTab === 'ratings') {
      const fetchRatings = async () => {
        try {
          const baseUrl = 'http://localhost:8081/api/ratings';
          const url = isCurrentUserProfile ? `${baseUrl}/users/me` : `${baseUrl}/users/${id}`;
          const res = await authFetch(url);
  
          if (res.ok) {
            const data = await res.json();
            setUserRatings(data?.ratings ?? []);
            setRatingsError(null);
          } else if (res.status === 403) {
            // Доступ запрещён
            setUserRatings([]);
            setRatingsError('Отзывы этого пользователя доступны только друзьям.');
          } else {
            const text = await res.text();
            console.error('Ошибка при загрузке отзывов:', text);
            setUserRatings([]);
            setRatingsError('Не удалось загрузить отзывы.');
          }
        } catch (err) {
          console.error('Ошибка при загрузке отзывов:', err);
          setUserRatings([]);
          setRatingsError('Ошибка при загрузке отзывов.');
        }
      };
      fetchRatings();
    }
  }, [activeTab, id]);
  


// Сброс данных друзей при смене пользователя
useEffect(() => {
  setUserFriends(null);
}, [id]);

// Загрузка друзей
useEffect(() => {
  if (activeTab === 'friends') {
    const fetchFriends = async () => {
      try {
        const baseUrl = 'http://localhost:8081/api/friends';
        const url = isCurrentUserProfile ? `${baseUrl}` : `${baseUrl}/users/${id}`;
        const res = await authFetch(url);
        if (res.ok) {
          const data = await res.json();
          setUserFriends(data?.friends ?? []);
        }
      } catch (err) {
        console.error('Ошибка при загрузке друзей:', err);
      }
    };
    fetchFriends();
  }
}, [activeTab, id]);


    // Загрузка настроек при открытии вкладки settings
    useEffect(() => {
      if (activeTab === 'settings' && isCurrentUserProfile) {
        const fetchSettings = async () => {
          try {
            const res = await authFetch('http://localhost:8081/api/users/me/settings');
            if (res.ok) {
              const data = await res.json();
              setRatingVisibility(data.rating_visibility || 'ALL');
            }
          } catch (err) {
            console.error('Ошибка при загрузке настроек:', err);
          }
        };
        fetchSettings();
      }
    }, [activeTab]);

  // Отправка изменения видимости отзывов
  const handleVisibilityChange = async (e) => {
    const newVisibility = e.target.value;
    const visibilityStr = typeof newVisibility === 'string' ? newVisibility : newVisibility.value;
    try {
      const res = await authFetch(`http://localhost:8081/api/users/me/ratings/visibility?visibility=${visibilityStr}`, {
        method: 'POST',
      });
      const text = await res.text();
      if (res.ok) {
        setRatingVisibility(newVisibility);
        alert('Настройки видимости отзывов обновлены');
      } else {
        console.error('Ошибка обновления видимости:', text);
        alert('Не удалось обновить настройки видимости');
      }
    } catch (err) {
      console.error('Ошибка при обновлении видимости отзывов:', err);
    }
  };

  const handleFriendClick = (friend) => {
    const isMe = currentUserId && friend.id === currentUserId;
    navigate(`/profile/${isMe ? 'me' : friend.id}`);
  };

  const handleRemoveFriend = async (friendId) => {
    if (!window.confirm('Вы уверены, что хотите удалить этого пользователя из друзей?')) return;
    try {
      const res = await authFetch(`http://localhost:8081/api/friends/remove/${friendId}`, {
        method: 'DELETE',
      });
      if (res.ok) {
        setUserFriends((prev) => prev.filter((f) => f.id !== friendId));
      } else {
        alert('Не удалось удалить друга.');
      }
    } catch (err) {
      console.error('Ошибка при удалении друга:', err);
    }
  };

  const handleUpdateProfile = async (e) => {
    e.preventDefault();
    const formData = new FormData();
    formData.append('username', updateProfileData.username);
    formData.append('email', updateProfileData.email);
    if (updateProfileData.avatar) formData.append('avatar', updateProfileData.avatar);

    try {
      const res = await fetch('http://localhost:8081/api/users/me/profile/update', {
        method: 'POST',
        headers: {
          Authorization: `Bearer ${localStorage.getItem('access_token')}`,
        },
        body: formData,
      });

      if (!res.ok) throw new Error('Ошибка при обновлении профиля');

      const infoRes = await authFetch('http://localhost:8081/api/users/me/info');
      if (!infoRes.ok) throw new Error('Ошибка при получении обновленного профиля');

      const data = await infoRes.json();
      setUserInfo(data);
      setUpdateProfileData({ username: data.username, email: data.email, avatar: null });
      alert('Профиль обновлён');
      setShowUpdateProfile(false);
    } catch (err) {
      alert(err.message);
    }
  };

  const handleChangePassword = async (e) => {
    e.preventDefault();
    const res = await authFetch('http://localhost:8081/api/users/me/password', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(passwordData),
    });

    if (res.ok) {
      alert('Пароль успешно изменён');
      setShowChangePassword(false);
      setPasswordData({ oldPassword: '', newPassword: '', newPasswordConfirm: '' });
    } else {
      alert('Ошибка при смене пароля');
    }
  };

  const handleLogout = async () => {
    const token = localStorage.getItem('access_token');
    await authFetch('http://localhost:8081/api/auth/logout', {
      method: 'POST',
      headers: { Authorization: `Bearer ${token}` },
    });
    localStorage.removeItem('access_token');
    localStorage.removeItem('refresh_token');
    navigate('/login');
  };

  const renderTabContent = () => {
    switch (activeTab) {
      case 'info':
        return (
          <div className="tab-content">
            <div className="profile-header">
              {userInfo?.avatar ? (
                <img
                  className="avatar large"
                  src={`data:image/png;base64,${userInfo.avatar}`}
                  alt={`${userInfo.username} avatar`}
                  onError={(e) => {
                    e.target.onerror = null;
                    e.target.src = '/default-avatar.png';
                  }}
                />
              ) : (
                <div className="avatar large"></div>
              )}
              <h3>{userInfo?.username ?? 'Загрузка...'}</h3>

              {isCurrentUserProfile && (
                <div className="profile-actions">
                  <button onClick={() => setShowUpdateProfile(true)}>Обновить профиль</button>
                  <button onClick={() => setShowChangePassword(true)}>Сменить пароль</button>
                  <button className="logout-btn" onClick={handleLogout}>Выйти</button>
                </div>
              )}

              {showUpdateProfile && (
                <div className="modal-overlay" onClick={() => setShowUpdateProfile(false)}>
                  <div className="modal-content" onClick={(e) => e.stopPropagation()}>
                    <button className="modal-close" onClick={() => setShowUpdateProfile(false)}>×</button>
                    <form onSubmit={handleUpdateProfile} className="profile-form">
                      <input
                        type="text"
                        placeholder="Имя пользователя"
                        value={updateProfileData.username}
                        onChange={(e) => setUpdateProfileData({ ...updateProfileData, username: e.target.value })}
                      />
                      <input
                        type="email"
                        placeholder="Email"
                        value={updateProfileData.email}
                        onChange={(e) => setUpdateProfileData({ ...updateProfileData, email: e.target.value })}
                      />
                      <input
                        type="file"
                        onChange={(e) => setUpdateProfileData({ ...updateProfileData, avatar: e.target.files[0] })}
                      />
                      <button type="submit">Сохранить</button>
                    </form>
                  </div>
                </div>
              )}

              {showChangePassword && (
                <div className="modal-overlay" onClick={() => setShowChangePassword(false)}>
                  <div className="modal-content" onClick={(e) => e.stopPropagation()}>
                    <button className="modal-close" onClick={() => setShowChangePassword(false)}>×</button>
                    <form onSubmit={handleChangePassword} className="profile-form">
                      <input
                        type="password"
                        placeholder="Старый пароль"
                        value={passwordData.oldPassword}
                        onChange={(e) => setPasswordData({ ...passwordData, oldPassword: e.target.value })}
                      />
                      <input
                        type="password"
                        placeholder="Новый пароль"
                        value={passwordData.newPassword}
                        onChange={(e) => setPasswordData({ ...passwordData, newPassword: e.target.value })}
                      />
                      <input
                        type="password"
                        placeholder="Подтверждение нового пароля"
                        value={passwordData.newPasswordConfirm}
                        onChange={(e) => setPasswordData({ ...passwordData, newPasswordConfirm: e.target.value })}
                      />
                      <button type="submit">Сменить пароль</button>
                    </form>
                  </div>
                </div>
              )}
            </div>
          </div>
        );

        case 'ratings':
          return (
            <div className="tab-content">
              <h3>Отзывы</h3>
              {userRatings === null ? (
                <p>Загрузка...</p>
              ) : ratingsError ? (
                <p>{ratingsError}</p>
              ) : userRatings.length === 0 ? (
                <p>Нет отзывов.</p>
              ) : (
                <div className="card-grid">
                  {userRatings.map((r, idx) => (
                    <div key={idx} className="card">
                      <h4>{r.movie.title}</h4>
                      <p><strong>{r.rating}/10</strong></p>
                      <p>{r.comment}</p>
                      <button onClick={() => navigate(`/movies/${r.movie.id}`)}>К фильму</button>
                    </div>
                  ))}
                </div>
              )}
            </div>
          );
        

      case 'friends':
        return (
          <div className="tab-content friends-page">
            <h3>Друзья</h3>
            {userFriends === null ? (
              <p>Загрузка...</p>
            ) : userFriends.length === 0 ? (
              <p>Нет друзей.</p>
            ) : (
              <ul className="friends-list">
                {userFriends.map((f) => (
                  <li
                    key={f.id}
                    className="friend-item"
                    onClick={() => handleFriendClick(f)}
                    role="button"
                    tabIndex={0}
                    onKeyDown={e => { if (e.key === 'Enter') handleFriendClick(f); }}
                  >
                    <div className="friend-info">
                      <div className="friend-avatar"></div>
                      <span>
                        {f.username} {currentUserId && f.id === currentUserId && <span className="you-badge">(Вы)</span>}
                      </span>
                    </div>
                    {isCurrentUserProfile && (
                      <div className="friend-actions" onClick={e => e.stopPropagation()}>
                        <button
                          onClick={() => handleRemoveFriend(f.id)}
                          className="friend-action-btn decline"
                          type="button"
                        >
                          Удалить
                        </button>
                      </div>
                    )}
                  </li>
                ))}
              </ul>
            )}
          </div>
        );

        case 'settings':
          return (
            <div className="tab-content">
              <h3>Настройки</h3>
              {isCurrentUserProfile ? (
                <div className="settings-item">
                  <label>Видимость ваших отзывов:</label>
                  <select value={ratingVisibility} onChange={handleVisibilityChange}>
                    <option value="ALL">Все</option>
                    <option value="FRIENDS">Только друзья</option>
                  </select>
                </div>
              ) : (
                <p>Вы не можете изменять настройки другого пользователя.</p>
              )}
            </div>
          );

      default:
        return null;
    }
  };

  return (
    <div className="user-profile">
      <h2>Профиль пользователя {userInfo?.username && `— ${userInfo.username}`}</h2>
      <div className="tabs">
        <button className={activeTab === 'info' ? 'active' : ''} onClick={() => setActiveTab('info')}>Информация</button>
        <button className={activeTab === 'ratings' ? 'active' : ''} onClick={() => setActiveTab('ratings')}>Отзывы</button>
        <button className={activeTab === 'friends' ? 'active' : ''} onClick={() => setActiveTab('friends')}>Друзья</button>
        {isCurrentUserProfile && (
          <button className={activeTab === 'settings' ? 'active' : ''} onClick={() => setActiveTab('settings')}>Настройки</button>
        )}
      </div>
      {renderTabContent()}
    </div>
  );
}

export default UserProfile;
