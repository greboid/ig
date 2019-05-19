import React from 'react'
import { BrowserRouter as Router, Route, Switch } from 'react-router-dom'
import Admin from './Admin';

export default class App extends React.Component {
	render() {
		return (
			<Router>
				<Switch>
					<Route exact path="/" component={Admin} />
				</Switch>
			</Router>
		);
	}
}