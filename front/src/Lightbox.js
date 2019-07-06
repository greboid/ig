import React, { useState } from 'react'
import './Lightbox.css'
import useWindowSize from '@rehooks/window-size'
import VideoImage from './VideoImage'

const Lightbox = (props) => {
	const [closed, setClosed] = useState(false)
	let windowSize = useWindowSize();
	function handleClick(event) {
		if (event.target === event.currentTarget) { 
			props.close()
		}
	}
	function handleKeys(event) {
		if (event.key = 'Escape') {
			props.close()
		}
	}
	function renderImage(props) {
		if (props.src.match(".*mp4.*")) {
			return <VideoImage id="lightbox-image" src={props.src} />
		} else {
			return <img id="lightbox-image" src={props.src} alt={props.alt} />
		}
	}
	return(
		<React.Fragment>
			<div 
				id="lightbox"
				className={props.src === 'undefined' || props.src === "" ? 'lightbox-hidden' : ''} 
				onClick={handleClick}
				onKeyDown={handleKeys}
				tabIndex={0}
			>
				<span id="lightbox-close" onClick={handleClick}>&times;</span>
				<div id="lightbox-content" style={{height: (0.85 * windowSize.innerHeight)}}>
					{renderImage(props)}
					<p id="lightbox-caption">{props.caption}</p>
				</div>
			</div>
		</React.Fragment>
	)
}
export default Lightbox