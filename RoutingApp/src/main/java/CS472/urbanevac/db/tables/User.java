package CS472.urbanevac.db.tables;

import java.util.Collection;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.jboss.logging.Logger;

import com.fasterxml.jackson.annotation.JsonIgnore;

import CS472.urbanevac.controllers.Route;
import CS472.urbanevac.controllers.Util;

@Entity
@Table(name = "app_users")
@NamedQueries({
	@NamedQuery(
		name = "getAllUsers",
		query = "SELECT u FROM User u"
	),
	@NamedQuery(
		name = "getUserById",
		query = "SELECT u FROM User u WHERE u.id = :id"
	),
	@NamedQuery(
		name = "getUserByUUID",
		query = "SELECT u FROM User u WHERE u.uid = :uid"
	)
})
public class User {
	@Transient
	private static Logger logger = Logger.getLogger(User.class);
	
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "id")
	private long id;
	
	@Column(name = "lat")
	private double lat;
	
	@Column(name = "lon")
	private double lon;
	
	@Column(name = "uid")
	private UUID uid;

	@ManyToOne
	@JoinColumn(name = "user_group", referencedColumnName = "id")
	private UserLocationGroup userLocationGroup;

	@OneToOne
	@JoinColumn(name = "route", referencedColumnName = "id")
	private UserRoute route;
	
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
	 * @return the lat
	 */
	public double getLat() {
		return lat;
	}

	/**
	 * @param lat the lat to set
	 */
	public void setLat(double lat) {
		this.lat = lat;
	}

	/**
	 * @return the lon
	 */
	public double getLon() {
		return lon;
	}

	/**
	 * @param lon the lon to set
	 */
	public void setLon(double lon) {
		this.lon = lon;
	}

	/**
	 * @return the uid
	 */
	public UUID getUid() {
		return uid;
	}

	/**
	 * @param uid the uid to set
	 */
	public void setUid(UUID uid) {
		this.uid = uid;
	}

	/**
	 * @return the userGroup
	 */
	public UserLocationGroup getUserLocationGroup() {
		return userLocationGroup;
	}

	/**
	 * @param userGroup the userGroup to set
	 */
	public void setUserLocationGroup(UserLocationGroup userGroup) {
		this.userLocationGroup = userGroup;
	}
	
	/**
	 * @return the route
	 */
	@JsonIgnore
	public UserRoute getRoute() {
		return route;
	}

	/**
	 * @param route the route to set
	 */
	public void setRoute(UserRoute route) {
		this.route = route;
	}
	
	@JsonIgnore
	public InternalNode getLocation() {
		Collection<InternalNode> allNodes = Route.NODES.values();
		
		InternalNode closestNode = null;
		double distance = 0.0;
		
		for (InternalNode node : allNodes) {
			if (closestNode == null) {
				closestNode = node;
				distance = Util.calculateDistance(this.getLat(), this.getLon(), closestNode.getLatitude(), closestNode.getLongitude());
			} else {
				double tempDistance = Util.calculateDistance(this.getLat(), this.getLon(), node.getLatitude(), node.getLongitude());
				if (tempDistance < distance) {
					closestNode = node;
					distance = tempDistance;
				}
			}
		}
		
		return closestNode;
	}

	@Override
	public String toString() {
		String ret = String.format(
				"User[id='%d', lat='%f', lon='%f', uid='%s', userGroup='%s', route='%s']", 
				this.id, this.lat, this.lon, this.uid.toString(), this.userLocationGroup, this.route == null);
		
		return ret;
	}
}
