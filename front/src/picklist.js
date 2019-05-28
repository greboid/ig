import React from 'react';
import Select from 'react-select';

export default function PickList(props) {
	return (
      <ul className="list-group sorted"> 
           {props.categories.map(function(category){
              return (
              	<li className="list-group-item" key={category}>
              		<p>{category}</p>
              		<Select
              			options={props.users}
              			closeMenuOnSelect={false}
              			getOptionValue={(option) => (option)}
              			getOptionLabel={(option) => (option)}
              			name={category} 
              			value={props.categoryMap.get(category)}
              			isMulti
              			onChange={props.onChange}
              		/>
              	</li>
             	);
            })}
        </ul>
	);
}