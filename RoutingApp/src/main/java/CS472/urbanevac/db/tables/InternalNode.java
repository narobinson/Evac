package CS472.urbanevac.db.tables;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class InternalNode {
	private long id;
	private double latitude;
	private double longitude;
	private boolean closed;
	
	public InternalNode(Node n) {
		this.id = n.getId();
		this.latitude = n.getLatitude();
		this.longitude = n.getLongitude();
		this.closed = false;
		
		if (n.getTags() != null) {
			this.closed = Boolean.parseBoolean(n.getTags().get("closed"));
		}
	}

	public long getId() {
		return this.id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public double getLatitude() {
		return this.latitude;
	}

	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}

	public double getLongitude() {
		return this.longitude;
	}

	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}
	
	@JsonIgnore
	public boolean isClosed() {
		return this.closed;
	}
	
	public void close() {
		this.closed = true;
	}
	
	public void open() {
		this.closed = false;
	}
	
	@Override
	public String toString() {
		return String.format("InternalNode[id = '%d', latitide = '%f', longitide ='%f', closed = '%s']", 
				this.id, this.latitude, this.longitude, this.closed);
	}
	
	@Override
	public boolean equals(Object o) {
		return (o instanceof InternalNode) && ((InternalNode) o).id == this.id;
	}
}
