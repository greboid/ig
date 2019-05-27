import React from 'react'
import { Route, Redirect } from 'react-router-dom'
import useAuthContext from './useAuthContext';

export default function PrivateRoute({component: Component, ...rest}) {
	const { authed } = useAuthContext();
	return (
  		<Route {...rest} render={(props) => ( authed ? <Component {...props} /> : <Redirect to='/login' /> )} />
  	);
}