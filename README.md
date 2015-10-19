JAX-RS Taglet
==============

A special [JavaDoc](http://www.oracle.com/technetwork/articles/java/index-137868.html) [taglet](https://docs.oracle.com/javase/8/docs/technotes/guides/javadoc/taglet/overview.html) that allows you to create beautiful JavaDocs for JAX-RS API's and embeds example responses and requests.
In order to do so it creates basic objects and serializes them through [Jackson](http://wiki.fasterxml.com/JacksonHome).

*Requires Java 8*

## Example usage:

```java
class MyResource {

	/**
	 * Create a new user.
	 * @param user {@link User} object to create.
	 * @return the persisted user.
	 * @api
	 */
	@POST
	User createUser(User user);

}
```

## Implementation notes:

In order for the plugin to work, you need to add it as taglet.
Furthermore, this program is a dynamic analyser, based on the reflection API and the [RestEasy](http://resteasy.jboss.org/) [`ResourceBuilder`](http://docs.jboss.org/resteasy/docs/3.0.13.Final/javadocs/org/jboss/resteasy/spi/metadata/ResourceBuilder.ResourceMethodParameterBuilder.html).
Because this Taglet runs dynamic, your project needs to be added to the classpath.
This can be done using the following configuration:

```xml
<plugin>
<groupId>org.apache.maven.plugins</groupId>
<artifactId>maven-javadoc-plugin</artifactId>
<version>2.10.3</version>
<configuration>
	<taglet>nl.tudelft.ewi.javax.JAXRSTaglet</taglet>
	<tagletArtifacts>
		<tagletArtifact>
			<artifactId>jax-rs-taglet</artifactId>
			<groupId>nl.tudelft.ewi.javadoc</groupId>
			<version>${project.version}</version>
		</tagletArtifact>
		<tagletArtifact>
			<artifactId>jax-rs-taglet-example</artifactId>
			<groupId>nl.tudelft.ewi.javadoc</groupId>
			<version>${project.version}</version>
		</tagletArtifact>
	</tagletArtifacts>
</configuration>
</plugin>
```

## Maven artefact

Currently there is no live artefact yet, so you have to clone the repository and install it locally using `mvn clean install -DskipTests`,