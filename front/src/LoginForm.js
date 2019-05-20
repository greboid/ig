import React from 'react'
import "./LoginForm.css"

class LoginForm extends React.Component {
	render() {
    return (
    	<React.Fragment>
    		<div className="login-page" action="#" method="post">
			    <div className="form">
			    	<div>{this.props.error && this.props.error}</div>
			        <form className="login-form" onSubmit={this.props.handleSubmit}>
			            <input value={this.props.username} onChange={this.props.handleUsernameChange} required={true} type="text" placeholder="username"/>
			            <input value={this.props.password} onChange={this.props.handlePasswordChange} required={true} type="password" placeholder="password"/>
			            <button>login</button>
			        </form>
			    </div>
			</div>
    	</React.Fragment>
    	);
  	}
}

export default LoginForm;