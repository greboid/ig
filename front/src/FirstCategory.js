import React, { useState, useEffect } from 'react'
import { Redirect } from 'react-router-dom'

function getFirstCategory(setfirstCat) {
	fetch(process.env.REACT_APP_API_URL+'profiles')
      .then(response => response.json())
      .then(json => Array.prototype.slice.call(json)[0])
      .then(value => setfirstCat(value)) 
}

export default function FirstCategory() {
	const [firstCat, setfirstCat] = useState("")
	useEffect(() => {
		getFirstCategory(setfirstCat)
	}, [])
	if (firstCat === "") {
		return (<React.Fragment></React.Fragment>)
	} else {
		return (<Redirect to={"/category/"+firstCat} />)
	}
}