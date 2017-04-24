package CS472.urbanevac.db;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.springframework.stereotype.Component;

import CS472.urbanevac.db.tables.Node;
import CS472.urbanevac.db.tables.User;
import CS472.urbanevac.db.tables.UserLocationGroup;
import CS472.urbanevac.db.tables.UserRoute;
import CS472.urbanevac.db.tables.Way;

@Component
public class DatabaseConnection {
	private SessionFactory postgresqlSessionFactory;
	private SessionFactory mssqlSessionFactory;
	
	@PostConstruct
	public void setup() {
		Configuration postgresql = new Configuration().configure("/hibernate-postgresql.cfg.xml");
		postgresql.addAnnotatedClass(Node.class);
		postgresql.addAnnotatedClass(Way.class);
		postgresql.addAnnotatedClass(User.class);
		postgresql.addAnnotatedClass(UserLocationGroup.class);
		postgresql.addAnnotatedClass(UserRoute.class);
		this.postgresqlSessionFactory = postgresql.buildSessionFactory();
		
//		Configuration mssql = new Configuration().configure("/hibernate-mssql.cfg.xml");
//		mssql.addAnnotatedClass(User.class);
//		mssql.addAnnotatedClass(UserLocationGroup.class);
//		mssql.addAnnotatedClass(UserRoute.class);
//		this.mssqlSessionFactory = mssql.buildSessionFactory();
	}
	
	@PreDestroy
	public void close() {
		this.postgresqlSessionFactory.close();
		this.mssqlSessionFactory.close();
	}
	
	public DatabaseSession getPostgresSession() {
		return new DatabaseSession(this.postgresqlSessionFactory.openSession());
	}
	
	public DatabaseSession getMSSqlSession() {
		return new DatabaseSession(this.mssqlSessionFactory.openSession());
	}
}
