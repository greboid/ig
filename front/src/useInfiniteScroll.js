import { useState, useEffect } from 'react';
import { useDebouncedCallback } from 'use-debounce';

const useInfiniteScroll = (callback) => {
  const [isFetching, setIsFetching] = useState(false);
  const [handleScroll] = useDebouncedCallback(
    (event) => {
      if (window.innerHeight + document.documentElement.scrollTop !== document.documentElement.scrollHeight || isFetching) return;
      setIsFetching(true);
    }, 100
  );

  useEffect(() => {
    window.addEventListener('scroll', handleScroll);
    return () => window.removeEventListener('scroll', handleScroll);
  }, [handleScroll]);

  useEffect(() => {
    if (!isFetching) return;
    callback();
  }, [isFetching, callback]);

  return [isFetching, setIsFetching];
};

export default useInfiniteScroll