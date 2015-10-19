package com.example.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

/**
 * @author Jan-Willem Gmelig Meyling
 */
@Data
public class User {

	@JsonIgnoreProperties(ignoreUnknown = true)
	private Integer id;

	private String name;

}
