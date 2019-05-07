import React from 'react';
import Select from 'react-select';

class PickList extends React.Component {
	render() {
		return (
	                  <ul className="list-group sorted"> 
				           {this.props.categories.map(function(category){
				              return (
				              	<li className="list-group-item" key={category}>
				              		<p>{category}</p>
				              		<Select
				              			options={this.props.users}
				              			closeMenuOnSelect={false}
				              			getOptionValue={(option) => (option)}
				              			getOptionLabel={(option) => (option)}
				              			name={category} 
				              			value={this.props.categoryMap.get(category)}
				              			isMulti
				              			onChange={this.props.onChange}
				              		/>
				              	</li>
				             	);
				            }, this)}
				        </ul>
		);
	}
}

export default PickList