package com.niit.collaboration.Controller;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.niit.collaboration.dao.FriendsDAO;
import com.niit.collaboration.dao.UserDAO;
import com.niit.collaboration.model.User;
import com.niit.collaboration.util.FileUtil;

@RestController
public class UserController {

    private Path path;
	
    private static final Logger Logger = LoggerFactory.getLogger(UserController.class);
	
	@Autowired
	UserDAO userDAO;
	
	@Autowired
	User user;
	
	@Autowired
	FriendsDAO friendsDAO;
	
	@RequestMapping(value="/users",method = RequestMethod.GET)
	public ResponseEntity<List<User>> listAllUsers()
	{
		Logger.debug("->->->calling method listAllUsers");
		List<User> user = userDAO.list();
		
		if(user.isEmpty()) {
			return new ResponseEntity<List<User>>(HttpStatus.NO_CONTENT);
		}
		return new ResponseEntity<List<User>>(user, HttpStatus.OK);
	}
	
   @RequestMapping(value = "/user/" , method = RequestMethod.POST)
   public ResponseEntity<User> createuser (@RequestBody User user, HttpServletRequest request)
   {
	   Logger.debug("->->-> calling method createUser");
	   if(userDAO.get(user.getId()) == null)
	   {
		   user.setIsOnline('N');
		   user.setStatus('N');
		   userDAO.save(user);
		   user.setErrorCode("200");
		   user.setErrorMessage("Thank You for registration .The operation are completed");
		   return new ResponseEntity<User>(user, HttpStatus.OK);
	   }
	  
	   MultipartFile file=user.getImage();
		/**/
		String rootDirectory=request.getSession().getServletContext().getRealPath("/");
		/*path=Paths.get(rootDirectory + "\\WEB-INF\\resources\\images\\"+product.getPro_id()+".jpg");*/
		path = Paths.get(rootDirectory + "\\resources\\image\\" + user.getId() + ".jpg");
		
		if(file!=null && !file.isEmpty()){
			try{
				file.transferTo(new File(path.toString()));
				System.out.println("Image Uploaded to Product.....");
			}catch(Exception e)
			{
				e.printStackTrace();
				throw new RuntimeException("image saving failed ",e);
			}
		}
		FileUtil.upload(path.toString(), file, user.getId() + ".jpg");
	   user.setErrorCode("404");
	   user.setErrorMessage("User already exist with id : " +user.getId());
	   return new ResponseEntity<User>(user, HttpStatus.OK);
   }
	
   @RequestMapping(value = "/user/{id}" , method = RequestMethod.PUT)
   public ResponseEntity<User> updateuser (@PathVariable("id") String id, @RequestBody User user)
   {
	   Logger.debug("->->-> calling method UpdateUser");
	   if(userDAO.get(id) == null)
	   { 
		   Logger.debug("->->->->User does not exist with id "+ user.getId());
		   user = new User();
		   user.setErrorMessage("User does not exist with id "+ user.getId());
		   return new ResponseEntity<User>(user, HttpStatus.NOT_FOUND);
	   }
	   userDAO.update(user);
	   return new ResponseEntity<User>(user, HttpStatus.OK);
   }
   
   @RequestMapping(value = "/user/{id}" , method = RequestMethod.DELETE)
   public ResponseEntity<User> deleteuser (@PathVariable("id") String id, @RequestBody User user)
   {
	   Logger.debug("->->-> calling method deleteUser");
	   if(userDAO.get(id) == null)
	   { 
		   Logger.debug("->->->->User does not exist with id " +id);
		   user = new User();
		   user.setErrorMessage("User does not exist with id ");
		   return new ResponseEntity<User>(user, HttpStatus.NOT_FOUND);
	   }
	   userDAO.delete(id);
	   Logger.debug("->->->User Deleted Successfully");
	   return new ResponseEntity<User>(user, HttpStatus.OK); 
   }
   

   @RequestMapping(value="/user/{id}",method = RequestMethod.GET)
  	public ResponseEntity<User> getuser(@PathVariable("id") String id)
  	{
  	   Logger.debug("->->-> calling method getUser");
  	   Logger.debug("->->->->"+id);
  	   User user = userDAO.get(id);
  	   if(userDAO.get(id) == null)
  	   { 
  		   Logger.debug("->->->->User does not exist with id " +id);
  		   user = new User();
  		   user.setErrorMessage("User does not exist with id ");
  		   return new ResponseEntity<User>(user, HttpStatus.NOT_FOUND);
  	   }
  	   Logger.debug("->->->->User exist with id " +id);
  	   return new ResponseEntity<User>(user, HttpStatus.OK); 
  	}
   
   @RequestMapping(value = "/user/authenticate/", method = RequestMethod.POST)
   public ResponseEntity<User> authenticate(@RequestBody User user, HttpSession session)
   {
	   Logger.debug("->->-> calling method createUser");
	   user = userDAO.authenticate(user.getName(), user.getPassword());
	   if(user==null)
	   {
		   user = new User();
		   user.setErrorMessage("Invalid Credentials. Please Enter valid credentials");
	   }
	   else
	   {
		   Logger.debug("->->->User exist with given credentials");
		   session.setAttribute("loggedInUser", user);
		   session.setAttribute("loggedInUserID", user.getId());
		   
	   }
	   return new ResponseEntity<User>(user, HttpStatus.OK); 
   }
   
   @RequestMapping(value="/user/logout" , method = RequestMethod.POST)
   public String logout(HttpSession session) {
	  Logger.debug("->->->->calling method logout");
	  String loggedInUserID = (String) session.getAttribute("loggedInUserID");
	 
	  friendsDAO.setOffLine(loggedInUserID);
	  userDAO.setOffLine(loggedInUserID);
	  
	  session.invalidate();
	  
	  return ("You successfully loggedout");
   }
   
   @RequestMapping(value="/useraccept/{id}" , method = RequestMethod.PUT)
   public ResponseEntity<User> useraccept(@PathVariable("id") String id , @RequestBody User user)
   {
	   user = userDAO.get(user.getId());
	   if(user==null)
	   {
		   Logger.debug("->->->User does not exist with id"+ user.getId());
		   user = new User();
		   user.setErrorMessage("User does not exist with id"+ user.getId());
		   return new ResponseEntity<User>(user, HttpStatus.NOT_FOUND);
	   }
	   user.setStatus('A');
	   userDAO.update(user);
	   
	   return new ResponseEntity<User>(user, HttpStatus.OK);
   }
   
   @RequestMapping(value="/userreject/{id}" , method = RequestMethod.PUT)
   public ResponseEntity<User> rejectUser(@PathVariable("id") String id , @RequestBody User user)
   {
	   user = userDAO.get(user.getId());
	   if(user==null)
	   {
		   Logger.debug("->->->User does not exist with id"+ user.getId());
		   user = new User();
		   user.setErrorMessage("User does not exist with id"+ user.getId());
		   return new ResponseEntity<User>(user, HttpStatus.NOT_FOUND);
	   }
	   user.setStatus('R');
	   userDAO.update(user);
	
		   return new ResponseEntity<User>(user, HttpStatus.OK);
   }
   
   @RequestMapping(value="/user/makeadmin/{id}" , method = RequestMethod.POST)
   public  ResponseEntity<User> makeadmin(@PathVariable("id") String id,@RequestBody User user) 
   {
	   user = userDAO.get(id);
	   user.setRole("admin");
	   
	   userDAO.update(user);
	   
	   return new ResponseEntity<User>(user, HttpStatus.OK);
   }

   
}
