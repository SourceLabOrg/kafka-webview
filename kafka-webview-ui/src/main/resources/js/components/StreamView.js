import React from 'react';
import MessageTable from './MessageTable';

export default function({ id, name, topic, messages }) {
  return (
    <div className="row">
      <div className="col-lg-12">
        <div className="card">
          <div className="card-header">
            <i className="fa fa-align-justify" />
            Stream <strong>{name}</strong> over topic <strong>{topic}</strong>
            <div
              className="btn-group float-right"
              role="group"
              aria-label="Button group"
            >
              <a
                className="btn"
                href={`/view/${id}`}
                style={{ paddingBottom: 0 }}
              >
                <i className="icon-eye" />
                &nbsp;Switch to View
              </a>
            </div>
          </div>
          <div className="card-body">
            <div className="row">
              <div className="col-md-6">
                <form className="form-inline">
                  <div className="form-group">
                    <label for="connect" style={{ paddingRight: 15 }}>
                      Stream Connection:
                    </label>
                    <button
                      id="connect"
                      className="btn btn-sm btn-default"
                      type="submit"
                    >
                      Connect
                    </button>
                    <button
                      id="pause"
                      className="btn btn-sm btn-default"
                      type="submit"
                      disabled="disabled"
                    >
                      Pause
                    </button>
                    <button
                      id="resume"
                      className="btn btn-sm btn-default"
                      type="submit"
                      disabled="disabled"
                    >
                      Resume
                    </button>
                    <button
                      id="disconnect"
                      className="btn btn-sm btn-default"
                      type="submit"
                      disabled="disabled"
                    >
                      Disconnect
                    </button>
                  </div>
                </form>
              </div>
            </div>
            <br />

            <div className="row">
              <div className="col-md-12">
                <div
                  className="alert alert-light"
                  role="alert"
                  id="notConnected"
                  style={{ display: 'block' }}
                >
                  <h4 className="alert-heading">Stream Disconnected</h4>
                  <p>
                    Review{' '}
                    <i>
                      <strong>Stream Settings</strong>
                    </i>{' '}
                    to alter behavior of consumer.<br />
                    Press{' '}
                    <i>
                      <strong>Connect</strong>
                    </i>{' '}
                    to start the streaming consumer.<br />
                  </p>
                </div>
              </div>
            </div>

            <div className="row">
              <div className="col-md-12">
                {messages &&
                  messages.length === 0 && <em>Waiting for messages...</em>}
                {messages &&
                  messages.length > 0 && <MessageTable messages={messages} />}
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
