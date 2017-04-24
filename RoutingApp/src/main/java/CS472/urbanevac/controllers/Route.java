package CS472.urbanevac.controllers;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.apache.commons.lang3.time.StopWatch;
import org.jboss.logging.Logger;
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
import CS472.urbanevac.db.tables.UserRoute;
import CS472.urbanevac.db.tables.Way;

@RestController
@RequestMapping("/route")
public class Route {
	@Autowired
	private Database db;

	public static Map<Long, InternalWay> WAYS;
	public static Map<Long, InternalNode> NODES;

	public static InternalNode EXIT_NODE_EAST;	//East
	public static InternalNode EXIT_NODE_SOUTH; //South-West
	public static InternalNode EXIT_NODE_NORTH; //North
	public static InternalNode[] EXIT_NODES = new InternalNode[3];
	
	public static Map<Long, Map<Long, Long>> ADJ_MATRIX = new ConcurrentHashMap<>();			// Maps node IDs to adjacent nodes and the way that made them adjacent
	
	private static Logger logger = Logger.getLogger(Route.class);
	
	@PostConstruct
	public void setup() throws IOException {
		logger.info("Loading nodes and ways from database.");
		
		List<Node> nodes = db.getAllNodes();
		List<Way> ways = db.getAllWays();
		
		logger.info("Loaded " + nodes.size() + " nodes and " + ways.size() + " ways.");

		logger.info("Creating in-memory nodes and ways...");
		
		// Create the in-memory representation of Nodes and Ways
		NODES = nodes.parallelStream().map((Node n) -> {
			logger.debug("Converting node: " + n.getId());
			return new InternalNode(n);
		}).collect(Collectors.toMap((InternalNode n) -> n.getId(), (InternalNode n) -> n));
		WAYS = ways.parallelStream().map((Way n) -> {
			logger.debug("Converting way: " + n.getId());
			InternalWay way = new InternalWay(n);
			
			if (way.getStartNode() != null && way.getEndNode() != null) {
				return way;
			} else {
				return null;
			}
		}).filter(Objects::nonNull).collect(Collectors.toMap((InternalWay w) -> w.getId(), (InternalWay w) -> w));
		
//		.filter(w -> w.getTags() != null && w.getTags().containsKey("highway"))
		
		logger.info("Finished creating in-memory nodes and ways.");
		logger.info("Nodes: " + NODES.size() + " Ways: " + WAYS.size());

		// Setup the lists to hold the adjacent nodes
		logger.info("Creating mappings for all used nodes...");
//		NODES.values().parallelStream().forEach((InternalNode n) -> {
//			logger.debug("Creating mapping for node: " + n.getId());
//			ADJ_MATRIX.put(n.getId(), new ConcurrentHashMap<>());
//		});
		WAYS.values().parallelStream().forEach(w -> {
			logger.debug("Creating mapping for node: " + w.getStartNode().getId());
			ADJ_MATRIX.put(w.getStartNode().getId(), new ConcurrentHashMap<>());
			
			logger.debug("Creating mapping for node: " + w.getEndNode().getId());
			ADJ_MATRIX.put(w.getEndNode().getId(), new ConcurrentHashMap<>());
		});
		logger.info("Finished creating mappings for all used nodes.");

		logger.info("Removing unused nodes...");
		NODES = NODES.keySet().parallelStream().filter(n -> ADJ_MATRIX.containsKey(n)).collect(Collectors.toMap(n -> n, n -> NODES.get(n)));
		logger.info("Unused nodes removed.");
		
		logger.info("Adjacent node matrix has " + ADJ_MATRIX.size() + " entries.");
		
		logger.info("Loading adjacent nodes...");
		// Load the adjacent nodes
		new ArrayList<>(WAYS.values()).parallelStream().forEach((InternalWay w) -> {
			logger.debug("Processing way: " + w.getId());
			Map<Long, Long> first = ADJ_MATRIX.get(w.getStartNode().getId());
			Map<Long, Long> second = ADJ_MATRIX.get(w.getEndNode().getId());
			
			if (first == null) {
				logger.error("No mapping exists for " + w.getStartNode().getId() + ". Creating a new mapping.");
				first = new ConcurrentHashMap<>();
				ADJ_MATRIX.put(w.getStartNode().getId(), first);
			}
			if (second == null) {
				logger.error("No mapping exists for " + w.getStartNode().getId() + ". Creating a new mapping.");
				second = new ConcurrentHashMap<>();
				ADJ_MATRIX.put(w.getEndNode().getId(), second);
			}
			
			first.put(w.getEndNode().getId(), w.getId());
			second.put(w.getStartNode().getId(), w.getId());
		});
		logger.info("Finished loading adjacent nodes.");

		File matrixFile = new File("Matrix.txt");
		File nodeFile = new File("Nodes.txt");
		File wayFile = new File("Ways.txt");

		logger.info("Saving matrix to file: " + matrixFile.getAbsolutePath());
		logger.info("Saving nodes to file: " + nodeFile.getAbsolutePath());
		logger.info("Saving ways to file: " + wayFile.getAbsolutePath());

		Files.write(matrixFile.toPath(), ADJ_MATRIX.toString().getBytes(), StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING);
		Files.write(nodeFile.toPath(), NODES.toString().getBytes(), StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING);
		Files.write(wayFile.toPath(), WAYS.toString().getBytes(), StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING);
		
		logger.info("Loading global exit nodes...");
		
		// Setup the exit nodes
		EXIT_NODE_EAST = NODES.get(new Long(492057177)); //East
		EXIT_NODE_SOUTH = NODES.get(new Long(703595533)); //South-West
		EXIT_NODE_NORTH = NODES.get(new Long(703595967)); //North
		EXIT_NODES[0] = EXIT_NODE_EAST;
		EXIT_NODES[1] = EXIT_NODE_SOUTH;
		EXIT_NODES[2] = EXIT_NODE_NORTH;
		
		logger.info("Finished loading global exit nodes: " + Arrays.toString(EXIT_NODES));
	}
	
