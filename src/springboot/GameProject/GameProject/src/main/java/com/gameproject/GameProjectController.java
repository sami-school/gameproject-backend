package com.gameproject;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.context.annotation.ImportResource;
import com.gameproject.responses.CreateUserResponse;
import com.gameproject.responses.GetLevelResponse;
import com.gameproject.responses.GetUserResponse;
import com.gameproject.responses.LevelsResponse;
import com.gameproject.responses.LoginResponse;
import com.gameproject.responses.SessionsResponse;
import com.gameproject.responses.UsersResponse;
import com.gameproject.responses.CreateLevelResponse;
import com.gameproject.user.User;
import com.gameproject.user.UserJDBCTemplate;
import com.gameproject.session.Session;
import com.gameproject.session.SessionJDBCTemplate;
import com.gameproject.level.Level;
import com.gameproject.level.LevelJDBCTemplate;
import com.gameproject.level.LevelJSON;

import java.sql.Timestamp;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCrypt;

@RestController
@ImportResource("beans.xml")
public class GameProjectController {

	@Autowired
	private UserJDBCTemplate userJDBCTemplate;
	@Autowired
	private SessionJDBCTemplate sessionJDBCTemplate;
	@Autowired
	private LevelJDBCTemplate levelJDBCTemplate;
	

	
		@RequestMapping(value = "/ajax/")
		public String index() {
			// return simple directory listing to help with testing backend separately
			String reply = "<a href=\"/ajax/users/\">Users</a><BR><a href=\"/ajax/levels/\">Levels</a><BR><a href=\"/ajax/sessions/\">Sessions</a><BR>";
			return reply;
		}
		
		@RequestMapping(value = "/ajax/levels",method = RequestMethod.GET)
		public LevelsResponse levels() {
			 // return list of level object, exposing all the DB columns
			List<Level> levels = levelJDBCTemplate.listLevels();
			return new LevelsResponse(levels);							
		}
	
		@RequestMapping(value = "/ajax/levels/name={name}")
		public GetLevelResponse getlevel(@PathVariable("name") String name) {
			// return specified level object, exposing all the DB columns
			Level level = levelJDBCTemplate.getLevel(name);
			return new GetLevelResponse(level);
		}
		
		@RequestMapping(value = "/ajax/levels", method = RequestMethod.PUT)
		public @ResponseBody CreateLevelResponse createlevel(@RequestBody LevelJSON level){
			//  create new level, requires valid session
			// expects String name, String session, String data 
			String status = "fail";
			if(sessionJDBCTemplate.checkIfSessionIsValid(level.getSession()))
			{
				// session exists and is recent enough
				System.out.println(level.getData());
				levelJDBCTemplate.create(level);
				status  = "success";
				sessionJDBCTemplate.refresh(level.getSession());
			}
			return new CreateLevelResponse(status);
			// return is simply status of the operation
		}
		
		
		
		@RequestMapping(value = "/ajax/users/")
		public UsersResponse users() {
			 // return list of user object, exposing all the DB columns
			 // this is security issue because it exposes user names and hashed passwords...
			List<User> users = userJDBCTemplate.listUsers();
			return new UsersResponse(users);							
		}
	
		@RequestMapping(value = "/ajax/users/id={id}")
		public GetUserResponse getuser(@PathVariable("id") int id) {
			// return user based on id 
			User usr = userJDBCTemplate.getUser(id);
			String name = usr.getName();
			return new GetUserResponse(id, name);
		}
		
		@RequestMapping(value = "/ajax/users", method = RequestMethod.PUT)
		public @ResponseBody CreateUserResponse createuser(@RequestBody User user){
			// register new user
			// expects String name, String password
			if(user.getName() == null || user.getPassword() == null)
			{
				return new CreateUserResponse("Missing data");
			}
			String status = "Success";
			if (userJDBCTemplate.checkIfNameExists(user.getName()))
				status = "Username taken";
				// user name already existss
			else {
				String pw_hash = BCrypt.hashpw(user.getPassword(), BCrypt.gensalt());
				userJDBCTemplate.create(user.getName(), pw_hash);
				// hash the password and register user
			}
			
			return new CreateUserResponse(status);
		}

		
		@RequestMapping(value = "/ajax/sessions")
		public SessionsResponse sessions() {
			 // return list of session objects, exposing all the DB columns

			List<Session> sessions = sessionJDBCTemplate.listSessions();
			return new SessionsResponse(sessions);							
		}
		
		
		@RequestMapping(value = "/ajax/sessions", method = RequestMethod.POST)
		public LoginResponse login(@RequestBody User user) {
			// login user
			// creates a session for user 
			// expects String name, String password
			String session = "fail";
			if (userJDBCTemplate.checkIfNameExists(user.getName()))
			{
				// username exists
				User usr = userJDBCTemplate.getUserByName(user.getName());
				if (BCrypt.checkpw(user.getPassword(), usr.getPassword())) 
				{
					// password is correct
					if(sessionJDBCTemplate.checkIfSessionExists(usr.getId()))
						sessionJDBCTemplate.delete(usr.getId());
					
					session = java.util.UUID.randomUUID().toString();
					sessionJDBCTemplate.create(session, usr.getId(), new Timestamp(System.currentTimeMillis()));
					// generate session id
		
				}
			}
			// returns either sessionid or error message
			return new LoginResponse(session);
		}
		
		
}