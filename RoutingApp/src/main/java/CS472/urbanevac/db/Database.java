package CS472.urbanevac.db;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import CS472.urbanevac.db.tables.Node;
import CS472.urbanevac.db.tables.User;
import CS472.urbanevac.db.tables.UserLocationGroup;
import CS472.urbanevac.db.tables.UserRoute;
import CS472.urbanevac.db.tables.Way;

@Component
@SuppressWarnings("unchecked")
public class Database {
	// Try to avoid using this call
	public static Database INSTANCE;

	@Autowired
	private DatabaseConnection dbc;

	@PostConstruct
	private void setupInstance() {
		INSTANCE = this;
	}

	/* Node */
	public List<Node> getAllNodes() {
		DatabaseSession session = dbc.getPostgresSession();

		List<Node> nodes = session
				.getNamedQuery("getAllNodes")
				.list();

		session.close();

		return nodes;
	}

	public Node getNodeById(long id) {
		DatabaseSession session = dbc.getPostgresSession();

		Node node = session
				.getNamedQuery("getNodeById")
				.setParameter("id", id)
				.single();

		session.close();

		return node;
	}

	public List<Node> getNodesByIds(List<Long> ids) {
		DatabaseSession session = dbc.getPostgresSession();

		List<Node> nodes = session
				.getNamedQuery("getNodeByIds")
				.setParameterList("idList", ids)
				.list();

		session.close();

		return nodes;
	}
	/* Node */

	/* User */
	public List<User> getAllUsers() {
		DatabaseSession session = dbc.getMSSqlSession();

		List<User> users = session
				.getNamedQuery("getAllUsers")
				.list();

		session.close();
		
		return users;
	}

	public User getUserById(long id) {
		DatabaseSession session = dbc.getMSSqlSession();

		User user = null;

		user = session
				.getNamedQuery("getUserById")
				.setParameter("id", id)
				.single();

		session.close();

		return user;
	}

	public User getUserByUUID(UUID uid) {
		DatabaseSession session = dbc.getMSSqlSession();

		User user = (User) session
				.getNamedQuery("getUserByUUID")
				.setParameter("uid", uid)
				.single();

		session.close();

		return user;
	}

	public User addOrUpdateUserLocation(UUID uid, double ulat, double ulon) {
		double glat = Math.floor(ulat * 10000) / 10000;
		double glon = Math.floor(ulon * 10000) / 10000;
		
		DatabaseSession session = dbc.getMSSqlSession();

		// Create the new group for the user to be placed in
		UserLocationGroup newGroup = (UserLocationGroup) session
				.getNamedQuery("getUserLocationGroupByLatLon")
				.setParameter("lat", glat)
				.setParameter("lon", glon)
				.single();
		
		if (newGroup == null) {
			// Group didn't exist, so create it
			System.out.println("Did not find a group for lat: " + glat + " lon: " + glon);
	
			newGroup = new UserLocationGroup();
			newGroup.setLat(glat);
			newGroup.setLon(glon);
			newGroup.setCount(0);
	
			session.persist(newGroup);
		}
		
		User user = (User) session
				.getNamedQuery("getUserByUUID")
				.setParameter("uid", uid)
				.single();

		// Check to see if the user is already in the database
		if (user != null) {
			// This person already exists
			System.out.println("Person with GUID: " + uid + " already exists");

			UserLocationGroup prevGroup = user.getUserLocationGroup();

			// Update the location
			user.setLat(ulat);
			user.setLon(ulon);
			
			// If the user moved to a new group, update the counts
			if (prevGroup.getId() != newGroup.getId()) {
				prevGroup.decrementCount();
				newGroup.incrementCount();
				
				user.setUserLocationGroup(newGroup);
			}
		} else {
			// This person didn't exist, add them
			System.out.println("Adding new person with GUID: " + uid);

			// Create the user
			user = new User();
			user.setLat(ulat);
			user.setLon(ulon);
			user.setUid(uid);
			user.setUserLocationGroup(newGroup);

			// Add the user to the group location count
			newGroup.incrementCount();

			// Add it to the database
			session.persist(user);
		}
		
		session.close();

		return user;
	}
	/* User */

	/* User Location Group */
	public List<UserLocationGroup> getAllUserLocationGroups() {
		DatabaseSession session = dbc.getMSSqlSession();
		
		List<UserLocationGroup> groups = session
				.getNamedQuery("getAllUserLocationGroups")
				.list();
		
		session.close();

		return groups;
	}

	public UserLocationGroup getUserLocationGroupById(long id) {
		DatabaseSession session = dbc.getMSSqlSession();
		
		UserLocationGroup group = (UserLocationGroup) session
				.getNamedQuery("getUserLocationGroupById")
				.setParameter("id", id)
				.single();
		
		session.close();

		return group;
	}