	/**
	 * Closes a node with the given ID
	 * 
	 * @param id
	 * @return
	 */
	@RequestMapping("/close/{id}")
	public @ResponseBody boolean closeRoad(@PathVariable long id) {
//		boolean closed = db.closeWay(id);
//		WAYS.get(new Long(id)).closeWay();
		
		boolean closed = db.closeNode(id);
		NODES.get(new Long(id)).close();
		
		db.getAllUsers().stream().filter(Util::routeContainsClosedNode).forEach(u -> {
			logger.info("Closing way: " + id + " has caused " + u.getUid() + "'s route to be recalculated.");
			calculateRoute(u);
		});
		
		return closed;
	}
	
	/**
	 * Opens a node with the given ID
	 * 
	 * @param id
	 * @return
	 */
	@RequestMapping("/open/{id}")
	public @ResponseBody boolean openRoad(@PathVariable long id) {
		boolean closed = db.openNode(id);
		NODES.get(new Long(id)).open();
		
		return closed;
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
		
		if (user == null) {
			logger.error("User: " + uid + " requested a route but is not enrolled in the system.");
			return null;
		}
		
		UserRoute route = user.getRoute();
		
		logger.info("User: " + uid + " has requested a route.");
		
		if (route == null) {
			logger.info("No route stored for " + uid + ". Calulating a new route.");
			
			StopWatch sw = new StopWatch();
			sw.start();
			
			route = this.calculateRoute(user);
			
			sw.stop();
			logger.info("Calculated the route for " + uid + " in " + sw.getTime() + "ms. Contains " + route.getRoute().size() + " nodes.");
		}
		
		return route;
	}
	
