import com.example.api.UserAPI;
import nl.tudelft.ewi.javax.JAXRSTaglet;
import org.jboss.resteasy.spi.metadata.ResourceMethod;
import org.junit.Ignore;
import org.junit.Test;

import java.lang.reflect.Method;
import java.util.List;

/**
 * @author Jan-Willem Gmelig Meyling
 */
public class TestSubResource {

	@Test
	@Ignore
	public void testName() throws Exception {
		Method method = UserAPI.class.getMethod("getUserById", Integer.class);
		JAXRSTaglet.scanResourceClasses(method);
		ResourceMethod resourceMethod = JAXRSTaglet.getResourceMethod(method);
		List<String> paths = JAXRSTaglet.getFullPaths(resourceMethod);
		System.out.println(paths);
	}
}
