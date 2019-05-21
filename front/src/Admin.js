import React from 'react'
import Container from 'react-bootstrap/Container'
import Row from 'react-bootstrap/Row'
import Col from 'react-bootstrap/Col'
import List from './EditableList.js'
import PickList from './picklist.js'
import Button from 'react-bootstrap/Button'
import './Admin.css';
import LoginForm from './LoginForm';
import MenuBar from './MenuBar'
import 'bootstrap/dist/css/bootstrap.min.css';
import '@fortawesome/fontawesome-free/css/all.min.css';

class Admin extends React.Component {
  constructor(props) {
    super(props);
    this.state = {
      users: [],
      newUser: '',
      categories: [],
      newCategory: '',
      categoryMap: new Map()
    };
    this.state.authToken = sessionStorage.getItem('authToken') ? sessionStorage.getItem('authToken') : ""
    this.state.authExpires = sessionStorage.getItem('authExpires') ? sessionStorage.getItem('authExpires') : ""
    this.handleChangeUser = this.handleChangeUser.bind(this);
    this.handleRemoveUser = this.handleRemoveUser.bind(this);
    this.handleAddUsers = this.handleAddUsers.bind(this);
    this.handleChangeCategory = this.handleChangeCategory.bind(this);
    this.handleRemoveCategory = this.handleRemoveCategory.bind(this);
    this.handleAddCategories = this.handleAddCategories.bind(this);
    this.handleCategoryChange = this.handleCategoryChange.bind(this);
    this.handleSave = this.handleSave.bind(this);
    this.componentDidMount = this.componentDidMount.bind(this);
    this.loadCategoryMap = this.loadCategoryMap.bind(this);
    this.handleLogout = this.handleLogout.bind(this);
    this.setAuthInfo = this.setAuthInfo.bind(this);
  }

  componentDidMount() {
    fetch(this.props.apiURL+'/users')
      .then(response => response.json())
      .then(json => {
        this.setState({users: json})
      })
      .then(this.loadCategories())
  }

  loadCategories() {
    fetch(this.props.apiURL+'/profiles')
      .then(response => response.json())
      .then(json => {
        this.setState({categories: json})
        return json
      })
      .then(json => this.loadCategoryMap(json))
  }

  loadCategoryMap(categories) {
    var categoryMap = new Map(this.state.categoryMap)
    categories.forEach(function(value){
      fetch(this.props.apiURL+'/ProfileUsers/'+value)
      .then(response => response.json())
      .then(json => {
        categoryMap.set(value, Array.prototype.slice.call(json))
      })
      .then(empty => {
        this.setState({categoryMap: categoryMap})
        })
    }, this)
  }

  handleChangeUser(event) {
    this.setState({newUser: event.target.value});
  }

  handleChangeCategory(event, type) {
    this.setState({newCategory: event.target.value});
  }

  handleRemoveUser(value) {
    this.setState({users: this.state.users.filter(user => user !== value)});
  }

  handleRemoveCategory(value) {
    this.state.categoryMap.delete(value)
    this.setState({
      categories: this.state.categories.filter(category => category !== value),
      categoryMap: this.state.categoryMap
    });
  }

  handleAddUsers(event) {
    if (this.state.newUser !== "" && !this.state.users.includes(this.state.newUser)) {
      var newUsers = this.state.users.slice()
      newUsers.push(this.state.newUser)
      this.setState({users: newUsers, newUser: ''})
    }
    event.preventDefault()
  }

  handleAddCategories(event) {
    if (this.state.newCategory !== "" && !this.state.categories.includes(this.state.newCategory)) {
      var newCategories = this.state.categories.slice()
      newCategories.push(this.state.newCategory)
      this.state.categoryMap.set(this.state.newCategory, [])
      this.setState({categories: newCategories, newCategory: ''})
    }
    event.preventDefault()
  }

  handleCategoryChange(selected, type) {
    this.state.categoryMap.set(type.name, 
        Array.prototype.slice.call(selected)
      )
    this.setState({categoryMap: this.state.categoryMap})
  }

  handleSave(event) {
    fetch(
      this.props.apiURL+'/admin/users', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': 'Bearer '+this.state.authToken,
        }, 
        body: JSON.stringify(this.state.users)
    })
    fetch(
      this.props.apiURL+'/admin/profiles', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': 'Bearer '+this.state.authToken
        },
        body: JSON.stringify(this.state.categories)
    })
    fetch(
      this.props.apiURL+'/admin/ProfileUsers', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': 'Bearer '+this.state.authToken
        }, 
        body: JSON.stringify(this.getCategoryArray(this.state.categoryMap))
    })
  }

  getCategoryArray(map) {
    var profiles = {}
    var test = {}
    for (var [key, value] of map) {
      test[key] = value;
    }
    profiles.profiles = test
    return profiles
  }

  handleHistory(user) {
    var count = prompt("History")
    fetch(this.props.apiURL+'/admin/backfill/'+user+'/'+count)
  }

  handleLogout(event) {
    event.preventDefault()
    sessionStorage.setItem('authToken', "")
    sessionStorage.setItem('authExpires', "")
    this.setState({
      authToken: "",
      authExpires: ""
    });
  }

  setAuthInfo(token = "", expires = "") {
    sessionStorage.setItem('authToken', token)
    sessionStorage.setItem('authExpires', expires)
    this.setState({
      authToken: token,
      authExpires: expires
    });
  }

  render() {
    return (
      <React.Fragment>
        {
          this.state.authToken.length ? (
            <React.Fragment>
              <Container fluid={true}>
                <MenuBar 
                  authToken={this.state.authToken}
                  handleLogout={this.handleLogout}
                />
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
                    <List 
                      items={this.state.users}
                      newItem={this.state.newUser}
                      showHistory = {true}
                      handleAdd={this.handleAddUsers}
                      handleRemove={this.handleRemoveUser}
                      handleChange={this.handleChangeUser}
                      handleHistory={this.handleHistory}
                    />
                  </Col>
                  <Col sm="3">
                    <h2>Categories</h2>
                    <List 
                      items={this.state.categories}
                      newItem={this.state.newCategory}
                      handleAdd={this.handleAddCategories}
                      handleRemove={this.handleRemoveCategory}
                      handleChange={this.handleChangeCategory}
                    />
                  </Col>
                  <Col sm="3">
                    <h2>Assignment</h2>
                    <PickList 
                      onChange={this.handleCategoryChange}
                      users={this.state.users}
                      categories={this.state.categories}
                      categoryMap={this.state.categoryMap}
                    />
                  </Col>
                </Row>
                <Row className="justify-content-md-center">
                  <Col sm="auto">
                    <Button onClick={this.handleSave}>Save</Button>
                  </Col>
                </Row>
              </Container>
            </React.Fragment>
          ) : (
                  <LoginForm 
                    setAuthInfo={this.setAuthInfo}
                    apiURL={this.props.apiURL}
                  />
          )
        }
      </React.Fragment>
    );
  }
}

export default Admin;
