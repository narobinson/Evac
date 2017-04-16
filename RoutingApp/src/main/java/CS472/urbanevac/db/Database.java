package CS472.urbanevac.db;

import java.util.List;
import java.util.UUID;

import javax.annotation.PostConstruct;

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
		List<Node> nodes = (List<Node>) session.createQuery("SELECT n FROM Node n").list();
		session.getTransaction().commit();
		session.close();
		
		return nodes;
	}
	
	public Node getNodeById(long id) {
		Session session = dbc.getPostgresSession().openSession();
		session.beginTransaction();
		Node node = (Node) session.createQuery("SELECT n FROM Node n WHERE n.id = :id")
				.setParameter("id", id).getSingleResult();
		session.getTransaction().commit();
		session.close();
		
		return node;
	}
	
	public List<Node> getNodesByIds(List<Long> ids) {
		Session session = dbc.getPostgresSession().openSession();
		session.beginTransaction();
		
		List<Node> nodes = (List<Node>) session.createQuery("SELECT n FROM Node n WHERE n.id IN :idList")
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
		List<User> users = (List<User>) session.createQuery("SELECT u FROM User u").list();
		session.getTransaction().commit();
		session.close();
		
		return users;
	}
	
	public User getUserById(long id) {
		Session session = dbc.getMSSqlSession().openSession();
		session.beginTransaction();
		User user = (User) session.createQuery("SELECT u FROM User u WHERE u.id = :id")
				.setParameter("id", id).getSingleResult();
		session.getTransaction().commit();
		session.close();
		
		return user;
	}
	
	public User getUserByUUID(UUID uid) {
		Session session = dbc.getMSSqlSession().openSession();
		session.beginTransaction();
		User user = (User) session.createQuery("SELECT u FROM User u WHERE u.uid = :uid")
				.setParameter("uid", uid).getSingleResult();
		session.getTransaction().commit();
		session.close();
		
		return user;
	}
	/* User */
	
	/* User Location Group */
	public List<UserLocationGroup> getAllUserLocationGroups() {
		Session session = dbc.getMSSqlSession().openSession();
		session.beginTransaction();
		List<UserLocationGroup> groups = (List<UserLocationGroup>) session.createQuery("SELECT g FROM UserLocationGroup g").list();
		session.getTransaction().commit();
		session.close();
		
		return groups;
	}
	
	public UserLocationGroup getUserLocationGroupById(long id) {
		Session session = dbc.getMSSqlSession().openSession();
		session.beginTransaction();
		UserLocationGroup user = (UserLocationGroup) session.createQuery("SELECT u FROM UserLocationGroup u WHERE u.id = :id")
				.setParameter("id", id).getSingleResult();
		session.getTransaction().commit();
		session.close();
		
		return user;
	}
	
	/**
	 * Gets the UserLocationGroup for the specified Latitude/Longitude or creates
	 * a new one if it doesn't exist.
	 * 
	 * @param lat
	 * @param lon
	 * @return
	 */
	public UserLocationGroup getUserLocationGroupByLatLon(double lat, double lon) {
		lat = Math.floor(lat * 10000) / 10000;
		lon = Math.floor(lon * 10000) / 10000;
		
		Session session = dbc.getMSSqlSession().openSession();
		session.beginTransaction();
		UserLocationGroup group = (UserLocationGroup) session.createQuery("SELECT u FROM UserLocationGroup u WHERE u.lat = :lat AND u.lon = :lon")
				.setParameter("lat", lat).setParameter("lon", lon).getSingleResult();

		if (group == null) {
			System.out.println("Did not find a group for lat: " + lat + " lon: " + lon);
			
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
		List<UserRoute> routes = (List<UserRoute>) session.createQuery("SELECT r FROM UserRoute r").list();
		session.getTransaction().commit();
		session.close();
		
		return routes;
	}
	
	public UserRoute getUserRouteById(long id) {
		Session session = dbc.getMSSqlSession().openSession();
		session.beginTransaction();
		UserRoute user = (UserRoute) session.createQuery("SELECT u FROM UserRoute u WHERE u.id = :id")
				.setParameter("id", id).getSingleResult();
		session.getTransaction().commit();
		session.close();
		
		return user;
	}
	/* User Route */
	
	/* Way */
	public List<Way> getAllWays() {
		Session session = dbc.getPostgresSession().openSession();
		session.beginTransaction();
		List<Way> ret = (List<Way>) session.createQuery("SELECT w FROM Way w").list();
		session.getTransaction().commit();
		session.close();
		
		return ret;
	}
	
	public Way getWayById(long id) {
		Session session = dbc.getPostgresSession().openSession();
		session.beginTransaction();
		Way way = (Way) session.createQuery("SELECT w FROM Way w WHERE w.id = :id")
				.setParameter("id", id).getSingleResult();
		session.getTransaction().commit();
		session.close();
		
		return way;
	}
	
	public boolean closeWay(long id) {
		Session session = dbc.getPostgresSession().openSession();
		session.beginTransaction();
		Way way = (Way) session.createQuery("SELECT w FROM Way w WHERE w.id = :id")
				.setParameter("id", id).getSingleResult();
		
		way.getTags().put("closed", Boolean.TRUE.toString());
		
		session.getTransaction().commit();
		session.close();
		
		return way.getTags().get("closed") != null && Boolean.parseBoolean(way.getTags().get("closed"));
	}
	
	public boolean openWay(long id) {
		Session session = dbc.getPostgresSession().openSession();
		session.beginTransaction();
		Way way = (Way) session.createQuery("SELECT w FROM Way w WHERE w.id = :id")
				.setParameter("id", id).getSingleResult();
		
		way.getTags().put("closed", Boolean.FALSE.toString());
		
		session.getTransaction().commit();
		session.close();
		
		return way.getTags().get("closed") != null && Boolean.parseBoolean(way.getTags().get("closed"));
	}
	/* Way */
	
	/**
	 * Persists an object to the correct backing database.
	 * 
	 * @param o The object to persist.
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
