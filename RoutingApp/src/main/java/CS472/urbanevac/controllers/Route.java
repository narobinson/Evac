package CS472.urbanevac.controllers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import CS472.urbanevac.db.Database;
import CS472.urbanevac.db.tables.Node;
import CS472.urbanevac.db.tables.User;
import CS472.urbanevac.db.tables.UserRoute;
import CS472.urbanevac.db.tables.Way;

@RestController
@RequestMapping("/route")
public class Route {
	@Autowired
	private Database db;

	public static List<Way> WAYS;
	public static List<Node> NODES;
	
	public static List<Way> PRIV_WAYS;
	public static List<Node> PRIV_NODES;


	public static Way exitWay1;	//East
	public static Way exitWay2; //South-West
	public static Way exitWay3; //North
	public static Way[] exitWays = new Way[2];
	
	@PostConstruct
	public void setup() {
		PRIV_WAYS = db.getAllWays();
		List<Way> newWays = new ArrayList<>(PRIV_WAYS.size());
		
		PRIV_WAYS.stream().forEach((Way w) -> {
			Way way = new Way();
			way.setChangesetId(w.getChangesetId());
			way.setId(w.getId());
			way.setNodes(w.getNodes());
			way.setTags(w.getTags());
			way.setTimestamp(w.getTimestamp());
			way.setUserId(w.getUserId());
			way.setVersion(w.getVersion());
			newWays.add(way);
		});
		
		PRIV_NODES = db.getAllNodes();
		List<Node> newNodes = new ArrayList<>(PRIV_NODES.size());
		
		PRIV_NODES.stream().forEach((Node n) -> {
			Node node = new Node();
			node.setChangesetId(n.getChangesetId());
			node.setGeom(n.getGeom());
			node.setId(n.getId());
			node.setTags(n.getTags());
			node.setTimestamp(n.getTimestamp());
			node.setUserId(n.getUserId());
			node.setVersion(n.getVersion());
			newNodes.add(node);
		});
		
		WAYS = Collections.unmodifiableList(newWays);
		NODES = Collections.unmodifiableList(newNodes);
		
		exitWay1 = Way.getWayFromNode(db.getNodeById(492057177)); //East
		exitWay2 = Way.getWayFromNode(db.getNodeById(703595533)); //South-West
		exitWay3 = Way.getWayFromNode(db.getNodeById(703595967)); //North
		exitWays[0] = exitWay2;
		exitWays[1] = exitWay3;
		//exitWays[2] = exitWay3;
		
		System.out.println(Arrays.toString(exitWays));
		
//		WAYS.parallelStream().forEach((Way w1) -> {
//			System.out.println(count);
//			
//			WAYS.parallelStream().forEach((Way w2) -> {
//				if (w1.getNodes().get(0).getId() == w2.getNodes().get(1).getId()) {
//					w1.addConnectingWays(w2);
//					w2.addConnectingWays(w1);
//				}
//			});
//			
//			System.out.println(count++);
//		});
	}
	
	/**
	 * Closes a way with the given ID
	 * 
	 * @param id
	 * @return
	 */
	@RequestMapping("/close/{id}")
	public @ResponseBody boolean closeRoad(@PathVariable long id) {
		boolean closed = db.closeWay(id);
		
		List<User> allUsers = db.getAllUsers();
		for (User user : allUsers) {
			String route = user.getRoute().getRoute();
			String[] ways = route.split("$");
			for (String way : ways) {
				long wayId = Long.parseLong(way.substring(way.indexOf('&') + 1));
				Way wayObject = db.getWayById(wayId);
				wayObject.closeWay();
				if (wayId == id) {
					UserRoute newRoute = calculateRoute(user, user.getRoute().getLastVisitedNode());
					user.setRoute(newRoute);
				}
			}
		}
		
		return closed;
	}
	
	/**
	 * Opens a way with the given ID
	 * 
	 * @param id
	 * @return
	 */
	@RequestMapping("/open/{id}")
	public @ResponseBody boolean openRoad(@PathVariable long id) {
		return db.openWay(id);
	}
	
	/**
	 * Updates the last visited node of the user
	 * 
	 * @param uid
	 * @param nodeId
	 * @return
	 */
	@RequestMapping("/update/{uid}/{nodeId}")
	public @ResponseBody UserRoute updateLastVisitedNode(@PathVariable UUID uid, @PathVariable long nodeId) {
		return db.updateLastVisitedNode(uid, nodeId);
	}
	
