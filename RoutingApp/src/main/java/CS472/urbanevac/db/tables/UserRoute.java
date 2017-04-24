package CS472.urbanevac.db.tables;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonIgnore;

import CS472.urbanevac.controllers.Route;

@Entity
@Table(name = "app_user_routes")
@NamedQueries({
	@NamedQuery(
		name = "getAllUserRoutes",
		query = "SELECT r FROM UserRoute r"
	),
	@NamedQuery(
		name = "getUserRouteById",
		query = "SELECT u FROM UserRoute u WHERE u.id = :id"
	)
})
public class UserRoute {
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "id")
	private long id;
	
	@Column(name = "route")
	private String route;

	@ManyToOne
	@JoinColumn(name = "last_visited_node", referencedColumnName = "id")
	private Node lastVisitedNode;
	
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
	@JsonIgnore
	public String getRawRoute() {
		return route;
	}

	/**
	 * @param route the route to set
	 */
	public void setRawRoute(String route) {
		this.route = route;
	}
	
	public List<InternalNode> getRoute() {
		if (this.route == null) {
			return null;
		}
		
		return Arrays.asList(this.route.split(Pattern.quote("$"))).parallelStream().filter(s -> s.length() != 0).map(s -> Route.NODES.get(Long.parseLong(s))).filter(Objects::nonNull).collect(Collectors.toList());
	}

	/**
	 * @return the lastVisitedNode
	 */
	public InternalNode getLastVisitedNode() {
		return this.lastVisitedNode == null ? null : Route.NODES.get(this.lastVisitedNode.getId());
	}
	
	/**
	 * @param lastVisitedNode the lastVisitedNode to set
	 */
	public void setLastVisitedNode(Node lastVisitedNode) {
		this.lastVisitedNode = lastVisitedNode;
	}

	@Override
	public String toString() {
		String ret = String.format(
				"UserRoute[id='%d', rawRoute='%s' route='%s', lastVisitedNode='%s']", 
				this.id, this.route, this.getRoute(), this.lastVisitedNode);
		
		return ret;
	}
}
