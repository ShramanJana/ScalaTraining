import React from 'react';

const DemographicTable = ({ headings, data }) => {
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
                <td>{row.age}</td>
                <td>{row.gender}</td>
                <td>{row.location}</td>
                <td>{row.averageRating}</td>
                <td>{row.totalRating}</td>
          </tr>
        ))}
      </tbody>
    </table>
  );
};

export default DemographicTable;
