import React from 'react'

const VideoImage = ({id, src}) => {
  return (
    <video key={src} controls>
      <source
        src={src} 
        type="video/mp4"
      />
    </video>
  )
}

export default VideoImage