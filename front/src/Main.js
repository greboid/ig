import React, { useState, useEffect } from 'react'
import MenuBar from './MenuBar'
import Lightbox from './Lightbox'
import './Main.css'
import useInfiniteScroll from './useInfiniteScroll'
import useWindowSize from '@rehooks/window-size'

const MainPage = ({match}) => {
	let type = match.params.type
	let name = match.params.name
	let windowSize = useWindowSize()
	const [images, setImages] = useState([])
	const [isFetching, setIsFetching] = useInfiniteScroll(fetchMoreListItems);
	const [lightboxData, setLightboxData] = useState({
		src: "",
		caption: "",
		alt: "",
		index: -1
	})
	function fetchMoreListItems() {
		getImages(images.length)
		setIsFetching(false)
	}
	useEffect(() => {
		setImages([])
	}, [type, name]);
	useEffect(() => {
		if (isFetching) return
		if (images.length === 0) {
			setIsFetching(false)
			getImages(images.length)
		} else {
			if ((window.scrollMaxY || (document.body.scrollHeight - window.innerHeight)) === 0) {
				getImages(images.length)
			}
		}
	// eslint-disable-next-line react-hooks/exhaustive-deps
	}, [images, getImages])
	function getImages(offset=0, firstCall=false) {
		setIsFetching(true)
		if (type === 'user') {
			getUserImages(name, offset, true)
		} else {
			getProfileImages(name, offset, true)
		}
	}
	function getProfileImages(profile, offset=0) {
		fetch(process.env.REACT_APP_API_URL+'igposts/?start='+offset+'&count='+150+'&profile='+profile)
	      .then(response => response.json())
	      .then(json => {
	      	if (json.length !== 0) {
	      		setImages(images.concat(Array.prototype.slice.call(json)))
	      	}
	      })
	}

	function getUserImages(profile, offset=0) {
		fetch(process.env.REACT_APP_API_URL+'igposts/?start='+offset+'&count='+150+'&user='+profile)
	      .then(response => response.json())
	      .then(json => {
	      	if (json.length !== 0) {
	      		setImages(images.concat(Array.prototype.slice.call(json)))
	      	}
	      })
	}
	function showLightbox(i) {
		if (i >= images.length || i < 0) { return }
		setLightboxData({
			src: images[i].url,
			caption: images[i].caption,
			alt: images[i].caption,
			source: images[i].source,
			shortcode: images[i].shortcode,
			index: i
		})
	}
	function closeLightbox() {
		setLightboxData({
			src: "",
			caption: "",
			alt: "",
			source: "",
			shortcode: "",
			index: -1
		})
	}
	return (
		<React.Fragment>
		<Lightbox 
			src={lightboxData.src}
			alt={lightboxData.caption}
			caption={lightboxData.caption}
			source={lightboxData.source}
			shortcode={lightboxData.shortcode}
			close={closeLightbox}
			prev={() => showLightbox(lightboxData.index -1)}
			next={() => showLightbox(lightboxData.index +1)}
		/>
			<MenuBar isFetching={isFetching} />
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