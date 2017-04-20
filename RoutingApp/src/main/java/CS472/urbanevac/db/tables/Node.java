package CS472.urbanevac.db.tables;

import java.util.List;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map;

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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.vividsolutions.jts.geom.Geometry;

import CS472.urbanevac.db.Database;
import CS472.urbanevac.db.types.HstoreUserType;

@Entity
@Table(name = "nodes")
@TypeDefs({
	@TypeDef(name = "hstore", typeClass = HstoreUserType.class)
})
@NamedQueries({
	@NamedQuery(
		name = "getAllNodes",
		query = "SELECT n FROM Node n"
	),
	@NamedQuery(
		name = "getNodeById",
		query = "SELECT n FROM Node n WHERE n.id = :id"
	),
	@NamedQuery(
		name = "getNodeByIds",
		query = "SELECT n FROM Node n WHERE n.id IN :idList"
	)
})
public class Node{
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
	
	@JsonIgnore
	@Column(name = "geom", columnDefinition = "org.hibernate.spatial.JTSGeometryType")
	private Geometry geom;

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
	 * @return the geom
	 */
	public Geometry getGeom() {
		return geom;
	}

	/**
	 * @param geom the geom to set
	 */
	public void setGeom(Geometry geom) {
		this.geom = geom;
	}
	
	public double getLatitude() {
		return this.geom.getInteriorPoint().getY();
	}
	
	public double getLongitude() {
		return this.geom.getInteriorPoint().getX();
	}
	
	@Override
	public String toString() {
		String ret = String.format("Node[id='%d', version='%d', userId='%d', timestamp='%s', "
				+ "changesetId='%d', tags='%s', geom='%s', lat='%f', lon='%f']",
				this.id, this.version, this.userId, this.timestamp.toString(), this.changesetId, 
				this.tags.toString(), this.geom.toString(), this.getLatitude(), this.getLongitude());
		
		return ret;
	}
	
	public Double calculateDistance(Node node) {
		Double distance = 0.0;
		int radius = 6371; //Radius of Earth in Km
		Double dlat = degToRad(node.getLatitude() - this.getLatitude());
		Double dlon = degToRad(node.getLongitude() - this.getLongitude());
		
		Double a = Math.sin(dlat/2) * Math.sin(dlat/2) +
				Math.cos(degToRad(this.getLatitude())) * Math.cos(degToRad(node.getLatitude())) *
				Math.sin(dlon/2) * Math.sin(dlon/2);
		
		Double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
		distance = radius * c;
		
		return distance;
	}
	
	private Double degToRad(Double deg) {
		return deg * (Math.PI/180);
	}
	
	@Override
	public boolean equals(Object obj) {
		return (this.id == ((Node) obj).getId());
	}
}
