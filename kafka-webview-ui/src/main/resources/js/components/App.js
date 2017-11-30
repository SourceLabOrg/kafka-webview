import React from 'react';
import HelloWorld from './HelloWorld';

export default class App extends React.Component {
  render() {
    console.log('Rendering <App/>...');

    return (
      <div>
        <h1>Test React Component!!!</h1>
        <HelloWorld />
      </div>
    );
  }
}
