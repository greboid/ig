import React, { useState, useEffect } from 'react'
import MenuBar from './MenuBar'
import Lightbox from './Lightbox'
import './Main.css'
import useInfiniteScroll from './useInfiniteScroll'
import useWindowSize from '@rehooks/window-size'
import useDeepCompareEffect from 'use-deep-compare-effect'

const MainPage = ({match}) => {
	let type = match.params.type
	let name = match.params.name
	let typeName = type + "/" + name
	let windowSize = useWindowSize()
	const [images, setImages] = useState({
	})
	const [isFetching, setIsFetching] = useInfiniteScroll(fetchMoreListItems);
	const [lightboxData, setLightboxData] = useState({
		src: "",
		caption: "",
		alt: "",
		index: -1
	})
	function fetchMoreListItems() {
		if (images[typeName] === undefined) return
		getImages(images[typeName].length)
		setIsFetching(false)
	}
	useEffect(() => {
		setImages({
			[typeName]: []
		})
	}, [type, name]);
	useDeepCompareEffect(() => {
		if (isFetching) return
		if (images[typeName] === undefined) return
		if (images[typeName].length === 0) {
			setIsFetching(false)
			getImages(0)
		} else {
			if ((window.scrollMaxY || (document.body.scrollHeight - window.innerHeight)) === 0) {
				getImages(images[typeName].length)
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
		fetch(process.env.REACT_APP_API_URL+'igposts/?start='+offset+'&count='+50+'&profile='+profile)
	      .then(response => response.json())
	      .then(json => {
	      	if (json.length !== 0) {
	      		let tempImages = Object.assign({}, images)
	      		tempImages[typeName] = tempImages[typeName].concat(Array.prototype.slice.call(json))
	      		setImages(tempImages)
	      	}
	      })
	}

	function getUserImages(profile, offset=0) {
		fetch(process.env.REACT_APP_API_URL+'igposts/?start='+offset+'&count='+50+'&user='+profile)
	      .then(response => response.json())
	      .then(json => {
	      	if (json.length !== 0) {
	      		let tempImages = Object.assign({}, images)
	      		tempImages[typeName] = tempImages[typeName].concat(Array.prototype.slice.call(json))
	      		setImages(tempImages)
	      	}
	      })
	}
	function showLightbox(i) {
		if (i >= images[typeName].length || i < 0) { return }
		setLightboxData({
			src: images[typeName][i].url,
			caption: images[typeName][i].caption,
			alt: images[typeName][i].caption,
			source: images[typeName][i].source,
			shortcode: images[typeName][i].shortcode,
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
			<MenuBar />
			<div id="app" className="contentContainer">
			    {images[typeName] !== undefined && images[typeName].map((image, i) => { 
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
		</React.Fragment>
	);
}

export default MainPage