	/**
	 * Gets the UserLocationGroup for the specified Latitude/Longitude or
	 * creates a new one if it doesn't exist.
	 * 
	 * @param lat
	 * @param lon
	 * @return
	 */
	public UserLocationGroup getUserLocationGroupByLatLon(double lat, double lon) {
		double glat = Math.floor(lat * 10000) / 10000;
		double glon = Math.floor(lon * 10000) / 10000;

		DatabaseSession session = dbc.getMSSqlSession();
		
		UserLocationGroup group = (UserLocationGroup) session
				.getNamedQuery("getUserLocationGroupByLatLon")
				.setParameter("lat", glat)
				.setParameter("lon", glon)
				.single();
			
		if (group == null) {
			// Didn't exist, so create it
			System.out.println("Did not find a group for lat: " + glat + " lon: " + glon);

			group = new UserLocationGroup();
			group.setLat(lat);
			group.setLon(lon);
			group.setCount(0);

			session.persist(group);
		}

		session.close();

		return group;
	}
	/* User Location Group */

	/* User Route */
	public List<UserRoute> getAllUserRoutes() {
		DatabaseSession session = dbc.getMSSqlSession();
		
		List<UserRoute> routes = session
				.getNamedQuery("getAllUserRoutes")
				.list();
		
		session.close();

		return routes;
	}

	public UserRoute getUserRouteById(long id) {
		DatabaseSession session = dbc.getMSSqlSession();
		
		UserRoute route = (UserRoute) session
				.getNamedQuery("getUserRouteById6")
				.setParameter("id", id)
				.single();
		
		session.close();

		return route;
	}
	
	public UserRoute updateLastVisitedNode(UUID uid, long nodeId) {
		DatabaseSession session = dbc.getMSSqlSession();

		UserRoute route = null;
		
		User user = (User) session
				.getNamedQuery("getUserByUUID")
				.setParameter("uid", uid)
				.single();
		
		if (user != null) {
			route = user.getRoute();
			
			Node node = (Node) session
					.getNamedQuery("getNodeById")
					.setParameter("id", nodeId)
					.single();
			
			if (route != null && node != null) {
				route.setLastVisitedNode(node);
			}
		}

		session.close();

		return route;
	}
	
	public void updateRoute(long id, String route) {
		DatabaseSession session = dbc.getPostgresSession();
		
		UserRoute r = session
				.getNamedQuery("getUserRouteById")
				.setParameter("id", id)
				.single();
		
		r.setRoute(route);
		
		session.close();
	}
	/* User Route */

	/* Way */
	public List<Way> getAllWays() {
		DatabaseSession session = dbc.getPostgresSession();
		
		List<Way> ret = session
				.getNamedQuery("getAllWays")
				.list();
		
		session.close();

		return ret;
	}

	public Way getWayById(long id) {
		DatabaseSession session = dbc.getPostgresSession();
		
		Way way = (Way) session
				.getNamedQuery("getWayById")
				.setParameter("id", id)
				.single();
		
		session.close();

		return way;
	}

	public boolean closeWay(long id) {
		DatabaseSession session = dbc.getPostgresSession();
		
		Way way = (Way) session
				.getNamedQuery("getWayById")
				.setParameter("id", id)
				.single();
		
		if (way != null) {
			way.getTags().put("closed", Boolean.TRUE.toString());
		}
		
		session.close();
		
		return way != null && way.getTags().get("closed") != null && Boolean.parseBoolean(way.getTags().get("closed"));
	}

	public boolean openWay(long id) {
		DatabaseSession session = dbc.getPostgresSession();
		
		Way way = (Way) session
				.getNamedQuery("getWayById")
				.setParameter("id", id)
				.single();

		if (way != null) {
			way.getTags().put("closed", Boolean.FALSE.toString());
		}

		session.close();

		return way != null && way.getTags().get("closed") != null && Boolean.parseBoolean(way.getTags().get("closed"));
	}
	/* Way */

	/**
	 * Persists an object to the correct backing database.
	 * 
	 * @param objects
	 *            The object to persist.
	 * @return True, if the object was persisted.
	 */
	public boolean persist(Object... objects) {
		Object o = new Object();
		
		if (objects.length > 0) {
			o = objects[0];
		}
		
		if (o instanceof Node || o instanceof Way) {
			DatabaseSession session = dbc.getPostgresSession();
			
			System.out.println("Persiting: " + objects.length);
			
			Arrays.asList(objects).stream().forEach((Object no) -> session.persist(no));
			
			session.close();
		} else if (o instanceof User || o instanceof UserLocationGroup || o instanceof UserRoute) {
			DatabaseSession session = dbc.getMSSqlSession();
			
			Arrays.asList(objects).stream().forEach((Object no) -> session.persist(no));

			session.close();
		} else {
			return false;
		}

		return true;
	}
	
	public boolean delete(Object... objects) {
		Object o = new Object();
		
		if (objects.length > 0) {
			o = objects[0];
		}
		
		if (o instanceof Node || o instanceof Way) {
			DatabaseSession session = dbc.getPostgresSession();

			System.out.println("Deleting: " + objects.length);
			
			Arrays.asList(objects).stream().forEach((Object no) -> session.getSession().delete(no));
			
			session.close();
		} else if (o instanceof User || o instanceof UserLocationGroup || o instanceof UserRoute) {
			DatabaseSession session = dbc.getMSSqlSession();
			
			Arrays.asList(objects).stream().forEach((Object no) -> session.getSession().delete(no));

			session.close();
		} else {
			return false;
		}

		return true;
	}
}
