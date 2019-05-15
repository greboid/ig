import React from 'react'
import { Navbar, Nav } from 'react-bootstrap'

class MenuBar extends React.Component {
	render() {
	    return (
	      <React.Fragment>
	      	<Navbar>
			  <Navbar.Brand>IG</Navbar.Brand>
			  <Nav className="mr-auto">
			    <Nav.Link href='/'>Home</Nav.Link>
			  </Nav>
			  <Nav>
			  	{this.props.authToken === "" ?
			  		<Nav.Link href='/login'>Login</Nav.Link>
			  	:
			  		<Nav.Link href='/logout'>Logout</Nav.Link>
			  	}
			  </Nav>
			</Navbar>
	      </React.Fragment>
	    )
	}
}

export default MenuBar;