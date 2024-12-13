import api from "../axiosConfig"
import React, { useEffect, useState } from 'react';
import MetricsTable from '../components/MetricsTable';

const MovieMetrics = () => {
  const [data, setData] = useState([]);
  const headings = ["Movie Id", "Movie Title", "Genres", "Average Rating", "Total Ratings"];

  useEffect(() => {
    async function fetchData() {
        try {
            const response = await api.get('/api/movie-metrics')
            setData(response.data)
        } catch (error) {
            console.error('Error fetching data:', error);
        }
    }

    fetchData(); // Call the async function
  }, []);

  return (
    <div>
      <h1>Movie Metrics</h1>
      <MetricsTable headings={headings} data={data} />
    </div>
  );
};

export default MovieMetrics;
