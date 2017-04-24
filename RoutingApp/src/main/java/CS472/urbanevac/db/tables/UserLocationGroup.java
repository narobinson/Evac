package CS472.urbanevac.db.tables;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

@Entity
@Table(name = "[dbo].[UserLocationGroup]")
@NamedQueries({
	@NamedQuery(
		name = "getAllUserLocationGroups",
		query = "SELECT g FROM UserLocationGroup g"
	),
	@NamedQuery(
		name = "getUserLocationGroupById",
		query = "SELECT u FROM UserLocationGroup u WHERE u.id = :id"
	),
	@NamedQuery(
		name = "getUserLocationGroupByLatLon",
		query = "SELECT u FROM UserLocationGroup u WHERE u.lat = :lat AND u.lon = :lon"
	)
})
public class UserLocationGroup {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private long id;
	
	@Column(name = "lat")
	private double lat;
	
	@Column(name = "lon")
	private double lon;
	
	@Column(name = "count")
	private int count;

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
	 * Increments the user count.
	 * 
	 * @return The new count
	 */
	public int incrementCount() {
		return ++this.count;
	}
	
	/**
	 * Decrements the user count.
	 * 
	 * @return The new count
	 */
	public int decrementCount() {
		return --this.count;
	}
	
	/**
	 * @return the count
	 */
	public int getCount() {
		return count;
	}

	/**
	 * @param count the count to set
	 */
	public void setCount(int count) {
		this.count = count;
	}
	
	@Override
	public String toString() {
		String ret = String.format(
				"UserLocationGroup[id='%d', lat='%f', lon='%f', count='%d']", 
				this.id, this.lat, this.lat, this.count);
		
		return ret;
	}
}
