package com.example.api;

import com.example.models.User;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.List;

/**
 * The {@code UsersAPI} allows CRUD-operations to {@link User} entities.
 * This class is a nice example of how the {@code JAXRSTaglet} works.
 *
 * @author Jan-Willem Gmelig Meyling
 */
@Path("api/users")
@Consumes(MediaType.WILDCARD)
@Produces(MediaType.APPLICATION_JSON)
public interface UsersAPI {

	/**
	 * List all users.
	 * @return A list containing all users.
	 * @api
	 */
	@GET
	List<User> listUsers();

	/**
	 * Create a new user.
	 * @param user {@link User} object to create.
	 * @return the persisted user.
	 * @api
	 */
	@POST
	User createUser(User user);


	/**
	 * Get a {@code UserAPI} to interact with a user.
	 * @param id
	 * @return the UserAPI.
	 */
	@Path("{userId}")
	UserAPI getUser(@PathParam("userId") Integer id);

}
