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
	let bigger = window.matchMedia('(max-device-width: 480px) or (min-width: 2000px)').matches
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
	}, [type, name, typeName]);
	useDeepCompareEffect(() => {
		if (isFetching) return
		if (images[typeName] === undefined) return
		setIsFetching(false)
		if (images[typeName].length === 0) {
			if (bigger) {
				getImages(0, parseInt(windowSize.innerHeight/200 * windowSize.innerWidth/200 + 10))
			} else {
				getImages(0, parseInt(windowSize.innerHeight/100 * windowSize.innerWidth/100 + 10))
			}
		}
	// eslint-disable-next-line react-hooks/exhaustive-deps
	}, [images, getImages])
	function getImages(offset=0, count=150) {
		setIsFetching(true)
		if (type === 'user') {
			getUserImages(name, offset, count)
		} else {
			getProfileImages(name, offset, count)
		}
	}
	function getProfileImages(profile, offset=0, count=150) {
		fetch(process.env.REACT_APP_API_URL+'igposts/?start='+offset+'&count='+count+'&profile='+profile)
	      .then(response => response.json())
	      .then(json => {
	      	if (json.length !== 0) {
	      		let tempImages = Object.assign({}, images)
	      		tempImages[typeName] = tempImages[typeName].concat(Array.prototype.slice.call(json))
	      		setImages(tempImages)
	      	}
	      })
	      .catch(error => console.log(error))
	}

	function getUserImages(profile, offset=0, count=150) {
		fetch(process.env.REACT_APP_API_URL+'igposts/?start='+offset+'&count='+count+'&user='+profile)
	      .then(response => response.json())
	      .then(json => {
	      	if (json.length !== 0) {
	      		let tempImages = Object.assign({}, images)
	      		tempImages[typeName] = tempImages[typeName].concat(Array.prototype.slice.call(json))
	      		setImages(tempImages)
	      	}
	      })
	      .catch(error => console.log(error))
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