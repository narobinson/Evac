package CS472.urbanevac.db.tables;

import java.util.List;
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

import CS472.urbanevac.db.Database;

@Entity
@Table(name = "[dbo].[User]")
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
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private long id;
	
	@Column(name = "lat")
	private double lat;
	
	@Column(name = "lon")
	private double lon;
	
	@Column(name = "uid")
	private UUID uid;

	@ManyToOne
	@JoinColumn(name = "userGroup", referencedColumnName = "id")
	//@Cascade({CascadeType.SAVE_UPDATE})
	private UserLocationGroup userLocationGroup;

	@OneToOne
	@JoinColumn(name = "id")
	//@Cascade({CascadeType.SAVE_UPDATE})
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
	public UserRoute getRoute() {
		return route;
	}

	/**
	 * @param route the route to set
	 */
	public void setRoute(UserRoute route) {
		this.route = route;
	}
	
	public Node getLocation() {
		Node location = new Node();
		List<Node> allNodes = Database.INSTANCE.getAllNodes();
		
		Node closestNode = null;
		Double distance = 0.0;
		for (Node node : allNodes) {
			if (closestNode == null) {
				closestNode = node;
				distance = calculateDistance(this.getLat(), this.getLon(), closestNode.getLatitude(), closestNode.getLongitude());
			} else {
				Double tempDistance = calculateDistance(this.getLat(), this.getLon(), node.getLatitude(), node.getLongitude());
				if ( tempDistance < distance){
					closestNode = node;
					distance = tempDistance;
				}
			}
		}
		
		return location;
	}
	
	private Double calculateDistance(Double lat1, Double lon1, Double lat2, Double lon2) {
		Double distance = 0.0;
		int radius = 6371; //Radius of Earth in Km
		Double dlat = degToRad(lat2 - lat1);
		Double dlon = degToRad(lon2 - lon1);
		
		Double a = Math.sin(dlat/2) * Math.sin(dlat/2) +
				Math.cos(degToRad(lat1)) * Math.cos(degToRad(lat2)) *
				Math.sin(dlon/2) * Math.sin(dlon/2);
		
		Double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
		distance = radius * c;
		
		return distance;
	}
	
	private Double degToRad(Double deg) {
		return deg * (Math.PI/180);
	}

	@Override
	public String toString() {
		String ret = String.format(
				"User[id='%d', lat='%f', lon='%f', uid='%s', userGroup='%s', route='%s']", 
				this.id, this.lat, this.lon, this.uid.toString(), this.userLocationGroup, this.route);
		
		return ret;
	}
}
