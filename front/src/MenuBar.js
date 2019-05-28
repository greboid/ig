import React from 'react'
import { Link } from 'react-router-dom'
import { Navbar, Nav } from 'react-bootstrap'
import useAuthContext from './useAuthContext'

export default function MenuBar() {
	const { authed, setLoggedOut } = useAuthContext();
    return (
      <React.Fragment>
      	<Navbar>
		  <Navbar.Brand>IG</Navbar.Brand>
		  <Nav className="mr-auto">
		    <Nav.Link as={Link} to='/'>Home</Nav.Link>
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