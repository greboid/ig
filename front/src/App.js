import React from 'react'
import { BrowserRouter as Router, Route, Switch } from 'react-router-dom'
import AdminPage from './Admin'
import LoginForm from './LoginForm'
import MenuBar from './MenuBar'
import useAuthContext from './useAuthContext';
import PrivateRoute from './PrivateRoute';

export default function App() {
	return (
		<useAuthContext.Provider>
			<Router>
				<Switch>
					<Route exact path="/" component={MenuBar} />
					<PrivateRoute exact path="/admin" component={AdminPage} />
					<Route exact path="/login" component={LoginForm} />
				</Switch>
			</Router>
		</useAuthContext.Provider>
	);
}