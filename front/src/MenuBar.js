import React, { useState, useEffect } from 'react'
import { Link } from 'react-router-dom'
import { Navbar, Nav } from 'react-bootstrap'
import useAuthContext from './useAuthContext'

function getCategories(setCategories) {
	fetch(process.env.REACT_APP_API_URL+'profiles')
      .then(response => response.json())
      .then(json => setCategories(Array.prototype.slice.call(json)))
}

export default function MenuBar() {
	const { authed, setLoggedOut } = useAuthContext();
	const [categories, setCategories] = useState([])
	useEffect(() => {
		getCategories(setCategories)
	}, []);
    return (
      <React.Fragment>
      	<Navbar>
		  <Navbar.Brand>IG</Navbar.Brand>
		  <Nav className="mr-auto">
			{categories.map((category, i) => {
		    	return (<Nav.Link key={i} as={Link} to={'/category/'+category}>{category}</Nav.Link>)
			})}
		  </Nav>
		  <Nav>
		  	{authed ?
		  		<React.Fragment>
			  		<Nav.Link as={Link} to='/admin'>Admin</Nav.Link>
			  		<Nav.Link as={Link} to='#' onClick={() => setLoggedOut()}>Logout</Nav.Link>
		  		</React.Fragment>
		  	:
		  		<Nav.Link as={Link} to='/login'>Login</Nav.Link>
		  	}
		  </Nav>
		</Navbar>
      </React.Fragment>
    )
}