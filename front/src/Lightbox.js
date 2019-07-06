import React, { useEffect } from 'react'
import './Lightbox.css'
import useWindowSize from '@rehooks/window-size'
import VideoImage from './VideoImage'

const Lightbox = ({src, caption, alt, close}) => {
	let windowSize = useWindowSize();
	function handleClick(event) {
		if (event.target.id === 'lightbox-image' || event.target.id === 'lightbox-caption') { return }
		close()
	}
	
	function renderImage(src, alt) {
		if (src.match(".*mp4.*")) {
			return <VideoImage id="lightbox-image" src={src} />
		} else {
			return <img id="lightbox-image" src={src} alt={alt} />
		}
	}
	useEffect(() => {
		const handleEscape = (event) => { if (event.key === 'Escape') { close() } }
		window.addEventListener("keydown", handleEscape)
		return () => window.removeEventListener("keydown", handleEscape)
	}, [close]);
	return(
		<React.Fragment>
			<div 
				id="lightbox"
				className={src === 'undefined' || src === "" ? 'lightbox-hidden' : ''} 
				onClick={handleClick}
				tabIndex={0}
			>
				<span id="lightbox-close" onClick={handleClick}>&times;</span>
				<div id="lightbox-content" style={{height: (0.85 * windowSize.innerHeight)}}>
					{renderImage(src, alt)}
					<p id="lightbox-caption">{caption}</p>
				</div>
			</div>
		</React.Fragment>
	)
}
export default Lightbox
