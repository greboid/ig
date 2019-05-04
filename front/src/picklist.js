import React from 'react';

class PickList extends React.Component {
	render() {
		return (
	                  <ul className="list-group sorted"> 
				           {this.props.categories.map(function(category){
				              return (
				              	<li className="list-group-item" key={category}>
				              		<p>{category}</p>
				              		<select 
				              			onChange={this.props.onChange}
				              			name={category} 
				              			value={this.props.categoryMap.get(category)} 
				              			multiple
				              		>
				              			{this.props.users.map(function(user){
				              				return (
				              					<option 
				              						key={user} 
				              					>
				              						{user}
				              					</option>
				              				);
				              			}, this)}
				              		</select>
				              	</li>
				             	);
				            }, this)}
				        </ul>
		);
	}
}

export default PickList