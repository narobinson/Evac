package CS472.urbanevac.db.tables;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import CS472.urbanevac.db.Database;

@Entity
@Table(name = "[dbo].[UserRoute]")
public class UserRoute {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private long id;
	
	@Column(name = "route")
	private String route;

	@Column(name = "lastVisitedNode")
	private Long lastVisitedNode;
	
	/**
	 * @return the id
	 */
	public long getId() {
		return id;
	}

	/**
	 * @param id the id to set
	 */
	public void setId(long id) {
		this.id = id;
	}

	/**
	 * @return the route
	 */
	public String getRoute() {
		return route;
	}

	/**
	 * @param route the route to set
	 */
	public void setRoute(String route) {
		this.route = route;
	}

	/**
	 * @return the lastVisitedNode
	 */
	public Node getLastVisitedNode() {
		return this.lastVisitedNode == null ? null : Database.INSTANCE.getNodeById(this.lastVisitedNode);
	}
	
	/**
	 * @param lastVisitedNode the lastVisitedNode to set
	 */
	public void setLastVisitedNode(Node lastVisitedNode) {
		this.lastVisitedNode = lastVisitedNode.getId();
	}

	@Override
	public String toString() {
		String ret = String.format(
				"UserRoute[id='%d', route='%s', lastVisitedNode='%s']", 
				this.id, this.route, this.lastVisitedNode);
		
		return ret;
	}
}
