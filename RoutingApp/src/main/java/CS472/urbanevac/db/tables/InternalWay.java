package CS472.urbanevac.db.tables;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnore;

import CS472.urbanevac.controllers.Route;
import CS472.urbanevac.controllers.Util;

public class InternalWay {
	private long id;
	private Map<String, String> tags;
	private InternalNode startNode;
	private InternalNode endNode;
	private double roadLength;

	public InternalWay(Way way) {
		this.id = way.getId();
		this.tags = new HashMap<>();

		way.getTags().entrySet().parallelStream().forEach((Map.Entry<String, String> e) -> {
			this.tags.put(new String(e.getKey()), new String(e.getValue()));
		});

		if (way.getNodeIds() != null && way.getNodeIds().length == 2) {
			this.startNode = Route.NODES.get(way.getNodeIds()[0]);
			this.endNode = Route.NODES.get(way.getNodeIds()[1]);
		}
		
		if (this.startNode != null && this.endNode != null) {
			this.roadLength = Util.calculateDistance(startNode, endNode);
		}
	}

	public long getId() {
		return this.id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public Map<String, String> getTags() {
		return this.tags;
	}

	public void setTags(Map<String, String> tags) {
		this.tags = tags;
	}

	public InternalNode getStartNode() {
		return this.startNode;
	}

	public void setStartNode(InternalNode startNode) {
		this.startNode = startNode;
	}

	public InternalNode getEndNode() {
		return this.endNode;
	}

	public void setEndNode(InternalNode endNode) {
		this.endNode = endNode;
	}
	
	@Override
	public String toString() {
		return String.format("InternalWay[id = '%d', tags = '%s', startNode = '%s', endNode = '%s', roadLength = '%f']",
				this.id, this.tags, this.startNode, this.endNode, this.roadLength);
	}
	
	@Override
	public boolean equals(Object o) {
		return (o instanceof InternalWay) && ((InternalWay) o).id == this.id;
	}

	public boolean isClosed() {
		return this.tags != null && Boolean.parseBoolean(this.tags.get("closed"));
	}
	
	@JsonIgnore
	public void closeWay() {
		this.tags.put("closed", "true");
	}

	@JsonIgnore
	public int getMaxSpeed() {
		String maxSpeed = this.tags.get("maxspeed");
		if (maxSpeed == null) {
			maxSpeed = "45";
		} else {
			int idx = maxSpeed.indexOf(" ");
			
			if (idx != -1) {
				maxSpeed = maxSpeed.substring(0, idx);
			} else {
				maxSpeed = "45";
			}
		}
		return Integer.parseInt(((maxSpeed != null) ? maxSpeed : "45"));
	}

	@JsonIgnore
	public int getNumOfLanes() {
		String lanes = this.tags.get("lanes");
		return Integer.parseInt(((lanes != null) ? lanes : "1"));
	}

	@JsonIgnore
	public double getRoadLength() {
		return this.roadLength;
	}

	@JsonIgnore
	public int getNumberOfCars() {
		String cars = this.tags.get("cars");
		
		if (cars == null) {
			cars = "0";
		}
		
		return Integer.parseInt(cars);
	}

	@JsonIgnore
	public void incrementNumOfCars() {
		String cars = this.tags.get("cars");
		Integer numOfCars = 0;
		if (cars != null) {
			numOfCars = Integer.parseInt(cars);
		}
		numOfCars++;
		this.tags.put("cars", numOfCars.toString());
	}

	@JsonIgnore
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
