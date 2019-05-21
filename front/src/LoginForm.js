import React from 'react'
import "./LoginForm.css"

class LoginForm extends React.Component {

	constructor(props) {
    	super(props);
    	this.state = {
			loginUsername: "",
			loginPassword: "",
			loginError: ""
    	}
    	this.handleLoginSubmit = this.handleLoginSubmit.bind(this);
    	this.handleUsernameChange = this.handleUsernameChange.bind(this);
    	this.handlePasswordChange = this.handlePasswordChange.bind(this);
	}

	handleUsernameChange(event) {
		this.setState({loginUsername: event.target.value});
	}

	handlePasswordChange(event) {
		this.setState({loginPassword: event.target.value});
	}

	handleLoginSubmit(event) {
		event.preventDefault()
		var login = this
		fetch(
			login.props.apiURL+'/login', {
				method: 'POST',
				headers: {
				'Content-Type': 'application/json'
			}, 
			body: JSON.stringify({ 
				user: this.state.loginUsername, password: this.state.loginPassword
			})
		})
		.then(function(response) {
			return response.json();
		})
		.then(function(response) {
			if (response.hasOwnProperty("token") && response.hasOwnProperty("expires")) {
				login.props.setAuthInfo(response.token, response.expires)
			} else {
				login.setState({
					loginPassword: "", 
					loginUsername: "", 
					loginError: response.message
				});
			}
		})
		.catch(function(error) {
			login.setState({
				loginPassword: "", 
				loginUsername: "", 
				loginError: "Unable to login, try again later"
			});
		})
	}

	render() {
	    return (
	    	<React.Fragment>
	    		<div className="login-page" action="#" method="post">
				    <div className="form">
				    	<div>{this.state.loginError && this.state.loginError}</div>
				        <form className="login-form" onSubmit={this.handleLoginSubmit}>
				            <input 
				            	value={this.state.username} 
				            	onChange={this.handleUsernameChange} 
				            	required={true} 
				            	type="text" 
				            	placeholder="username"
				            />
				            <input 
				            	value={this.state.password}
				            	onChange={this.handlePasswordChange}
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
}

export default LoginForm;