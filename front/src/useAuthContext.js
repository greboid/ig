import {useState} from "react";
import { useSession } from  'react-use-session';
import createUseContext from "constate";

function useAuthState() {
  const { session, save, clear } = useSession('ig')
  var sessionAuthed = false
  if (session != null && session.token !== null) {
    sessionAuthed = true
  }
  const [authed, setAuthed] = useState(sessionAuthed);
  const [token, setToken] = useState("");
  const [expires, setExpires] = useState(0);
  const setLoggedIn = (token, expires) => { 
  	setAuthed(true)
  	setToken(token)
  	setExpires(expires)
    save({token: token})
    save({expires: expires})
  };
  const setLoggedOut = () => {
  	setAuthed(false)
  	setToken("")
  	setExpires(0)
    clear()
  };
  const getToken = () => { return token };
  const getExpires = () => { return expires };
  return { authed, setLoggedIn, setLoggedOut, getToken, getExpires };
}

const useAuthContext = createUseContext(useAuthState);

export default useAuthContext;