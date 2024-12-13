import api from "../axiosConfig"
import React, { useEffect, useState } from 'react';
import DemographicsTable from '../components/DemographicsTable';

const DemographicMetrics = () => {
  const [data, setData] = useState([]);
  const headings = ["Age", "Gender", "Location", "Average Rating", "Total Ratings"];

  useEffect(() => {
    async function fetchData() {
        try {
            const response = await api.get('/api/demographics-metrics')
            setData(response.data)
        } catch (error) {
            console.error('Error fetching data:', error);
        }
    }

    fetchData(); // Call the async function
  }, []);

  return (
    <div>
      <h1>User Demographic Metrics</h1>
      <DemographicsTable headings={headings} data={data} />
    </div>
  );
};

export default DemographicMetrics;
