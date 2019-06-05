import React from 'react';
import FontAwesome from 'react-fontawesome';

class List extends React.Component {
  constructor(props) {
    super(props);
    this.state = {
      newItem: ''
    }
    this.handleChange = this.handleChange.bind(this);
    this.handleRemove = this.handleRemove.bind(this);
    this.handleAdd = this.handleAdd.bind(this);
  }

  handleChange(event) {
    this.setState({newItem: event.target.value});
  }

  handleRemove(value) {
    this.props.setItems(this.props.items.filter(user => user !== value))
  }

  handleAdd(value) {
    value.preventDefault()
    var newItems = this.props.items.slice()
    newItems.push(this.state.newItem)
    this.props.setItems(newItems)
    this.setState({newItem: ""});
  }

  renderInputField() {
    return (
      <InputField
        value={this.state.newItem}
        handleSubmit={ this.handleAdd }
        handleChange={ this.handleChange }
      />
    );
  }

  renderListItem(value, showHistory = false) {
    return (
        <ListItem 
          value={value} 
          onRemove={() => this.handleRemove(value)}
          onHistory={() => this.props.handleHistory(value)}
          showHistory={showHistory}
        />
    );
  }

  render() {
    return (
      <React.Fragment>
        {this.renderInputField()}
        <ul className="list-group sorted"> 
           {this.props.items.map(function(value){
              return <li className="list-group-item" key={value}>{this.renderListItem(value, this.props.showHistory)}</li>;
            }, this)}
        </ul>
      </React.Fragment>
    );
  }
}

function ListItem(props) {
  return (
      <React.Fragment>
        <span className="list-item-value">{props.value}</span>
        {props.showHistory &&
          <FontAwesome className="list-item-history" name="history" onClick={() => props.onHistory(props.value)} />
        }
        <FontAwesome className="list-item-remove" name="trash-alt" onClick={() => props.onRemove(props.value)} />
      </React.Fragment>
  );
}

function InputField(props) {
  return (
    <form onSubmit={props.handleSubmit} className="form-inline">
      <div className="form-group">
        <input className="form-control" type="text" value={props.value} onChange={props.handleChange} />
        <input className="btn btn-light" type="submit" value="Add" />
      </div>
    </form>
  );
}

export default List