package com.smart.controller;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.smart.dao.UserRepository;
import com.smart.entities.User;
import com.smart.helper.Message;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

@Controller
public class HomeController {
    
	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	private BCryptPasswordEncoder passwordEnconder;
	
	
	@RequestMapping("/")
	public String home(Model model)
	{   
		model.addAttribute("title", "Home - Smart Contact Manager");
		
		return "home";
	}
	
	@RequestMapping("/about")
	public String about(Model model)
	{   
		model.addAttribute("title", "about - Smart Contact Manager");
		return "about";
	}
	
	@RequestMapping("/signup")
	public String signup(Model model)
	{   
		model.addAttribute("title", "signup - Smart Contact Manager");
		model.addAttribute("user",new User());
		return "signup";
	}
	
	@RequestMapping(value= "/do_register" , method=RequestMethod.POST)
	public String registerUser(@Valid @ModelAttribute("user") User user ,BindingResult bResult, @RequestParam(value="agreement",
	defaultValue="false") Boolean agreement,Model model,HttpSession session)
	{   
		try {
			if(!agreement)
			{
			  System.out.println("Please agree the terms and conditions");
			  throw new Exception("Please agree the terms and conditions");
			}
			
			if(bResult.hasErrors())
				
			{   
				System.out.println("ERROR"+bResult.toString());
				model.addAttribute("user",user);
				return "signup";
			}
				
			
			user.setPassword(passwordEnconder.encode(user.getPassword()));
			
			user.setRole("ROLE_USER");
			user.setEnabled(true);
			user.setImageUrl("default.jpg");
			
			System.out.println("agreement"+agreement);
			System.out.println("user"+user);
			
			User result = this.userRepository.save(user);
			model.addAttribute("user",new User());
			
			session.setAttribute("message", new Message("Successfully registered ","alert-success"));
            return "signup";
			
			
		}
		catch(Exception e)
		   
		{    e.printStackTrace();
			model.addAttribute("user",user);
			session.setAttribute("message", new Message("Something went wrong "+e.getMessage(),"alert-danger"));
						
			return "signup";
		}
		
		
		
	}
	
	//login
	@RequestMapping("/signin")
    public String customLogin(Model model)
    {
  	  return "login";
    }
}
