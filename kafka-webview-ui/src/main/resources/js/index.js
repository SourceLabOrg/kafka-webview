import React from 'react';
import ReactDOM from 'react-dom';
import PropTypes from 'prop-types';
import { AppContainer } from 'react-hot-loader';
import Redbox from 'redbox-react';
import App from './components/App';

// Note that the error reporter will catch exceptions, NOT parse errors and then Redbox them
const CustomErrorReporter = ({ error }) => {
  return <Redbox error={error} />;
};

CustomErrorReporter.propTypes = {
  error: PropTypes.instanceOf(Error).isRequired,
};

const rootEl = document.getElementById('root');
const render = Component =>
  ReactDOM.render(
    <AppContainer errorReporter={Redbox}>
      <Component />
    </AppContainer>,
    rootEl
  );

render(App);

if (module.hot) {
  module.hot.accept('./components/App', () => render(App));
}
