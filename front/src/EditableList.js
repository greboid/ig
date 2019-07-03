import React, { useState } from 'react';
import { FaHistory, FaTrashAlt } from 'react-icons/fa';

function handleChange(event, setNewItem) {
  setNewItem(event.target.value)
}

function handleAdd(event, newItem, items, setItems, setNewItem) {
  event.preventDefault()
  var newItems = items.slice()
  newItems.push(newItem)
  setItems(newItems)
  setNewItem("")
}

function onHistory(user) {
  var count = prompt("History")
    fetch(process.env.REACT_APP_API_URL+'admin/backfill/'+user+'/'+count)
}

function onRemove(value, items, setItems) {
  setItems(items.filter(item => item !== value))
}

function List(items, setItems, showHistory) {
  return (
    <React.Fragment>
      {InputField(items, setItems)}
      <ul className="list-group sorted"> 
          {items.map(function(value){
             return <li className="list-group-item" key={value}>{ListItem(value, showHistory, () => onRemove(value, items, setItems))}</li>;
           }, this)} 
      </ul>
    </React.Fragment>
  );
}

function ListItem(item, showHistory, onRemove) {
  return (
      <React.Fragment>
        <span className="list-item-value">{item}</span>
        {showHistory &&
          <FaHistory className="list-item-history" onClick={() => onHistory(item)} />
        }
        <FaTrashAlt className="list-item-remove" onClick={() => onRemove(item)} />
      </React.Fragment>
  );
}

function InputField(items, setItems) {
  const [newItem, setNewItem] = useState("");
  return (
    <form onSubmit={(event) => handleAdd(event, newItem, items, setItems, setNewItem)} className="form-inline">
      <div className="form-group">
        <input className="form-control" type="text" value={newItem} onChange={(event) => handleChange(event, setNewItem)} />
        <input className="btn btn-light" type="submit" value="Add" />
      </div>
    </form>
  );
}

export default List