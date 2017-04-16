package CS472.urbanevac.controllers;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import CS472.urbanevac.db.Database;
import CS472.urbanevac.db.tables.UserRoute;

@RestController
@RequestMapping("/route")
public class Route {
	@Autowired
	private Database db;
	
	@RequestMapping("/close/{id}")
	public @ResponseBody boolean closeRoad(@PathVariable long id) {
		boolean closed = db.closeWay(id);
		
		// TODO: Recalculate affected routes here!!!
		
		return closed;
	}
	
	@RequestMapping("/open/{id}")
	public @ResponseBody boolean openRoad(@PathVariable long id) {
		return db.openWay(id);
	}
	
	@RequestMapping("/{uid}")
	public @ResponseBody UserRoute getRoute(@PathVariable UUID uid) {
		UserRoute route = db.getUserByUUID(uid).getRoute();
		
		// TODO: Calculate the route if needed!!!
		if (route == null) {
			
		}
		
		return route;
	}
}
