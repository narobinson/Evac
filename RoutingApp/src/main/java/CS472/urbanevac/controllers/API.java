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
	
	/**
	 * Gets all of the nodes
	 * 
	 * @return
	 */
	@RequestMapping("/nodes")
	public @ResponseBody List<Node> getNodes() {
		return db.getAllNodes();
	}
	
	/**
	 * Gets the node with the specified ID
	 * 
	 * @param id
	 * @return
	 */
	@RequestMapping("/nodes/{id}")
	public @ResponseBody Node getNodeById(@PathVariable long id) {
		return db.getNodeById(id);
	}
	
	/**
	 * Gets all of the ways
	 * 
	 * @return
	 */
	@RequestMapping("/ways")
	public @ResponseBody List<Way> getWays() {
		return db.getAllWays();
	}
	
	/**
	 * Gets the way with the specified ID
	 * 
	 * @param id
	 * @return
	 */
	@RequestMapping("/ways/{id}")
	public @ResponseBody Way getWayById(@PathVariable long id) {
		return db.getWayById(id);
	}
	
	/**
	 * Gets all of the closed ways
	 * 
	 * @return
	 */
	@RequestMapping("/closed")
	public @ResponseBody List<Way> getClosedWays() {
		return db.getAllWays().stream().filter((Way w) -> w.getTags() != null && Boolean.parseBoolean(w.getTags().get("closed"))).collect(Collectors.toList());
	}
	
	/**
	 * Gets all the calculated routes
	 * 
	 * @return
	 */
	@RequestMapping("/routes")
	public @ResponseBody List<UserRoute> getRoutes() {
		return db.getAllUserRoutes();
	}
	
	/**
	 * Gets all of the user location groups
	 * Has the counts of cars for each area
	 * 
	 * @return
	 */
	@RequestMapping("/heatmap")
	public @ResponseBody List<UserLocationGroup> getHeatmap() {
		return db.getAllUserLocationGroups();
	}
	
	/**
	 * Gets the user location group with the specified ID
	 * 
	 * @param id
	 * @return
	 */
	@RequestMapping("/heatmap/{id}")
	public @ResponseBody UserLocationGroup getHeatmapById(@PathVariable long id) {
		return db.getUserLocationGroupById(id);
	}
	
	/**
	 * Gets all of the users
	 * 
	 * @return
	 */
	@RequestMapping("/users")
	public @ResponseBody List<User> getUsers() {
		return db.getAllUsers();
	}
	
	/**
	 * Gets the user with the specified UID
	 * 
	 * @param uid
	 * @return
	 */
	@RequestMapping("/users/{uid}")
	public @ResponseBody User getUserByUID(@PathVariable UUID uid) {
		return db.getUserByUUID(uid);
	}
	
	/**
	 * Adds or updates a user's location in the system
	 * 
	 * @param uid
	 * @param lat
	 * @param lon
	 * @return
	 */
	@RequestMapping("/users/add/{uid}/{lat}/{lon}")
	public @ResponseBody User addUserLocation(@PathVariable UUID uid, @PathVariable double lat, @PathVariable double lon) {
		return db.addOrUpdateUserLocation(uid, lat, lon);
	}
}
