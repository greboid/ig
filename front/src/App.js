import React from 'react'
import Container from 'react-bootstrap/Container'
import Row from 'react-bootstrap/Row'
import Col from 'react-bootstrap/Col'
import List from './EditableList.js'
import PickList from './picklist.js'
import Button from 'react-bootstrap/Button'
import './App.css';
import LoginForm from './LoginForm';
import 'bootstrap/dist/css/bootstrap.min.css';
import '@fortawesome/fontawesome-free/css/all.min.css';

const BASEURL = ''

function postJSON(url, data, response = function(){}) {
  var request = new XMLHttpRequest();
  request.open('POST', url, true);
  request.setRequestHeader('Content-Type', 'application/json; charset=UTF-8');
  request.onreadystatechange = response
  request.send(data);
}

class App extends React.Component {
  constructor(props) {
    super(props);
    this.state = {
      users: [],
      newUser: '',
      categories: [],
      newCategory: '',
      categoryMap: new Map(),
      authToken: "",
      authExpires: "",
      loginUsername: "",
      loginPassword: ""
    };
    this.handleChangeUser = this.handleChangeUser.bind(this);
    this.handleRemoveUser = this.handleRemoveUser.bind(this);
    this.handleAddUsers = this.handleAddUsers.bind(this);
    this.handleChangeCategory = this.handleChangeCategory.bind(this);
    this.handleRemoveCategory = this.handleRemoveCategory.bind(this);
    this.handleAddCategories = this.handleAddCategories.bind(this);
    this.handleCategoryChange = this.handleCategoryChange.bind(this);
    this.handleUsernameChange = this.handleUsernameChange.bind(this);
    this.handlePasswordChange = this.handlePasswordChange.bind(this);
    this.handleLoginSubmit = this.handleLoginSubmit.bind(this);
    this.handleSave = this.handleSave.bind(this);
    this.componentDidMount = this.componentDidMount.bind(this);
    this.loadCategoryMap = this.loadCategoryMap.bind(this);
  }

  componentDidMount() {
    fetch(BASEURL+'/users')
      .then(response => response.json())
      .then(json => {
        this.setState({users: json})
      })
      .then(this.loadCategories())
  }

  loadCategories() {
    fetch(BASEURL+'/profiles')
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
      fetch(BASEURL+'/ProfileUsers/'+value)
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
    this.setState({categories: this.state.categories.filter(category => category !== value)});
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
      BASEURL+'/admin/users', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': 'Bearer '+this.state.authToken,
        }, 
        body: JSON.stringify(this.state.users)
    })
    fetch(
      BASEURL+'/admin/profiles', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': 'Bearer '+this.state.authToken
        },
        body: JSON.stringify(this.state.users)
    })
    fetch(
      BASEURL+'/admin/ProfileUsers', {
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
    fetch(BASEURL+'/admin/backfill/'+user+'/'+count)
  }

  handleUsernameChange(event) {
    this.setState({loginUsername: event.target.value});
  }

  handlePasswordChange(event) {
    this.setState({loginPassword: event.target.value});
  }

  handleLoginSubmit(event) {
    event.preventDefault()
    var app = this
    postJSON(
      BASEURL+'/login', 
      JSON.stringify({ user: this.state.loginUsername, password: this.state.loginPassword}),
      function() {
        if (this.readyState === 4) {
          var response = JSON.parse(this.responseText)
          if (response.hasOwnProperty("token") && response.hasOwnProperty("expires")) {
            app.setState({
              loginPassword: "", 
              loginUsername: "",
              authToken: response.token,
              authExpires: response.expires
            });
          } else {
            app.setState({loginPassword: "", loginUsername: ""});
          }
        }
      }
    )
  }

  render() {
    return (
      <React.Fragment>
        {
          this.state.authToken.length ? (
            <React.Fragment>
              <Container fluid={true}>
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
                    username={this.state.loginUsername}
                    password={this.state.loginPassword}
                    handleUsernameChange={this.handleUsernameChange}
                    handlePasswordChange={this.handlePasswordChange}
                    handleSubmit={this.handleLoginSubmit}
                  />
          )
        }
      </React.Fragment>
    );
  }
}

export default App;
