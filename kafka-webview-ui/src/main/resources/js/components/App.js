import React from 'react';
import HelloWorld from './HelloWorld';

export default class App extends React.Component {
  constructor(props) {
    super(props);
    this.state = {
      messages: [],
    };
  }

  componentWillMount() {
    var socket = new SockJS('/websocket');
    var stompClient = Stomp.over(socket);
    console.log('Connecting...');
    stompClient.connect({}, frame => {
      console.log('Connected...');
      stompClient.subscribe('/user/topic/view/1/1', message => {
        console.log('Subscribing...');
        var data = JSON.parse(message.body);
        this.setState({
          messages: [...this.state.messages, data],
        });
      });
      stompClient.send(
        '/websocket/consume/1',
        {},
        JSON.stringify({
          partitions: '0',
          filters: [],
          action: 'head',
        })
      );
    });
  }

  render() {
    return (
      <div>
        <h1>Test React Component!!!</h1>
        <HelloWorld />
        <ul>
          {this.state.messages.map(message => (
            <li key={'message' + message.offset}>{message.value}</li>
          ))}
        </ul>
      </div>
    );
  }
}
