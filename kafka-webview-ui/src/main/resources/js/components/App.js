import React from 'react';
import Stream from './Stream';

export default class App extends React.Component {
  render() {
    console.log('props', this.props);
    return (
      <div>
        <Stream {...this.props} />
      </div>
    );
  }
}
