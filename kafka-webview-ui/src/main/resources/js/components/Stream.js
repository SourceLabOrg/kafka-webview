import React from 'react';
import StreamView from './StreamView';

export default class Stream extends React.Component {
  constructor(props) {
    super(props);
    this.state = { messages: [] };
  }

  componentWillMount() {
    // TODO: Move this into something that can be mocked over easily
    var socket = new SockJS('/websocket');
    var stompClient = Stomp.over(socket);
    // Remove this line and debug data is logged to the console
    stompClient.debug = null;

    stompClient.connect({}, frame => {
      stompClient.subscribe('/user/topic/view/1/1', message => {
        var data = JSON.parse(message.body);
        this.addMessage(data);
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

  addMessage(message) {
    // Prepend the new message and take the last 19, for 20 total
    this.setState({ messages: [message, ...this.state.messages.slice(0, 19)] });
  }

  render() {
    return <StreamView messages={this.state.messages} />;
  }
}
