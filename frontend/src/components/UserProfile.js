import '../styles/UserProfile.css';
import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';

function UserProfile() {
  const { id } = useParams();
  const navigate = useNavigate();
  const token = localStorage.getItem('token');
  const headers = { Authorization: `Bearer ${token}` };
  const [currentUserId, setCurrentUserId] = useState(null);

  const isCurrentUserProfile = !id || id === 'me';
  const [activeTab, setActiveTab] = useState('info');
  const [userInfo, setUserInfo] = useState(null);
  const [userRatings, setUserRatings] = useState(null);
  const [userFriends, setUserFriends] = useState(null);

  useEffect(() => {
    setActiveTab('info');
    setUserInfo(null);
    setUserRatings(null);
    setUserFriends(null);
  }, [id]);

  useEffect(() => {
    const fetchCurrentUser = async () => {
      try {
        const res = await fetch('http://localhost:8081/api/users/me/info', { headers });
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

  useEffect(() => {
    const fetchInfo = async () => {
      try {
        const baseUrl = 'http://localhost:8081/api/users';
        const url = isCurrentUserProfile ? `${baseUrl}/me/info` : `${baseUrl}/${id}/info`;
        const res = await fetch(url, { headers });
        if (res.ok) {
          const data = await res.json();
          setUserInfo(data);
        }
      } catch (err) {
        console.error('Ошибка при загрузке информации:', err);
      }
    };

    fetchInfo();
  }, [id]);

  useEffect(() => {
    if (activeTab === 'ratings' && userRatings === null) {
      const fetchRatings = async () => {
        try {
          const baseUrl = 'http://localhost:8081/api/ratings';
          const url = isCurrentUserProfile ? `${baseUrl}/users/me` : `${baseUrl}/users/${id}`;
          const res = await fetch(url, { headers });
          if (res.ok) {
            const data = await res.json();
            setUserRatings(data?.ratings ?? []);
          }
        } catch (err) {
          console.error('Ошибка при загрузке отзывов:', err);
        }
      };

      fetchRatings();
    }
  }, [activeTab, id]);

  useEffect(() => {
    if (activeTab === 'friends' && userFriends === null) {
      const fetchFriends = async () => {
        try {
          const baseUrl = 'http://localhost:8081/api/users';
          const url = isCurrentUserProfile ? `http://localhost:8081/api/friends` : `${baseUrl}/${id}/friends`;
          const res = await fetch(url, { headers });
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

  const handleFriendClick = (friend) => {
    const isMe = currentUserId && friend.id === currentUserId;
    navigate(`/profile/${isMe ? 'me' : friend.id}`);
  };

  const handleRemoveFriend = async (friendId) => {
    if (!window.confirm('Вы уверены, что хотите удалить этого пользователя из друзей?')) return;
    try {
      const res = await fetch(`http://localhost:8081/api/friends/remove/${friendId}`, {
        method: 'DELETE',
        headers,
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

  const renderTabContent = () => {
    switch (activeTab) {
      case 'info':
        return (
          <div className="tab-content">
            <div className="profile-header">
              <div className="avatar large"></div>
              <h3>{userInfo?.username ?? 'Загрузка...'}</h3>
            </div>
          </div>
        );
        case 'ratings':
          return (
            <div className="tab-content">
              <h3>Отзывы</h3>
              {userRatings === null ? (
                <p>Загрузка...</p>
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
                          <div className="friend-avatar">
                            {/* Если есть URL аватарки, то заменить div на img: <img src={f.avatarUrl} alt={`${f.username} avatar`} /> */}
                          </div>
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
          
        
      default:
        return null;
    }
  };

  return (
    <div className="user-profile">
      <h2>Профиль пользователя {userInfo?.username && `— ${userInfo.username}`}</h2>
      <div className="tabs">
        <button
          className={activeTab === 'info' ? 'active' : ''}
          onClick={() => setActiveTab('info')}
        >
          Информация
        </button>
        <button
          className={activeTab === 'ratings' ? 'active' : ''}
          onClick={() => setActiveTab('ratings')}
        >
          Отзывы
        </button>
        <button
          className={activeTab === 'friends' ? 'active' : ''}
          onClick={() => setActiveTab('friends')}
        >
          Друзья
        </button>
      </div>
      {renderTabContent()}
    </div>
  );
}

export default UserProfile;
