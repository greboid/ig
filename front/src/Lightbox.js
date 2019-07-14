import React, { useEffect } from 'react'
import './Lightbox.css'
import useWindowSize from '@rehooks/window-size'
import VideoImage from './VideoImage'
import { Link } from 'react-router-dom'

const Lightbox = ({src, caption, alt, source, shortcode, close, next, prev}) => {
	let windowSize = useWindowSize();
	function handleClick(event) {
		if (event.target.id === 'lightbox-image' 
			|| event.target.id === 'lightbox-caption' 
			|| event.target.tagName === 'A' ) { 
				return 
		}
		if (isFunction(close)) { close() }
	}
	
	function renderImage(src, alt) {
		if (src.match(".*mp4.*")) {
			return <VideoImage id="lightbox-image" src={src} />
		} else {
			return (
				<div className="outter">
					<div className="inner"/>
					<div id="lightbox-image" style={{backgroundImage: `url(${src})`}} />
					<div className="inner"/>
				</div>
			)
		}
	}
	useEffect(() => {
		const handleEscape = (event) => { 
			if (event.key === 'Escape') { 
				if (isFunction(close)) { close() } 
			}
			if (event.key === 'ArrowRight') {
				if (isFunction(next)) { next() } 
			}
			if (event.key === 'ArrowLeft') {
				if (isFunction(next)) { prev() } 
			}
		}
		window.addEventListener("keydown", handleEscape)
		return () => window.removeEventListener("keydown", handleEscape)
	}, [close, next, prev]);
	return(
		<React.Fragment>
			<div 
				id="lightbox"
				className={src === 'undefined' || src === "" ? 'lightbox-hidden' : ''} 
				onClick={handleClick}
				tabIndex={0}
			>
				<span id="lightbox-close" onClick={handleClick}>&times;</span>
				<div id="lightbox-content" style={{height: (0.85 * windowSize.innerHeight), width: (0.85 * windowSize.innerWidth)}}>
					{renderImage(src, alt)}
					<p id="lightbox-caption"><Link to={`/user/${source}`}>{source}</Link> - <a href={`https://instagram.com/p/${shortcode}`}>{shortcode}</a><br />{caption}</p>
				</div>
			</div>
		</React.Fragment>
	)
}

function isFunction(functionToCheck) {
 return functionToCheck && {}.toString.call(functionToCheck) === '[object Function]';
}

export default Lightbox
