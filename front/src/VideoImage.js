import React from 'react'

const VideoImage = (props) => {
  return (
    <video controls>
      <source
        src={props.src} 
        type="video/mp4"
      />
    </video>
  )
}

export default VideoImage