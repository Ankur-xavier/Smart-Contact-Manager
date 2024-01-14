package com.smart.controller;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.smart.dao.ContactRepository;
import com.smart.dao.UserRepository;
import com.smart.entities.Contact;
import com.smart.entities.User;
import com.smart.helper.Message;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/user")
public class UserController {
     
	@Autowired
	private UserRepository userRepository;
	@Autowired
	private ContactRepository contactRepository;
	
	//method for adding common data in response
	@ModelAttribute
	private void addCommonData(Model model,Principal principal)
	{
		
		String username= principal.getName();
		System.out.println("USERNAME :" +username);
		
		
		User user =userRepository.getUserByUserName(username);
		model.addAttribute("user", user);
		
	}
	
	//user Dashboard home
	@RequestMapping("/index")
	public String userDashboard(Model model,Principal principal) {
		
		
		model.addAttribute("title", "User Dashboard");
		return "normal/userDashboard";
	}
	
	//adding contact
	@RequestMapping("/add_contact")
	public String addContact(Model model)
	{   
		model.addAttribute("title", "Add Contact");
		model.addAttribute("contact", new Contact());
		
		return "normal/addContact";
	}
	//processing add contact form
	@PostMapping("/process_contact")
	public String processContact(@ModelAttribute Contact contact,
			@RequestParam("profileImage") MultipartFile multiPartFile,
			Principal principal,HttpSession session)
	{   
		try {
		String name=principal.getName();
		User user =this.userRepository.getUserByUserName(name);
		
		if(multiPartFile.isEmpty()) {
			//if file is empty
			System.out.println("file is Empty");
			contact.setImage("Contact.png");
		
		}
		else 
		{	
			String[] split = contact.getEmail().split("@");
			contact.setImage(split[0]+"_"+multiPartFile.getOriginalFilename());
			File fileObject = new ClassPathResource("static/img").getFile();
			Path path = Paths.get(fileObject.getAbsolutePath()+File.separator+split[0]+"_"+multiPartFile.getOriginalFilename());	 
			Files.copy(multiPartFile.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
			System.out.println("Image is uploaded");
		}

		contact.setUser(user);
		user.getContacts().add(contact);
		this.userRepository.save(user);
		System.out.println(user);
		
		session.setAttribute("message", new Message("Successfully added contact","alert-success"));
		}
		catch(Exception e){
			e.printStackTrace();
			session.setAttribute("message", new Message("Error occurred while adding contact","alert-danger"));
		}
		
		return "normal/addContact";
	}
	
	@GetMapping("/view_contact/{page}")
	public String viewContact(@PathVariable("page") Integer page ,Model model,Principal principal)
	{   
		model.addAttribute("title", "Show Contacts");
		String name = principal.getName();
		User user = this.userRepository.getUserByUserName(name);
		
		
		Pageable pageable = PageRequest.of(page, 5);
		Page<Contact> contacts =  this.contactRepository.findContactsByUser(user.getId(), pageable);
		model.addAttribute("contacts", contacts);
		model.addAttribute("currentPage", page);
		model.addAttribute("totalPages", contacts.getTotalPages());
		
		return "normal/viewContact";
				
	}
	@RequestMapping(value = "/{cId}/contact")
	public String showContactDetail(@PathVariable("cId") Integer cid, Model model, Principal principal) {
	    model.addAttribute("title", "Contact Details");
	    System.out.println("Cid : " + cid);
	    Optional<Contact> optional = this.contactRepository.findById(cid);

	    if (optional.isPresent()) {
	        Contact contact = optional.get();
	        String name = principal.getName();
	        User user = this.userRepository.getUserByUserName(name);

	        if (user.getId() == contact.getUser().getId()) {
	            model.addAttribute("contact", contact);
	        }
	    } else {
	        // Add an attribute to indicate that no contact was found
	        model.addAttribute("contactNotFound", true);
	    }

	    return "normal/contact_detail";
	}

	//delete Contact handler
		@RequestMapping(value = "/delete/{cId}")
		public String deleteContact(@PathVariable("cId") Integer cid, Model model, Principal principal, HttpSession session) {
			Optional<Contact> contactOptional = this.contactRepository.findById(cid);
			Contact contact = contactOptional.get();
			
			String name = principal.getName();
			User user = this.userRepository.getUserByUserName(name);
			if(user.getId()==contact.getUser().getId()) {

				user.getContacts().remove(contact);

				this.contactRepository.deleteById(cid);
				session.setAttribute("message", new Message("Contact deleted Successfully...", "alert-success"));
			}
			return "redirect:/user/view_contact/0";
		}
		
		
		@PostMapping("/update-contact/{cId}")
		public String updateForm(@PathVariable("cId") Integer id, Model model) {
			model.addAttribute("title", "Update Contact");
			Optional<Contact> optional = this.contactRepository.findById(id);
			Contact contact = optional.get();
			model.addAttribute("contact", contact);
			return "normal/update_form";
		}
		
		//update Contact handler
		@RequestMapping(value = "/process-update" , method = RequestMethod.POST)
		public String updateHandler(@ModelAttribute Contact contact, Model model, HttpSession session, 
				@RequestParam("profileImage") MultipartFile multiPartFile, Principal principal) {
			try {
				//old Contact details
					Contact oldContactDetail = this.contactRepository.findById(contact.getcId()).get();
				if(!multiPartFile.isEmpty()) {
					String[] split = contact.getEmail().split("@");
					//file is not empty so update the file
					//delete old photo
					File deleteFile = new ClassPathResource("static/img").getFile();
					File file = new File(deleteFile, oldContactDetail.getImage());
					file.delete();
					
					//update new photo
					File fileObject = new ClassPathResource("static/img").getFile();
					Path path = Paths.get(fileObject.getAbsolutePath()+File.separator+split[0]+"_"+multiPartFile.getOriginalFilename());
					Files.copy(multiPartFile.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
					contact.setImage(split[0]+"_"+multiPartFile.getOriginalFilename());
				
				}
				else
				{
					//file is empty
					contact.setImage(oldContactDetail.getImage());
				}
				User user = this.userRepository.getUserByUserName(principal.getName());
				contact.setUser(user);
				this.contactRepository.save(contact);
				session.setAttribute("message",new Message("Your Contact is updated.......","alert-success"));
				System.out.println("Id :"+contact.getcId());
			}catch (Exception e) {
				e.printStackTrace();
			}
			return "redirect:/user/"+contact.getcId()+"/contact";
		}
		
		
		//profile page handler
		@GetMapping("/profile")
		public String yourProfilePage(Model model) {
			model.addAttribute("title", "Profile Page");
			return "normal/profile";
		}
}
