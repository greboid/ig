import React, { useState, useEffect } from 'react'
import MenuBar from './MenuBar'
import './Main.css'

function getProfileImages(setImages, profile, offset=0) {
	fetch(process.env.REACT_APP_API_URL+'igposts/?start='+offset+'&count='+(offset+150)+'&profile='+profile)
      .then(response => response.json())
      .then(json => setImages(Array.prototype.slice.call(json)))
}

function getUserImages(setImages, profile, offset=0) {
	fetch(process.env.REACT_APP_API_URL+'igposts/?start='+offset+'&count='+(offset+150)+'&user='+profile)
      .then(response => response.json())
      .then(json => setImages(Array.prototype.slice.call(json)))
}

const MainPage = (props) => {
	const [images, setImages] = useState([])
	useEffect(() => {
		var pathName = props.location.pathname.split('/')
		if (pathName[1] == 'user') {
			getUserImages(setImages, pathName[2])
		} else {
			getProfileImages(setImages, pathName[2])
		}
	}, [props.location.pathname]);
	return (
		<React.Fragment>
			<MenuBar />
			<div id="app" className="contentContainer">
			    {images.map((image, i) => {
			    	return (<a key={i} className="item" href={image.url} ><img className="itemimage" key={i} src={process.env.REACT_APP_API_URL+image.thumb} alt={image.source + ' ' + image.shortcode}/></a>)
			    })}
			</div>
		</React.Fragment>
	);
}

export default MainPage