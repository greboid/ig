import React, { useState } from 'react'
import './Lightbox.css'
import useWindowSize from '@rehooks/window-size';

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
	return(
		<React.Fragment>
			<div 
				id="lightbox"
				className={props.src === 'undefined' || props.src === "" ? 'lightbox-hidden' : ''} 
				onClick={handleClick}
				onKeyDown={handleKeys}
				tabIndex={0}
			>
				<span 
					className="lightbox-close"
				>
					&times;
				</span>
					<div id="lightbox-content" style={{height: (0.85 * windowSize.innerHeight)}}>
						<img id="lightbox-image" src={props.src} alt={props.alt} />
						<p id="lightbox-caption">{props.caption}</p>
					</div>
				</div>
		</React.Fragment>
	)
}
export default Lightbox