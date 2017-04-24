package CS472.urbanevac.db;

import java.util.Collection;
import java.util.List;

import javax.persistence.NoResultException;

import org.hibernate.query.Query;

@SuppressWarnings({"rawtypes", "unchecked"})
public class DatabaseQuery {
	private Query query;
	
	public DatabaseQuery(Query query) {
		this.query = query;
	}
	
	public DatabaseQuery setParameter(String name, Object value) {
		this.query.setParameter(name, value);
		
		return this;
	}
	
	public DatabaseQuery setParameterList(String name, Collection values) {
		this.query.setParameterList(name, values);
		
		return this;
	}
	
	public List list() {
		return this.query.list();
	}
	
	public <T> T single() {
		try {
			return (T) this.query.getSingleResult();
		} catch (NoResultException e) {
			return null;
		}
	}
	
	public Query getQuery() {
		return this.query;
	}
}
