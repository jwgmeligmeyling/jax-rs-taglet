package com.example.api;

import com.example.models.User;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.PUT;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.MediaType;

/**
 * The {@code UserAPI} is for CRUD operations to users.
 *
 * @author Jan-Willem Gmelig Meyling
 */
public interface UserAPI {

	/**
	 * Get an user by id.
	 * @param id Id to look for.
	 * @return The user.
	 * @throws javax.ws.rs.NotFoundException If the user could not be found.
	 * @api
	 */
	@GET
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
	@Consumes(MediaType.APPLICATION_JSON)
	User updateUser(@PathParam("userId") int id, User user);

	/**
	 * Delete an user.
	 * @param id Id of the user.
	 * @throws javax.ws.rs.NotFoundException If the user could not be found.
	 * @api
	 */
	@DELETE
	void deleteUser(@PathParam("userId") int id);

}
