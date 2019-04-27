import React from 'react'
import Container from 'react-bootstrap/Container'
import Row from 'react-bootstrap/Row'
import Col from 'react-bootstrap/Col'
import List from './EditableList.js'
import PickList from './picklist.js'
import Button from 'react-bootstrap/Button'
import './App.css';
import 'bootstrap/dist/css/bootstrap.min.css';
import '@fortawesome/fontawesome-free/css/all.min.css';

function postJSON(url, data) {
  var request = new XMLHttpRequest();
  request.open('POST', url, true);
  request.setRequestHeader('Content-Type', 'application/json; charset=UTF-8');
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
      categoryMap: new Map()
    };
    this.handleChangeUser = this.handleChangeUser.bind(this);
    this.handleRemoveUser = this.handleRemoveUser.bind(this);
    this.handleAddUsers = this.handleAddUsers.bind(this);
    this.handleChangeCategory = this.handleChangeCategory.bind(this);
    this.handleRemoveCategory = this.handleRemoveCategory.bind(this);
    this.handleAddCategories = this.handleAddCategories.bind(this);
    this.handleCategoryChange = this.handleCategoryChange.bind(this);
    this.handleSave = this.handleSave.bind(this);
  }

  handleChangeUser(event) {
    this.setState({newUser: event.target.value});
  }

  handleChangeCategory(event) {
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

  handleCategoryChange(event) {
    this.state.categoryMap.set(event.target.name, 
        Array.prototype.slice.call(event.target.options)
        .filter(option => option.selected)
        .map(option => option.value)
      )
    this.setState({categoryMap: this.state.categoryMap})
  }

  handleSave(event) {
    postJSON('/admin/users', JSON.stringify(this.state.users))
    postJSON('/admin/profiles', JSON.stringify(this.state.categories))
    postJSON('/admin/ProfileUsers', JSON.stringify(Array.from(this.state.categoryMap.entries())))
  }

  handleHistory(user) {
    var count = prompt("History")
    fetch('/admin/backfill/'+user+'/'+count)
  }

  render() {
    return (
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
            <Col sm="auto">
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
            <Col sm="auto">
              <h2>Categories</h2>
              <List 
                items={this.state.categories}
                newItem={this.state.newCategory}
                handleAdd={this.handleAddCategories}
                handleRemove={this.handleRemoveCategory}
                handleChange={this.handleChangeCategory}
              />
            </Col>
            <Col sm="auto">
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
    );
  }
}

export default App;
