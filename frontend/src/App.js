import React from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import Login from './components/Login';
import Register from './components/Register';
import Movies from './components/Movies';
import MovieDetails from './components/MovieDetails';
import Header from './components/Header';
import UserProfile from './components/UserProfile';
import Friends from './components/Friends';
import Favorite from './components/Favorite';




function App() {
  return (
    <Router>
      <Header />
      <Routes>
        <Route path="/" element={<Navigate to="/login" />} />
        <Route path="/login" element={<Login />} />
        <Route path="/register" element={<Register />} />
        <Route path="/movies" element={<Movies />} />
        <Route path="/movies/:id" element={<MovieDetails />} />
        <Route path="/profile/:id" element={<UserProfile />} />
        <Route path="/friends" element={<Friends />} />
        <Route path="/favorites" element={<Favorite />} />
      </Routes>
    </Router>
  );
}

export default App;
