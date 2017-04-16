package CS472.urbanevac.controllers;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import CS472.urbanevac.db.Database;
import CS472.urbanevac.db.tables.Node;
import CS472.urbanevac.db.tables.User;
import CS472.urbanevac.db.tables.UserLocationGroup;
import CS472.urbanevac.db.tables.UserRoute;
import CS472.urbanevac.db.tables.Way;

@RestController
@RequestMapping("/api")
public class API {
	@Autowired
	private Database db;
	
	@RequestMapping("/nodes")
	public @ResponseBody List<Node> getNodes() {
		return db.getAllNodes();
	}
	
	@RequestMapping("/nodes/{id}")
	public @ResponseBody Node getNodeById(@PathVariable long id) {
		return db.getNodeById(id);
	}
	
	@RequestMapping("/ways")
	public @ResponseBody List<Way> getWays() {
		return db.getAllWays();
	}
	
	@RequestMapping("/ways/{id}")
	public @ResponseBody Way getWayById(@PathVariable long id) {
		return db.getWayById(id);
	}
	
	@RequestMapping("/closed")
	public @ResponseBody List<Way> getClosedWays() {
		return db.getAllWays().stream().filter((Way w) -> w.getTags().get("closed") != null && Boolean.parseBoolean(w.getTags().get("closed"))).collect(Collectors.toList());
	}
	
	@RequestMapping("/routes")
	public @ResponseBody List<UserRoute> getRoutes() {
		return db.getAllUserRoutes();
	}
	
	@RequestMapping("/heatmap")
	public @ResponseBody List<UserLocationGroup> getHeatmap() {
		return db.getAllUserLocationGroups();
	}
	
	@RequestMapping("/heatmap/{id}")
	public @ResponseBody UserLocationGroup getHeatmapById(@PathVariable long id) {
		return db.getUserLocationGroupById(id);
	}
	
	@RequestMapping("/users")
	public @ResponseBody List<User> getUsers() {
		return db.getAllUsers();
	}
	
	@RequestMapping("/users/{uid}")
	public @ResponseBody User getUserByUID(@PathVariable UUID uid) {
		return db.getUserByUUID(uid);
	}
	
	@RequestMapping("/users/add/{lat}/{lon}/{uid}")
	public @ResponseBody boolean addUserLocation(@PathVariable double lat, @PathVariable double lon, @PathVariable UUID uid) {
		// Check to see if the user is already in the database
		User previous = db.getUserByUUID(uid);

		if (previous != null) {
			// This person already exists
			System.out.println("Person with GUID: " + uid + " already exists");

			// Get the group this person belonged to so it can be updated if necessary.
			UserLocationGroup prevGroup = previous.getUserGroup();
			UserLocationGroup newGroup = db.getUserLocationGroupByLatLon(lat, lon);

			// Update the location
			previous.setLat(lat);
			previous.setLon(lon);

			// If the user moved to a new group, update the counts
			if (prevGroup.getId() != newGroup.getId()) {
				previous.setUserGroup(newGroup);
				
				prevGroup.decrementCount();
				newGroup.incrementCount();
			}
		} else {
			// This person didn't exist, add them
			System.out.println("Adding new person with GUID: " + uid);

			// Get the group location
			UserLocationGroup group = db.getUserLocationGroupByLatLon(lat, lon);

			// Create the user
			User ul = new User();
			ul.setLat(lat);
			ul.setLon(lon);
			ul.setUid(uid);
			ul.setUserGroup(group);

			// Add the user to the group location count
			group.incrementCount();

			// Add it to the database
			db.persist(ul);
		}

		return true;
	}
}
