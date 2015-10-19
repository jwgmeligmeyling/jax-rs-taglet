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
	 * Get an user by id.
	 * @param id Id to look for.
	 * @return The user.
	 * @throws javax.ws.rs.NotFoundException If the user could not be found.
	 * @api
	 */
	@GET
	@Path("{userId}")
	@Consumes(MediaType.APPLICATION_JSON)
	User getUserById(@PathParam("userId") Integer id) throws NotFoundException;

	/**
	 * Update an user.
	 * @param id Id of the user.
	 * @param user The updated user information.
	 * @return The persisted user information.
	 * @throws javax.ws.rs.NotFoundException If the user could not be found.
	 * @api
	 */
	@PUT
	@Path("{userId}")
	@Consumes(MediaType.APPLICATION_JSON)
	User updateUser(@PathParam("userId") int id, User user);

	/**
	 * Delete an user.
	 * @param id Id of the user.
	 * @throws javax.ws.rs.NotFoundException If the user could not be found.
	 * @api
	 */
	@DELETE
	@Path("{userId}")
	void deleteUser(@PathParam("userId") Integer id);

}