	/**
	 * Gets the current route for the specified UID
	 * 
	 * @param uid
	 * @return
	 */
	@RequestMapping("/{uid}")
	public @ResponseBody UserRoute getRoute(@PathVariable UUID uid) {
		User user = db.getUserByUUID(uid);
		UserRoute route = user.getRoute();
		
		if (route == null) {
			route = this.calculateRoute(user, null);
		}
		
		return route;
	}
	
	public UserRoute calculateRoute(User user, Node lastVisited) {
		UserRoute route = new UserRoute();
		Way userLocation;
		if (lastVisited != null) {
			userLocation = Way.getWayFromNode(lastVisited);
		} else {
			userLocation = Way.getWayFromNode(user.getLocation());
		}
		
		System.out.println(userLocation);
		
		Way goal = userLocation.getClosestExitWay();

		System.out.println(user.getLocation());
		System.out.println(goal);
		
		Set<Way> closedSet = new HashSet<Way>();
		Set<Way> openSet = new HashSet<Way>();
		openSet.add(userLocation);
		Map<Way, Way> cameFrom = new HashMap<Way, Way>();
		
		Map<Way, Integer> gScore = new HashMap<Way, Integer>();
		gScore.put(userLocation, 0);
		
		Map<Way, Integer> fScore = new HashMap<Way, Integer>();
		fScore.put(userLocation, userLocation.getNodes().get(1).calculateDistance(goal.getNodes().get(0)).intValue());
		
		Way currentWay = null;
		while (openSet.isEmpty() == false) {
			Way wayWithLowestFScore = null;
			for (Object iteratedWay : openSet.toArray()) {
				Way way = (Way) iteratedWay;
				if (wayWithLowestFScore == null) {
					wayWithLowestFScore = way;
				} else {
					if (fScore.get(way) < fScore.get(wayWithLowestFScore)) {
						wayWithLowestFScore = way;
					}
				}
			}
			
			currentWay = wayWithLowestFScore;
			if (currentWay.equals(goal)) {
				break;
			}
			
			openSet.remove(currentWay);
			closedSet.add(currentWay);

			System.out.println("Current way: " + currentWay);
			
			List<Way> adjacentWays = currentWay.getConnectingWays();
			
			System.out.println("Adjacent ways: " + adjacentWays.size());
			
			for (Way adjacentWay : adjacentWays) {
				if (closedSet.contains(adjacentWay) || adjacentWay.isClosed()) {
					continue;
				} else {
					Integer tentativeGScore = gScore.get(currentWay) + calculateHeuristic(currentWay, adjacentWay, goal).intValue();
					
					if (openSet.contains(adjacentWay) == false) {
						openSet.add(adjacentWay);
					} else if (tentativeGScore >= gScore.get(adjacentWay)) {
						continue;
					}
					
					cameFrom.put(adjacentWay, currentWay);
					gScore.put(adjacentWay, tentativeGScore);
					fScore.put(adjacentWay, gScore.get(adjacentWay) + adjacentWay.getNodes().get(1).calculateDistance(goal.getNodes().get(0)).intValue());
				}
			}
		}
		
		System.out.println("Came from: " + cameFrom.size());
		
		// Route will consist of concatenated ways which will contain their lat, lon, and id, to produce a string that looks like:
		// "$LAT126112LON123123&12312412"
		for (Map.Entry<Way, Way> entry : cameFrom.entrySet()) {
			Way start = entry.getValue();
			start.incrementNumOfCars();
			route.setRoute(route.getRoute() + "$" + start.getNodes().get(0).getId() + "|" + start.getNodes().get(1).getId() + "&" + start.getId());
		}
		goal.incrementNumOfCars();
		route.setRoute(route.getRoute() + "$" + goal.getNodes().get(0).getId() + "|" + goal.getNodes().get(1).getId() + "&" + goal.getId());
		
		return route;
	}
	
	private Double calculateHeuristic(Way begin, Way end, Way goal) {
		Double distanceToGoal = begin.getNodes().get(1).calculateDistance(goal.getNodes().get(0));
		int numCars = end.getNumberOfCars();
		double endsRoadLength = end.getRoadLength();
		int maxSpeed = end.getMaxSpeed();
		int lanes = end.getNumOfLanes();
		Double estimatedSpeed = (-0.2) * Math.pow(Math.E, ((0.73529 * (numCars/endsRoadLength)/(maxSpeed * lanes)) + maxSpeed));
		
		return distanceToGoal/estimatedSpeed;
	}
}
