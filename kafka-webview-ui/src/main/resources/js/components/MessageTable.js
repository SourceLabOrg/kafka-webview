import React from 'react';
import MessageRow from './MessageRow';

export default function({ messages }) {
  return (
    <table className="table table-bordered table-striped table-sm" id="results">
      <thead>
        <tr>
          <th>Partition</th>
          <th>Offset</th>
          <th>Timestamp</th>
          <th>Key</th>
          <th>Value</th>
        </tr>
      </thead>
      <tbody>
        {messages &&
          messages.map(message => (
            <MessageRow key={'message' + message.offset} message={message} />
          ))}
      </tbody>
    </table>
  );
}
