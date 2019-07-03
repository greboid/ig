import {useState} from "react";
import { useSession } from  'react-use-session';
import createUseContext from "constate";

function useAuthState() {
  const { session, save, clear } = useSession('ig')
  var sessionAuthed = false
  var sessionToken = ""
  var sessionExpires = 0
  var refreshCheckTimeout
  if (session != null && session.token !== null) {
    sessionAuthed = true
    sessionToken = session.token
    sessionExpires = session.expires
  }
  if (new Date(sessionExpires * 1000) < new Date()) {
    sessionAuthed = false
    sessionToken = ""
    sessionExpires = 0
  }
  const [authed, setAuthed] = useState(sessionAuthed);
  const [token, setToken] = useState(sessionToken);
  const [expires, setExpires] = useState(sessionExpires);
  if (sessionAuthed) {
    refreshCheckTimeout = backgroundCheck(expires, token, setToken, setExpires)
  }
  const setLoggedIn = (token, expires) => { 
  	setAuthed(true)
  	setToken(token)
  	setExpires(expires)
    save({token: token, expires: expires})
    if (refreshCheckTimeout === null) {
      refreshCheckTimeout = backgroundCheck(expires, token, setToken, setExpires)
    }
  };
  const setLoggedOut = () => {
  	setAuthed(false)
  	setToken("")
  	setExpires(0)
    clear()
    clearTimeout(refreshCheckTimeout)
  };
  const getToken = () => { return token }
  const getExpires = () => { return expires }
  return { authed, setLoggedIn, setLoggedOut, getToken, getExpires }
}

function backgroundCheck(expires, token, setToken, setExpires) {
  var next = expires * 1000 - Date.now() - (10 * 60 * 1000)
  if (next > 0) {
    return setTimeout(function(){ 
      renewToken(token, setToken, setExpires)
    }, next);
  }
}

function renewToken(token, setToken, setExpires) {
  fetch(process.env.REACT_APP_API_URL+'refreshtoken', {
    method: 'GET',
    headers: {
      'Content-Type': 'application/json',
      'Authorization': 'Bearer '+token
    }
  })
  .then(function(response) {
    return response.json();
  })
  .then(function(response) {
    if (response.hasOwnProperty("token") && response.hasOwnProperty("expires")) {
      setToken(response.token)
      setExpires(response.expires)
    } else {
      console.log(`Error: ${response.message}`)
    }
  })
}

const useAuthContext = createUseContext(useAuthState);

export default useAuthContext;