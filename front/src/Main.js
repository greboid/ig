import React, { useState, useEffect } from 'react'
import MenuBar from './MenuBar'
import './Main.css'
import Lightbox from 'lightbox-react'
import 'lightbox-react/style.css'
import VideoImage from './VideoImage'

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
	const [isOpen, setIsOpen] = useState(false);
	const [lightboxIndex, setLightboxIndex] = useState(1);
	useEffect(() => {
		var pathName = props.location.pathname.split('/')
		if (pathName[1] === 'user') {
			getUserImages(setImages, pathName[2])
		} else {
			getProfileImages(setImages, pathName[2])
		}
	}, [props.location.pathname]);
	function openLightbox(index) {
		setIsOpen(true)
		setLightboxIndex(index)
	}
	var lightbox = images.map((image, i) => {
		if (image.url.match(".*mp4.*")) {
			var img = {url: (
				VideoImage(image.url)
			)}
			return img
		} else {
			return image
		}
	})
	console.log(lightbox)
	return (
		<React.Fragment>
			<MenuBar />
			{isOpen && (
	          <Lightbox
	            mainSrc={lightbox[lightboxIndex].url}
	            nextSrc={lightbox[(lightboxIndex + 1) % lightbox.length].url}
	            prevSrc={lightbox[(lightboxIndex + lightbox.length - 1) % lightbox.length].url}
	            onCloseRequest={() => setIsOpen(false) }
	            onMovePrevRequest={() =>setLightboxIndex((lightboxIndex + lightbox.length - 1) % lightbox.length) }
	            onMoveNextRequest={() =>setLightboxIndex((lightboxIndex + 1) % lightbox.length) }
	          />
	        )}
			<div id="app" className="contentContainer">
			    {images.map((image, i) => { 
			    	return (
			    		<a 
			    			onClick={ (event) => { event.preventDefault(); openLightbox(i) } } 
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
		</React.Fragment>
	);
}

export default MainPage