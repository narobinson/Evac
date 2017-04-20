package CS472.urbanevac.controllers;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

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
		Way goal = userLocation.getClosestExitWay();
		
		Set<Way> closedSet = new HashSet<Way>();
		Set<Way> openSet = new HashSet<Way>();
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
			
			List<Way> adjacentWays = currentWay.getConnectingWays();
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
		
		// Route will consist of concatenated ways which will contain their lat, lon, and id, to produce a string that looks like:
		// "$LAT126112LON123123&12312412"
		for (Map.Entry<Way, Way> entry : cameFrom.entrySet()) {
			Way start = entry.getValue();
			start.incrementNumOfCars();
			route.setRoute(route.getRoute() + "$LAT" + start.getNodes().get(0).getLatitude() + "LON"+ start.getNodes().get(0).getLongitude() + "&" + start.getId());
		}
		goal.incrementNumOfCars();
		route.setRoute(route.getRoute() + "$LAT" + goal.getNodes().get(0).getLatitude() + "LON"+ goal.getNodes().get(0).getLongitude() + "&" + goal.getId());
		
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
