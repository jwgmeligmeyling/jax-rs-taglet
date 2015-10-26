import nl.tudelft.ewi.javax.POJOInitializer;
import org.geojson.GeoJsonObject;
import org.junit.Ignore;
import org.junit.Test;

/**
 * @author Jan-Willem Gmelig Meyling
 */
public class TestGeoJsonCreation {

	@Test
	@Ignore
	public void testName() throws Exception {
		POJOInitializer pojoInitializer = new POJOInitializer();
		GeoJsonObject obj = (GeoJsonObject) pojoInitializer.initializeTestData(GeoJsonObject.class);
		System.out.println(obj);
	}
}
