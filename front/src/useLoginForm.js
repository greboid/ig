import {useState} from 'react';
import useAuthContext from './useAuthContext';
import { withRouter } from 'react-router-dom'

const useLoginForm = (postURL) => {
	const [inputs, setInputs] = useState({username: "", password: ""});
	const { setLoggedIn } = useAuthContext();

	const handleSubmit = (event) => {
		if (event) {
			event.preventDefault();
		}
		callback(postURL, inputs, setLoggedIn);
	}

	const handleInputChange = (event) => {
		event.persist();
		setInputs(inputs => ({...inputs, [event.target.name]: event.target.value}));
	}

	return {
		handleSubmit,
		handleInputChange,
		inputs
	};
}

function callback(postURL, inputs, setLoggedIn) {
	fetch(
		postURL, {
			method: 'POST',
			headers: {
			'Content-Type': 'application/json'
		}, 
		body: JSON.stringify({ 
			user: inputs.username, password: inputs.password
		})
	})
	.then(function(response) {
		return response.json();
	})
	.then(function(response) {
		if (response.hasOwnProperty("token") && response.hasOwnProperty("expires")) {
			setLoggedIn(response.token, response.expires)
			withRouter(
			  ({ history }) => history.push("/admin")
			)
		} else {
			console.log(`Error: ${response.message}`)
		}
	})
	.catch(function(error) {
		console.log(`Error: No endpoint`)
	})
}

export default useLoginForm