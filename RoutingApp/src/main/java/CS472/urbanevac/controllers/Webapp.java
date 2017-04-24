package CS472.urbanevac.controllers;

import java.util.UUID;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
@RequestMapping("/app")
public class Webapp {
	@RequestMapping("/")
	public ModelAndView index() {
		ModelAndView mav = new ModelAndView("index");
		
		return mav;
	}
	
	@RequestMapping("/{uid}")
	public ModelAndView indexWithRoute(UUID uid) {
		ModelAndView mav = new ModelAndView("index");
		
		
		
		return mav;
	}
}
