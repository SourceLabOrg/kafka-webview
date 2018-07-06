import React from 'react';

export default function({ index, message }) {
  return (
    <tr>
      <td>{message.partition}</td>
      <td>{message.offset}</td>
      <td>{message.timestamp}</td>
      <td>{message.key}</td>
      <td>{message.value}</td>
    </tr>
  );
}
