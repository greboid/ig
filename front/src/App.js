import React from 'react'
import { BrowserRouter as Router, Route, Switch } from 'react-router-dom'
import AdminPage from './Admin'
import LoginForm from './LoginForm'
import MainPage from './Main'
import IGRaw from './IGRaw'
import useAuthContext from './useAuthContext';
import PrivateRoute from './PrivateRoute';
import FirstCategory from './FirstCategory';

export default function App() {
	return (
		<useAuthContext.Provider>
			<Router>
				<Switch>
					<Route exact path="/" component={FirstCategory} />
					<PrivateRoute exact path="/admin" component={AdminPage} />
					<Route exact path="/login" component={LoginForm} />
					<Route path="/:type/:name" component={MainPage} />
					<Route path="/raw" component={IGRaw} />
				</Switch>
			</Router>
		</useAuthContext.Provider>
	);
}