	public UserRoute calculateRoute(User user) {
		UserRoute route = new UserRoute();
		InternalNode startLocation = null;
		
		if (user.getRoute() != null) {
			startLocation = user.getRoute().getLastVisitedNode();
		}
		
		if (startLocation == null) {
			startLocation = user.getLocation();
		}
		
		InternalNode goal = Util.getClosestExitNode(startLocation);
		
		logger.info(user.getUid() + "'s start location: " + startLocation);
		logger.info(user.getUid() + "'s end location: " + goal);
		
		Set<InternalNode> closedSet = new HashSet<>();
		Set<InternalNode> openSet = new HashSet<>();
		openSet.add(startLocation);
		Map<InternalNode, InternalNode> cameFrom = new HashMap<>();
		
		Map<InternalNode, Integer> gScore = new HashMap<>();
		gScore.put(startLocation, 0);
		
		Map<InternalNode, Integer> fScore = new HashMap<>();
		fScore.put(startLocation, (int) Util.calculateDistance(startLocation, goal));
		
		InternalNode currentNode = null;
		while (openSet.isEmpty() == false) {
			InternalNode nodeWithLowestFScore = null;
			for (InternalNode node : openSet) {
				if (nodeWithLowestFScore == null) {
					nodeWithLowestFScore = node;
				} else {
					if (fScore.get(node) < fScore.get(nodeWithLowestFScore)) {
						nodeWithLowestFScore = node;
					}
				}
			}
			
			currentNode = nodeWithLowestFScore;
			if (currentNode.equals(goal)) {
				break;
			}
			
			openSet.remove(currentNode);
			closedSet.add(currentNode);

			List<InternalNode> adjacentNodes = Route.ADJ_MATRIX.get(currentNode.getId()).keySet().parallelStream().map((Long id) -> Route.NODES.get(id)).collect(Collectors.toList());
			
			for (InternalNode adjacentNode : adjacentNodes) {
				if (closedSet.contains(adjacentNode) || Route.WAYS.get(Route.ADJ_MATRIX.get(currentNode.getId()).get(adjacentNode.getId())).isClosed() || adjacentNode.isClosed()) {
					continue;
				} else {
					int tentativeGScore = (int) (gScore.get(currentNode) + calculateHeuristic(currentNode, adjacentNode, goal));
					
					if (openSet.contains(adjacentNode) == false) {
						openSet.add(adjacentNode);
					} else if (tentativeGScore >= gScore.get(adjacentNode)) {
						continue;
					}
					
					cameFrom.put(adjacentNode, currentNode);
					gScore.put(adjacentNode, tentativeGScore);
					fScore.put(adjacentNode, gScore.get(adjacentNode) + (int) Util.calculateDistance(adjacentNode, goal));
				}
			}
		}
		
		List<InternalNode> totalPath = new LinkedList<>();
		totalPath.add(currentNode);
		
		while (cameFrom.containsKey(currentNode)) {
			currentNode = cameFrom.get(currentNode);
			totalPath.add(0, currentNode);
		}
		
		totalPath.stream().map(n -> "$" + n.getId()).forEachOrdered(s -> {
			route.setRawRoute(route.getRawRoute() == null ? "" : route.getRawRoute() + s);
		});
		
//		for (Map.Entry<InternalNode, InternalNode> entry : cameFrom.entrySet()) {
//			Route.WAYS.get(Route.ADJ_MATRIX.get(entry.getValue().getId()).get(entry.getKey().getId())).incrementNumOfCars();
//			
//			route.setRawRoute((route.getRawRoute() == null ? "" : route.getRawRoute()) + "$" +  entry.getValue().getId());
//		}
//		route.setRawRoute(route.getRawRoute() + "$" + goal.getId());
		
		db.persist(route);
		db.setUserRoute(user.getUid(), route);
		
		return route;
	}
	
	private double calculateHeuristic(InternalNode begin, InternalNode end, InternalNode goal) {
		InternalWay endWay = Route.WAYS.get(Route.ADJ_MATRIX.get(begin.getId()).get(end.getId()));
		
		double distanceToGoal = Util.calculateDistance(begin, goal);
		int numCars = endWay.getNumberOfCars();
		double endsRoadLength = endWay.getRoadLength();
		int maxSpeed = endWay.getMaxSpeed();
		int lanes = endWay.getNumOfLanes();
		double estimatedSpeed = (-0.2) * Math.pow(Math.E, ((0.73529 * (numCars/endsRoadLength)/(maxSpeed * lanes)) + maxSpeed));
		
		return distanceToGoal / estimatedSpeed;
	}
}
