import com.example.api.UsersAPI;
import com.fasterxml.jackson.core.JsonProcessingException;
import nl.tudelft.ewi.javax.JAXRSTaglet;
import org.jboss.resteasy.spi.metadata.ResourceMethod;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.lang.reflect.Method;
import java.util.Map;

/**
 * @author Jan-Willem Gmelig Meyling
 */
public class TestTheThing {

	JAXRSTaglet jaxrsTaglet;

	@Before
	public void setUp() {
		jaxrsTaglet = new JAXRSTaglet();
	}

	@Test
	public void testRegistration() {
		Map map = Mockito.mock(Map.class);
		JAXRSTaglet.register(map);
		Mockito.verify(map).put(Mockito.eq("api"), Mockito.any(JAXRSTaglet.class));
	}

	@Test
	public void test() throws NoSuchMethodException, JsonProcessingException, NoSuchFieldException, InstantiationException, IllegalAccessException {
		for(Method method : UsersAPI.class.getDeclaredMethods()) {
			JAXRSTaglet.scanResourceClasses(method);
			ResourceMethod resourceMethod = JAXRSTaglet.getResourceMethod(method);
			System.out.println(jaxrsTaglet.doMagic(resourceMethod));
			System.out.println();
		}
	}


}
