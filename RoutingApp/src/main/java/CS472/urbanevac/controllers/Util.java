package CS472.urbanevac.controllers;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import CS472.urbanevac.db.tables.InternalNode;
import CS472.urbanevac.db.tables.InternalWay;
import CS472.urbanevac.db.tables.User;
import CS472.urbanevac.db.tables.UserRoute;

public class Util {	
	public static InternalWay getWayFromNode(InternalNode n) {
		return Util.getWaysStream(n).findFirst().get();
	}
	
	public static List<InternalWay> getWaysFromNode(InternalNode n) {
		return Util.getWaysStream(n).collect(Collectors.toList());
	}
	
	private static Stream<InternalWay> getWaysStream(InternalNode n) {
		return Route.ADJ_MATRIX.get(n.getId()).values().parallelStream().map((Long id) -> Route.WAYS.get(id));
	}
	
	public static boolean routeContainsClosedNode(User user) {
		UserRoute route = user.getRoute();
		long count = 0;
		
		if (route != null && route.getRoute() != null) {
			count = new ArrayList<>(Route.NODES.values()).stream().filter(n -> n.isClosed()).filter(n -> route.getRoute().contains(n)).count();
		}
		
		return count != 0;
	}
	
	public static InternalNode getClosestExitNode(InternalNode node) {
		InternalNode closest = null;
		double dist = Double.MAX_VALUE;
		
		for (InternalNode exitNode : Route.EXIT_NODES) {
			if (exitNode != null && !exitNode.isClosed()) {
				double d = Util.calculateDistance(node, exitNode);
				
				if (d < dist) {
					dist = d;
					closest = exitNode;
				}
			}
		}
		
		return closest;
	}
	
	public static double calculateDistance(InternalNode node1, InternalNode node2) {
		return calculateDistance(node1.getLatitude(), node1.getLongitude(), node2.getLatitude(), node2.getLongitude());
	}
	
	public static double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
		double distance = 0.0;
		int radius = 6371; //Radius of Earth in Km
		double dlat = degToRad(lat2 - lat1);
		double dlon = degToRad(lon2 - lon1);
		
		double a = Math.sin(dlat/2) * Math.sin(dlat/2) +
				Math.cos(degToRad(lat1)) * Math.cos(degToRad(lat2)) *
				Math.sin(dlon/2) * Math.sin(dlon/2);
		
		double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
		distance = radius * c;
		
		return distance;
	}
	
	private static double degToRad(double deg) {
		return deg * (Math.PI/180);
	}
}
