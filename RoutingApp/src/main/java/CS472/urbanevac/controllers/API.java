package CS472.urbanevac.controllers;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import CS472.urbanevac.db.Database;
import CS472.urbanevac.db.tables.InternalNode;
import CS472.urbanevac.db.tables.InternalWay;
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
	public @ResponseBody List<InternalNode> getNodes() {
		return new ArrayList<>(Route.NODES.values());
	}
	
	/**
	 * Gets the node with the specified ID
	 * 
	 * @param id
	 * @return
	 */
	@RequestMapping("/nodes/{id}")
	public @ResponseBody InternalNode getNodeById(@PathVariable long id) {
		return Route.NODES.get(new Long(id));
	}
	
	/**
	 * Gets all of the ways
	 * 
	 * @return
	 */
	@RequestMapping("/ways")
	public @ResponseBody List<InternalWay> getWays() {
		return new ArrayList<>(Route.WAYS.values());
	}
	
	/**
	 * Gets the way with the specified ID
	 * 
	 * @param id
	 * @return
	 */
	@RequestMapping("/ways/{id}")
	public @ResponseBody InternalWay getWayById(@PathVariable long id) {
		return Route.WAYS.get(new Long(id));
	}
	
	/**
	 * Gets all of the closed nodes
	 * 
	 * @return
	 */
	@RequestMapping("/closed")
	public @ResponseBody List<InternalNode> getClosedNodes() {
		return new ArrayList<>(Route.NODES.values()).stream().filter(n -> n.isClosed()).collect(Collectors.toList());
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
	
	@RequestMapping("/nuke")
	public void nuke(){
		List<Way> ways = db.getAllWays();
		
		List<Way> newWays = ways.parallelStream().map((Way w) -> {
			List<Way> subWays = new LinkedList<>();
			
			for (int i = 0; i < w.getNodes().size() - 1; i++) {
				Way way = new Way();
				List<Node> nodes = new ArrayList<>(2);
				
				nodes.add(w.getNodes().get(i));
				nodes.add(w.getNodes().get(i + 1));
				
				way.setNodes(nodes);
				way.setTags(w.getTags());
				way.setTimestamp(w.getTimestamp());
				way.setChangesetId(w.getChangesetId());
				way.setUserId(w.getUserId());
				way.setVersion(w.getVersion());
				
				subWays.add(way);
			}
			
			return subWays;
		}).flatMap(List::stream).collect(Collectors.toList());
		
		System.out.println("Old: " + ways.size());
		System.out.println("New: " + newWays.size());
		
		db.persist(newWays.toArray());
		db.delete(ways.toArray());
	}
	
//	@RequestMapping("/delete")
//	public void delete() {
//		List<Node> all = Route.PRIV_NODES;
//		List<Node> toDelete = new LinkedList<>();
//		List<Long> used = new LinkedList<>();
//		
//		Route.PRIV_WAYS.parallelStream().forEach((Way w) -> {
//			used.add(w.getNodes().get(0).getId());
//			used.add(w.getNodes().get(1).getId());
//		});
//		
//		System.out.println("Nodes: " + used.size());
//		
//		for (Node n : all) {
//			if (!used.contains(n.getId())) {
//				toDelete.add(n);
//			}
//		}
//		
//		System.out.println("Delete: " + toDelete.size());
//		
//		db.delete(toDelete.toArray());
//	}
//	
//	@RequestMapping("/debug/{id}")
//	public @ResponseBody List<Way> debug(@PathVariable long id) {
//		return Way.getWaysFromNode(db.getNodeById(id));
//	}
}
