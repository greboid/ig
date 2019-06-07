import React, { useState, useEffect } from 'react'
import Container from 'react-bootstrap/Container'
import Row from 'react-bootstrap/Row'
import Col from 'react-bootstrap/Col'
import List from './EditableList.js'
import PickList from './picklist.js'
import Button from 'react-bootstrap/Button'
import './Admin.css';
import MenuBar from './MenuBar'
import 'bootstrap/dist/css/bootstrap.min.css';
import '@fortawesome/fontawesome-free/css/all.min.css';
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
            <Container fluid={true}>
              <MenuBar />
              <Row className="justify-content-md-center">
                <Col sm="auto">
                  <h2>User and Category Management</h2>
                  <ol>
                    <li>Add users you want to follow</li>
                    <li>Add categories to organise them</li>
                    <li>Assign users to categories so you can view them</li>
                  </ol>
                </Col>
              </Row>
              <Row className="justify-content-md-center">
                <Col sm="3">
                  <h2>Users</h2>
                  {List(users, setUsers, true)}
                </Col>
                <Col sm="3">
                  <h2>Categories</h2>
                  {List(categories, setCategories, false)}
                </Col>
                <Col sm="3">
                  <h2>Assignment</h2>
                  <PickList 
                    users={users}
                    categories={categories}
                    categoryMap={categoryMap}
                    setCategoryMap={setCategoryMap}
                  />
                </Col>
              </Row>
              <Row className="justify-content-md-center">
                <Col sm="auto">
                  <Button onClick={() => handleSave(getToken, users, categories, categoryMap)}>Save</Button>
                </Col>
              </Row>
            </Container>
          </React.Fragment>
        }
      </React.Fragment>
    );
}

export default AdminPage