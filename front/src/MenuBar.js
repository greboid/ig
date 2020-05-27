import React, { useState, useEffect } from 'react'
import { Link } from 'react-router-dom'
import useAuthContext from './useAuthContext'
import './MenuBar.css'

function getCategories(setCategories) {
	fetch(process.env.REACT_APP_API_URL+'profiles')
      .then(response => response.json())
      .then(json => setCategories(Array.prototype.slice.call(json)))
}

const MenuBar = () => {
	const { authed, setLoggedOut } = useAuthContext();
	const [categories, setCategories] = useState([])
	useEffect(() => {
		getCategories(setCategories)
	}, []);
    return (
      <React.Fragment>
      	<nav>
		  <div>
			{categories.map((category, i) => {
		    	return (<Link key={i} to={'/category/'+category}>{category}</Link>)
			})}
		  </div>
		  <div>
              <Link to='/raw'>IG Links</Link>
		  	{authed ?
		  		<React.Fragment>
			  		<Link to='/admin'>Admin</Link>
			  		<Link to='#' onClick={() => setLoggedOut()}>Logout</Link>
		  		</React.Fragment>
		  	:
		  		<Link to='/login'>Login</Link>
		  	}
		  </div>
		</nav>
      </React.Fragment>
    )
}

export default MenuBar
