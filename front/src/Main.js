import React, { useState, useEffect } from 'react'
import MenuBar from './MenuBar'
import Lightbox from './Lightbox'
import './Main.css'
import useInfiniteScroll from './useInfiniteScroll'

const MainPage = (props) => {
	const [images, setImages] = useState([])
	const [isFetching, setIsFetching] = useInfiniteScroll(fetchMoreListItems);
	const [lightboxData, setLightboxData] = useState({
		src: "",
		caption: "",
		alt: ""
	})
	function fetchMoreListItems() {
		getImages(props.location.pathname.split('/'), images.length)
		setIsFetching(false)
	}
	useEffect(() => {
		setImages([])
	}, [props.location.pathname]);
	useEffect(() => {
		if ((window.pageYOffset || document.documentElement.scrollTop) === 0) {
			getImages(props.location.pathname.split('/'), images.length)
		}
	}, [images])
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
	function showLightbox(i) {
		setLightboxData({
			src: images[i].url,
			caption: images[i].caption,
			alt: images[i].caption
		})
	}
	function closeLightbox() {
		setLightboxData({
			src: "",
			caption: "",
			alt: ""
		})
	}
	return (
		<React.Fragment>
		<Lightbox src={lightboxData.src} alt={lightboxData.caption} caption={lightboxData.caption} close={closeLightbox} />
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
				    			onClick={(event) => { showLightbox(i); event.preventDefault() } }
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