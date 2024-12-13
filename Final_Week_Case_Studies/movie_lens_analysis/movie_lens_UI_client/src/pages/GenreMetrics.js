import api from "../axiosConfig"
import React, { useEffect, useState } from 'react';
import GenreTable from '../components/GenreTable';

const GenreMetrics = () => {
  const [data, setData] = useState([]);
  const headings = ["Genre", "Average Rating", "Total Ratings"];

  useEffect(() => {
    async function fetchData() {
        try {
            const response = await api.get('/api/genre-metrics')
            setData(response.data)
        } catch (error) {
            console.error('Error fetching data:', error);
        }
    }

    fetchData(); // Call the async function
  }, []);

  return (
    <div>
      <h1>Genre Metrics</h1>
      <GenreTable headings={headings} data={data} />
    </div>
  );
};

export default GenreMetrics;
