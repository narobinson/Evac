package CS472.urbanevac.db;

import java.util.List;
import java.util.UUID;

import javax.annotation.PostConstruct;
import javax.persistence.NoResultException;

import org.hibernate.Session;
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
		Session session = dbc.getPostgresSession().openSession();
		session.beginTransaction();

		List<Node> nodes = session.createQuery("SELECT n FROM Node n").list();

		session.getTransaction().commit();
		session.close();

		return nodes;
	}

	public Node getNodeById(long id) {
		Session session = dbc.getPostgresSession().openSession();
		session.beginTransaction();

		Node node = null;

		try {
			node = (Node) session.createQuery("SELECT n FROM Node n WHERE n.id = :id").setParameter("id", id)
					.getSingleResult();
		} catch (NoResultException e) {
			// Do nothing, node is null
		}

		session.getTransaction().commit();
		session.close();

		return node;
	}

	public List<Node> getNodesByIds(List<Long> ids) {
		Session session = dbc.getPostgresSession().openSession();
		session.beginTransaction();

		List<Node> nodes = session.createQuery("SELECT n FROM Node n WHERE n.id IN :idList")
				.setParameterList("idList", ids).list();

		session.getTransaction().commit();
		session.close();

		return nodes;
	}
	/* Node */

	/* User */
	public List<User> getAllUsers() {
		Session session = dbc.getMSSqlSession().openSession();
		session.beginTransaction();

		List<User> users = session.createQuery("SELECT u FROM User u").list();

		session.getTransaction().commit();
		session.close();
		
		return users;
	}

	public User getUserById(long id) {
		Session session = dbc.getMSSqlSession().openSession();
		session.beginTransaction();

		User user = null;

		try {
			user = (User) session.createQuery("SELECT u FROM User u WHERE u.id = :id").setParameter("id", id)
					.getSingleResult();
		} catch (NoResultException e) {
			// Do nothing, user is null
		}

		session.getTransaction().commit();
		session.close();

		return user;
	}

	public User getUserByUUID(UUID uid) {
		Session session = dbc.getMSSqlSession().openSession();
		session.beginTransaction();

		User user = null;

		try {
			user = (User) session.createQuery("SELECT u FROM User u WHERE u.uid = :uid").setParameter("uid", uid)
					.getSingleResult();
		} catch (NoResultException e) {
			// Do nothing, user is null
		}

		session.getTransaction().commit();
		session.close();

		return user;
	}

	public User addOrUpdateUserLocation(UUID uid, double ulat, double ulon) {
		double glat = Math.floor(ulat * 10000) / 10000;
		double glon = Math.floor(ulon * 10000) / 10000;
		
		Session session = dbc.getMSSqlSession().openSession();
		session.beginTransaction();

		User user = null;
		UserLocationGroup newGroup = null;
		UserLocationGroup prevGroup = null;
		
		// Check to see if the user is already in the database
		try {
			user = (User) session.createQuery("SELECT u FROM User u WHERE u.uid = :uid")
				.setParameter("uid", uid).getSingleResult();
			
			// This person already exists
			System.out.println("Person with GUID: " + uid + " already exists");

			// getUserLocationGroupByLatLon
			newGroup = null;
			prevGroup = user.getUserLocationGroup();
			
			try {
				newGroup = (UserLocationGroup) session
						.createQuery("SELECT u FROM UserLocationGroup u WHERE u.lat = :lat AND u.lon = :lon")
						.setParameter("lat", glat).setParameter("lon", glon).getSingleResult();
			} catch (NoResultException e) {
				// Didn't exist, so create it
				System.out.println("Did not find a group for lat: " + glat + " lon: " + glon);

				newGroup = new UserLocationGroup();
				newGroup.setLat(glat);
				newGroup.setLon(glon);
				newGroup.setCount(0);

				session.persist(newGroup);
			}

			// Update the location
			user.setLat(ulat);
			user.setLon(ulon);
			
			// If the user moved to a new group, update the counts
			if (prevGroup.getId() != newGroup.getId()) {
				prevGroup.decrementCount();
				newGroup.incrementCount();
				
				user.setUserLocationGroup(newGroup);
			}
		} catch (NoResultException e) {
			// This person didn't exist, add them
			System.out.println("Adding new person with GUID: " + uid);

			try {
				newGroup = (UserLocationGroup) session
						.createQuery("SELECT u FROM UserLocationGroup u WHERE u.lat = :lat AND u.lon = :lon")
						.setParameter("lat", glat).setParameter("lon", glon).getSingleResult();
			} catch (NoResultException e1) {
				// Didn't exist, so create it
				System.out.println("Did not find a group for lat: " + glat + " lon: " + glon);

				newGroup = new UserLocationGroup();
				newGroup.setLat(glat);
				newGroup.setLon(glon);
				newGroup.setCount(0);

				session.persist(newGroup);
			}

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
		
		session.getTransaction().commit();
		session.close();

		return user;
	}
	/* User */

	/* User Location Group */
	public List<UserLocationGroup> getAllUserLocationGroups() {
		Session session = dbc.getMSSqlSession().openSession();
		session.beginTransaction();
		
		List<UserLocationGroup> groups = session
				.createQuery("SELECT g FROM UserLocationGroup g").list();
		
		session.getTransaction().commit();
		session.close();

		return groups;
	}

	public UserLocationGroup getUserLocationGroupById(long id) {
		Session session = dbc.getMSSqlSession().openSession();
		session.beginTransaction();
		
		UserLocationGroup group = null;
		
		try {
			group = (UserLocationGroup) session
					.createQuery("SELECT u FROM UserLocationGroup u WHERE u.id = :id").setParameter("id", id)
					.getSingleResult();
		} catch (NoResultException e) {
			// Do nothing, group is null
		}
		
		session.getTransaction().commit();
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

		Session session = dbc.getMSSqlSession().openSession();
		session.beginTransaction();
		
		UserLocationGroup group = null;

		try {
			group = (UserLocationGroup) session
					.createQuery("SELECT u FROM UserLocationGroup u WHERE u.lat = :lat AND u.lon = :lon")
					.setParameter("lat", glat).setParameter("lon", glon).getSingleResult();
		} catch (NoResultException e) {
			// Didn't exist, so create it
			System.out.println("Did not find a group for lat: " + glat + " lon: " + glon);

			group = new UserLocationGroup();
			group.setLat(lat);
			group.setLon(lon);
			group.setCount(0);

			session.persist(group);
		}

		session.getTransaction().commit();
		session.close();

		return group;
	}
	/* User Location Group */

	/* User Route */
	public List<UserRoute> getAllUserRoutes() {
		Session session = dbc.getMSSqlSession().openSession();
		session.beginTransaction();
		
		List<UserRoute> routes = session.createQuery("SELECT r FROM UserRoute r").list();
		
		session.getTransaction().commit();
		session.close();

		return routes;
	}

	public UserRoute getUserRouteById(long id) {
		Session session = dbc.getMSSqlSession().openSession();
		session.beginTransaction();
		
		UserRoute route = null;
		
		try {
			route = (UserRoute) session.createQuery("SELECT u FROM UserRoute u WHERE u.id = :id")
					.setParameter("id", id).getSingleResult();
		} catch (NoResultException e) {
			// Do nothing, route is null
		}
		
		session.getTransaction().commit();
		session.close();

		return route;
	}
	
	public UserRoute updateLastVisitedNode(UUID uid, long nodeId) {
		Session session = dbc.getMSSqlSession().openSession();
		session.beginTransaction();

		UserRoute route = null;

		try {
			User user = (User) session.createQuery("SELECT u FROM User u WHERE u.uid = :uid").setParameter("uid", uid)
					.getSingleResult();
			
			route = user.getRoute();
			
			Node node = (Node) session.createQuery("SELECT n FROM Node n WHERE n.id = :id").setParameter("id", nodeId)
					.getSingleResult();
			
			if (route != null) {
				route.setLastVisitedNode(node);
			}
		} catch (NoResultException e) {
			// Do nothing, user is null
		}

		session.getTransaction().commit();
		session.close();

		return route;
	}
	/* User Route */

	/* Way */
	public List<Way> getAllWays() {
		Session session = dbc.getPostgresSession().openSession();
		session.beginTransaction();
		
		List<Way> ret = session.createQuery("SELECT w FROM Way w").list();
		
		session.getTransaction().commit();
		session.close();

		return ret;
	}

	public Way getWayById(long id) {
		Session session = dbc.getPostgresSession().openSession();
		session.beginTransaction();
		
		Way way = null;
		
		try {
			way = (Way) session.createQuery("SELECT w FROM Way w WHERE w.id = :id").setParameter("id", id)
					.getSingleResult();
		} catch (NoResultException e) {
			// Do nothing, way is null
		}
			
		session.getTransaction().commit();
		session.close();

		return way;
	}

	public boolean closeWay(long id) {
		Session session = dbc.getPostgresSession().openSession();
		session.beginTransaction();
		
		Way way = null;
		
		try {
			way = (Way) session.createQuery("SELECT w FROM Way w WHERE w.id = :id").setParameter("id", id)
					.getSingleResult();

			way.getTags().put("closed", Boolean.TRUE.toString());
		} catch (NoResultException e) {
			// Do nothing, way is null
		}
		
		session.getTransaction().commit();
		session.close();
		
		return way != null && way.getTags().get("closed") != null && Boolean.parseBoolean(way.getTags().get("closed"));
	}

	public boolean openWay(long id) {
		Session session = dbc.getPostgresSession().openSession();
		session.beginTransaction();
		
		Way way = null;
		
		try {
			way = (Way) session.createQuery("SELECT w FROM Way w WHERE w.id = :id").setParameter("id", id)
					.getSingleResult();
			
			way.getTags().put("closed", Boolean.FALSE.toString());
		} catch (NoResultException e) {
			// Do nothing, way is null
		}

		session.getTransaction().commit();
		session.close();

		return way != null && way.getTags().get("closed") != null && Boolean.parseBoolean(way.getTags().get("closed"));
	}
	/* Way */

	/**
	 * Persists an object to the correct backing database.
	 * 
	 * @param o
	 *            The object to persist.
	 * @return True, if the object was persisted.
	 */
	public boolean persist(Object o) {
		if (o instanceof Node || o instanceof Way) {
			Session session = dbc.getPostgresSession().openSession();
			session.beginTransaction();
			session.persist(o);
			session.getTransaction().commit();
			session.close();
		} else if (o instanceof User || o instanceof UserLocationGroup || o instanceof UserRoute) {
			Session session = dbc.getMSSqlSession().openSession();
			session.beginTransaction();
			session.persist(o);
			session.getTransaction().commit();
			session.close();
		} else {
			return false;
		}

		return true;
	}
}
