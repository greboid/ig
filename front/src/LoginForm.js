import React from 'react'
import "./LoginForm.css"
import useLoginForm from './useLoginForm';
import useAuthContext from './useAuthContext';
import { Redirect } from 'react-router-dom'
import MenuBar from './MenuBar'

export default function LoginForm() {

	const { authed } = useAuthContext();
	const {inputs, handleInputChange, handleSubmit } = useLoginForm("http://localhost:8080/login");

	return (
		<React.Fragment>
			{ authed && <Redirect to="/admin" /> }
			<MenuBar />
    		<div className="login-page" action="#" method="post">
			    <div className="form">
			        <form className="login-form" onSubmit={handleSubmit}>
			            <input 
			            	name="username"
			            	value={inputs.username} 
			            	onChange={handleInputChange}
			            	required={true} 
			            	type="text" 
			            	placeholder="username"
			            />
			            <input 
			            	name="password"
			            	value={inputs.password}
			            	onChange={handleInputChange}
			            	required={true}
			            	type="password"
			            	placeholder="password"
			            />
			            <button>login</button>
			        </form>
			    </div>
			</div>
    	</React.Fragment>
	);
}