import React from 'react';
import { BrowserRouter as Router, Routes, Route, Link } from 'react-router-dom';
import MovieMetrics from './pages/MovieMetrics';
import GenreMetrics from './pages/GenreMetrics';
import DemographicMetrics from './pages/DemographicMetrics';

function App() {
  return (
    <Router>
      <div>
        <nav>
          <ul>
            <li><Link to="/movie-metrics">Movie Metrics</Link></li>
            <li><Link to="/genre-metrics">Genre Metrics</Link></li>
            <li><Link to="/demographic-metrics">Demographic Metrics</Link></li>
          </ul>
        </nav>
        <Routes>
          <Route path="/movie-metrics" element={<MovieMetrics />} />
          <Route path="/genre-metrics" element={<GenreMetrics />} />
          <Route path="/demographic-metrics" element={<DemographicMetrics />} />
        </Routes>
      </div>
    </Router>
  );
}

export default App;
