import React from 'react';

const MetricsTable = ({ headings, data }) => {
  return (
    <table border="1" style={{ width: '100%', textAlign: 'left', marginTop: '20px' }}>
      <thead>
        <tr>
          {headings.map((heading, index) => (
            <th key={index}>{heading}</th>
          ))}
        </tr>
      </thead>
      <tbody>
        {data?.map((row, index) => (
          <tr key={index}>
                <td>{row.movieId}</td>
                <td>{row.title}</td>
                <td>{row.genres}</td>
                <td>{row.averageRating}</td>
                <td>{row.totalRating}</td>
          </tr>
        ))}
      </tbody>
    </table>
  );
};

export default MetricsTable;
