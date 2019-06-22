import React from 'react';

const VideoImage = (source) => (
  <video controls style={{
      maxWidth: '90%',
      maxHeight: '90%',
      position: 'absolute',
      width: '90%',
      height: '90%',
      left: 0,
      right: 0,
      margin: 'auto',
      top: '50%',
      transform: 'translateY(-50%)',
    }}>
    <source 
    src={source} 
    type="video/mp4"
    />
  </video>
);

export default VideoImage;