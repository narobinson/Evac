package CS472.urbanevac.db.tables;

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
}
