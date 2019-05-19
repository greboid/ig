import React from 'react';
import ReactDOM from 'react-dom';
import Admin from './Admin';
import * as serviceWorker from './serviceWorker';

ReactDOM.render(<Admin />, document.getElementById('root'));

serviceWorker.unregister();
