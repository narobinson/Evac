package CS472.urbanevac.db.tables;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import org.hibernate.annotations.TypeDefs;

import CS472.urbanevac.db.Database;
import CS472.urbanevac.db.types.HstoreUserType;
import CS472.urbanevac.db.types.LongArrayUserType;

@Entity
@Table(name = "ways")
@TypeDefs({
	@TypeDef(name = "hstore", typeClass = HstoreUserType.class),
	@TypeDef(name = "longArray", typeClass = LongArrayUserType.class)
})
@NamedQueries({
	@NamedQuery(
		name = "getAllWays",
		query = "SELECT w FROM Way w"
	),
	@NamedQuery(
		name = "getWayById",
		query = "SELECT w FROM Way w WHERE w.id = :id"
	)
})
public class Way {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private long id;
	
	@Column(name = "version")
	private int version;
	
	@Column(name = "user_id")
	private int userId;
	
	@Column(name = "tstamp")
	@Temporal(TemporalType.TIMESTAMP)
	private Date timestamp;
	
	@Column(name = "changeset_id")
	private long changesetId;
	
	@Type(type = "hstore")
	@Column(name = "tags", columnDefinition = "hstore")
	private Map<String, String> tags;
	
	@Type(type = "longArray")
	@Column(name = "nodes", columnDefinition = "longArray")
	private long[] nodes;

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
	 * @return the version
	 */
	public int getVersion() {
		return version;
	}

	/**
	 * @param version the version to set
	 */
	public void setVersion(int version) {
		this.version = version;
	}

	/**
	 * @return the userId
	 */
	public int getUserId() {
		return userId;
	}

	/**
	 * @param userId the userId to set
	 */
	public void setUserId(int userId) {
		this.userId = userId;
	}

	/**
	 * @return the timestamp
	 */
	public Date getTimestamp() {
		return timestamp;
	}

	/**
	 * @param timestamp the timestamp to set
	 */
	public void setTimestamp(Date timestamp) {
		this.timestamp = timestamp;
	}

	/**
	 * @return the changesetId
	 */
	public long getChangesetId() {
		return changesetId;
	}

	/**
	 * @param changesetId the changesetId to set
	 */
	public void setChangesetId(long changesetId) {
		this.changesetId = changesetId;
	}

	/**
	 * @return the tags
	 */
	public Map<String, String> getTags() {
		return tags;
	}

	/**
	 * @param tags the tags to set
	 */
	public void setTags(Map<String, String> tags) {
		this.tags = tags;
	}

	/**
	 * @return the nodes
	 */
	public List<Node> getNodes() {
		return Database.INSTANCE.getNodesByIds(Arrays.stream(this.nodes).boxed().collect(Collectors.toList()));
	}
	
	/**
	 * @param nodes the nodes to set
	 */
	public void setNodes(List<Node> nodes) {
		this.nodes = nodes.stream().mapToLong((Node n) -> n.getId()).toArray();
	}
	
	/**
	 * Returns true if the way is closed.
	 * 
	 * @return
	 */
	public boolean isClosed() {
		return this.tags != null && Boolean.parseBoolean(this.tags.get("closed"));
	}
	
	@Override
	public String toString() {
		String ret = String.format(
				"Way[id='%d', version='%d', userId='%d', timestamp='%s', changesetId='%d', tags='%s', nodes='%s']", 
				this.id, this.version, this.userId, this.timestamp.toString(), this.changesetId, 
				this.tags, this.getNodes());
		
		return ret;
	}
	
	public List<Way> getConnectingWays() {
		List<Way> allWays = Database.INSTANCE.getAllWays();
		List<Way> adjacentWays = new ArrayList<Way>();
		
		for (Way currentWay : allWays) {
			if (currentWay.getNodes().get(0).getId() == this.getNodes().get(1).getId()) {
				adjacentWays.add(currentWay);
			}
		}
		
		return adjacentWays;
	}
	
	public static Way getWayFromNode(Node node) {
		Way way = null;
		List<Way> allWays = Database.INSTANCE.getAllWays();
		
		for(Way currentWay : allWays) {
			if (currentWay.getNodes().get(0).equals(node)) {
				way = currentWay;
				break;
			}
		}
		
		return way;
	}
	
	public Way getClosestExitWay() {
		Way exitWay1 = Database.INSTANCE.getWayById(492057177); //East
		Way exitWay2 = Database.INSTANCE.getWayById(703595533); //South-West
		Way exitWay3 = Database.INSTANCE.getWayById(703595967); //North
		Way[] exitWays = {exitWay1, exitWay2, exitWay3};
		
		Way closestWay = null;
		double shortestDistance = Double.MAX_VALUE;
		for (Way exitWay : exitWays) {
			double distance =  this.getNodes().get(1).calculateDistance(exitWay.getNodes().get(0));
			if (distance < shortestDistance) {
				shortestDistance = distance;
				closestWay = exitWay;
			}
		}
		
		return closestWay;
	}
	
	public void closeWay() {
		this.tags.put("closed", "true");
	}
	
	public int getMaxSpeed() {
		String maxSpeed = this.tags.get("maxspeed");
		if (maxSpeed == null) {
			maxSpeed = "45";
		} else {
			maxSpeed = maxSpeed.substring(0, maxSpeed.indexOf("mph"));
		}
		return Integer.parseInt(((maxSpeed != null) ? maxSpeed : "45"));
	}
	
	public int getNumOfLanes() {
		String lanes = this.tags.get("lanes");
		return Integer.parseInt(((lanes != null) ? lanes : "1"));
	}
	
	public double getRoadLength() {
		return this.getNodes().get(0).calculateDistance(this.getNodes().get(1));
	}
	
	public int getNumberOfCars() {
		String cars = this.tags.get("cars");
		
		if (cars == null) {
			cars = "0";
		}
		
		return Integer.parseInt(cars);
	}
	
	public void incrementNumOfCars() {
		String cars = this.tags.get("cars");
		Integer numOfCars = 0;
		if (cars != null) {
			numOfCars = Integer.parseInt(cars);
		}
		numOfCars++;
		this.tags.put("cars", numOfCars.toString());
	}
	
	public void decrementNumOfCars() {
		String cars = this.tags.get("cars");
		Integer numOfCars = 0;
		if (cars != null) {
			numOfCars = Integer.parseInt(cars);
			numOfCars--;
		}
		this.tags.put("cars", numOfCars.toString());
	}
}
