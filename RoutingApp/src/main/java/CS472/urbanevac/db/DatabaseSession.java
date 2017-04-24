package CS472.urbanevac.db;

import org.hibernate.Session;

public class DatabaseSession {

	private Session session;
	
	public DatabaseSession(Session session) {
		this.session = session;
		this.session.beginTransaction();
	}
	
	public void close() {
		this.session.getTransaction().commit();
		this.session.close();
	}
	
	public DatabaseQuery getNamedQuery(String name) {
		return new DatabaseQuery(this.session.getNamedQuery(name));
	}
	
	public void persist(Object o) {
		this.session.persist(o);
	}
	
	public Session getSession() {
		return this.session;
	}
}
