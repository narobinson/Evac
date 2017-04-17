package CS472.urbanevac.db.tables;

import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;

@Entity
@Table(name = "[dbo].[User]")
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
	@Cascade({CascadeType.SAVE_UPDATE})
	private UserLocationGroup userGroup;

	@OneToOne
	@JoinColumn(name = "id")
	@Cascade({CascadeType.SAVE_UPDATE})
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
		return userGroup;
	}

	/**
	 * @param userGroup the userGroup to set
	 */
	public void setUserLocationGroup(UserLocationGroup userGroup) {
		this.userGroup = userGroup;
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

	@Override
	public String toString() {
		String ret = String.format(
				"User[id='%d', lat='%f', lon='%f', uid='%s', userGroup='%s', route='%s']", 
				this.id, this.lat, this.lon, this.uid.toString(), this.userGroup, this.route);
		
		return ret;
	}
}
