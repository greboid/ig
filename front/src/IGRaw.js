import React, { useState, useEffect } from 'react'
import MenuBar from "./MenuBar";

function IGRaw() {

    const [users, setUsers] = useState([]);

    useEffect(() => {
        fetch(process.env.REACT_APP_API_URL+'users')
            .then(response => {
                return response.json()
            })
            .then(json => {
                setUsers(json)
                return json
            })
    }, []);

    return (
        <React.Fragment>
            <MenuBar />
            <div id="app" className="contentContainer">
                <ul>
                {users.map((user, i) => {
                    return (
                        <li><a href="https://instagram.com/{user}">{user}</a></li>
                    )
                })}
                </ul>
            </div>
        </React.Fragment>
    );
}

export default IGRaw
