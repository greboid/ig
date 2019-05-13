import React from 'react'
import "./LoginForm.css"

class LoginForm extends React.Component {
	render() {
    return (
    	<React.Fragment>
    		<div className="login-page" action="#" method="post">
			    <div className="form">
			        <form className="login-form" onSubmit={this.props.handleSubmit}>
			            <input value={this.props.username} onChange={this.props.handleUsernameChange} type="text" placeholder="username"/>
			            <input value={this.props.password} onChange={this.props.handlePasswordChange} type="password" placeholder="password"/>
			            <button>login</button>
			        </form>
			    </div>
			</div>
    	</React.Fragment>
    	);
  	}
}

export default LoginForm;