import React, { useState, useEffect } from 'react'
import List from './EditableList.js'
import PickList from './picklist.js'
import './Admin.css';
import MenuBar from './MenuBar'
import useAuthContext from './useAuthContext';

function handleSave(getToken, users, categories, categoryMap) {
    fetch(
      process.env.REACT_APP_API_URL+'admin/users', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': 'Bearer '+getToken(),
        }, 
        body: JSON.stringify(users)
    })
    fetch(
      process.env.REACT_APP_API_URL+'admin/profiles', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': 'Bearer '+getToken()
        },
        body: JSON.stringify(categories)
    })
    fetch(
      process.env.REACT_APP_API_URL+'admin/ProfileUsers', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': 'Bearer '+getToken()
        }, 
        body: JSON.stringify(getCategoryArray(categoryMap))
    })
  }

function getCategoryArray(map) {
    var profiles = {}
    var test = {}
    for (var [key, value] of map) {
      test[key] = value;
    }
    profiles.profiles = test
    return profiles
  }

async function getCategoryMap(categories) {
  var localMap = new Map()
  return Promise.all(categories.map(async (value) => {
      await localMap.set(value, await fetch(process.env.REACT_APP_API_URL+'ProfileUsers/'+value)
      .then(response => response.json())
      .then(json => Array.prototype.slice.call(json)))
    })).then (empty => localMap)
}

function AdminPage() {
  const [users, setUsers] = useState([]);
  const [categories, setCategories] = useState([]);
  const [categoryMap, setCategoryMap] = useState(new Map())
  const {getToken} = useAuthContext();
  useEffect(() => {
    fetch(process.env.REACT_APP_API_URL+'users')
      .then(response => response.json())
      .then(json => {
        setUsers(json)
        return json
      })
      .then(users => {
        fetch(process.env.REACT_APP_API_URL+'profiles')
        .then(response => response.json())
        .then(json => {
          setCategories(json)
          return json
        })
        .then(categories => {
          getCategoryMap(categories).then(map => setCategoryMap(map))
        })
      })
  }, []);

    return (
      <React.Fragment>
        {
          <React.Fragment>
            <div className="container">
              <MenuBar />
              <div className="adminContainer">
                <div className="row justify-content-md-center">
                  <div className="col center">
                    <h2>User and Category Management</h2>
                    <ol>
                      <li>Add users you want to follow</li>
                      <li>Add categories to organise them</li>
                      <li>Assign users to categories so you can view them</li>
                    </ol>
                  </div>
                </div>
                <div className="row justify-content-md-center">
                  <div className="col">
                    <h2>Users</h2>
                    {List(users, setUsers, true)}
                  </div>
                  <div className="col">
                    <h2>Categories</h2>
                    {List(categories, setCategories, false)}
                  </div>
                  <div className="col ">
                    <h2>Assignment</h2>
                    <PickList 
                      users={users}
                      categories={categories}
                      categoryMap={categoryMap}
                      setCategoryMap={setCategoryMap}
                    />
                  </div>
                </div>
                <div className="row justify-content-md-center">
                  <div className="col center">
                    <button className="saveButton" onClick={() => handleSave(getToken, users, categories, categoryMap)}>Save</button>
                  </div>
                </div>
              </div>
            </div>
          </React.Fragment>
        }
      </React.Fragment>
    );
}

export default AdminPage