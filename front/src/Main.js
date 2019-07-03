import React, { useState, useEffect } from 'react'
import MenuBar from './MenuBar'
import './Main.css'
import useInfiniteScroll from './useInfiniteScroll'

const MainPage = (props) => {
	const [images, setImages] = useState([])
	const [isFetching, setIsFetching] = useInfiniteScroll(fetchMoreListItems);
	function fetchMoreListItems() {
		getImages(props.location.pathname.split('/'), images.length)
		setIsFetching(false)
	}
	useEffect(() => {
		setImages([])
	}, [props.location.pathname]);
	useEffect(() => {
		if (document.documentElement.scrollTopMax === 0) {
			getImages(props.location.pathname.split('/'), images.length)
		}
	}, [images, props.location.pathname])
	function getImages(path, offset=0, firstCall=false) {
		if (path[1] === 'user') {
			getUserImages(path[2], offset, true)
		} else {
			getProfileImages(path[2], offset, true)
		}
	}
	function getProfileImages(profile, offset=0) {
		fetch(process.env.REACT_APP_API_URL+'igposts/?start='+offset+'&count='+60+'&profile='+profile)
	      .then(response => response.json())
	      .then(json => {
	      	if (json.length !== 0) {
	      		setImages(images.concat(Array.prototype.slice.call(json)))
	      	}
	      })
	}

	function getUserImages(profile, offset=0) {
		fetch(process.env.REACT_APP_API_URL+'igposts/?start='+offset+'&count='+60+'&user='+profile)
	      .then(response => response.json())
	      .then(json => {
	      	if (json.length !== 0) {
	      		setImages(images.concat(Array.prototype.slice.call(json)))
	      	}
	      })
	}
	return (
		<React.Fragment>
			<MenuBar />
			<div id="app" className="contentContainer">
			    {images.map((image, i) => { 
			    	return (
			    		<a 
			    			key={i+1} 
			    			className="item" 
			    			href={image.url} 
			    		>
				    		<img 
				    			className="itemimage" 
				    			key={i} 
				    			src={process.env.REACT_APP_API_URL+image.thumb} 
				    			alt={image.source + ' ' + image.shortcode} 
				    		/>
			    		</a>
			    	)
			    })}
			</div>
			{isFetching && <p>Loading more posts.</p>}
		</React.Fragment>
	);
}

export default MainPage