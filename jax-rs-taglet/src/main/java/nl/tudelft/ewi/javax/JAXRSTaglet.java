package nl.tudelft.ewi.javax;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.sun.javadoc.Doc;
import com.sun.javadoc.MethodDoc;
import com.sun.javadoc.Parameter;
import com.sun.javadoc.Tag;
import com.sun.javadoc.Type;
import com.sun.tools.doclets.formats.html.markup.RawHtml;
import com.sun.tools.doclets.internal.toolkit.Content;
import com.sun.tools.doclets.internal.toolkit.taglets.Taglet;
import com.sun.tools.doclets.internal.toolkit.taglets.TagletWriter;
import org.jboss.resteasy.spi.metadata.MethodParameter;
import org.jboss.resteasy.spi.metadata.Parameter.ParamType;
import org.jboss.resteasy.spi.metadata.ResourceBuilder;
import org.jboss.resteasy.spi.metadata.ResourceClass;
import org.jboss.resteasy.spi.metadata.ResourceLocator;
import org.jboss.resteasy.spi.metadata.ResourceMethod;
import org.reflections.Reflections;

import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * The JAXRS taglet allows you to create beautiful JAX-RS API information to your JavaDocs.
 *
 * @author Jan-Willem Gmelig Meyling
 */
public class JAXRSTaglet implements Taglet {

	private static final String NAME = "api";
	private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
	private static final POJOInitializer POJO_INITIALIZER = new POJOInitializer();
	private static final Map<Class<?>, ResourceClass> RESOURCES = new HashMap<>();

	@SuppressWarnings({"unchecked", "unused"})
	public static void register(Map tagletMap) {
		JAXRSTaglet tag = new JAXRSTaglet();
		if (tagletMap.containsKey(NAME)) {
			tagletMap.remove(NAME);
		}
		tagletMap.put(NAME, tag);
	}

	public static void scanResourceClasses(Method method) {
		System.out.println("In scanResourceClasses!");
		new Reflections(method.getDeclaringClass().getPackage().getName())
			.getTypesAnnotatedWith(Path.class)
			.forEach(JAXRSTaglet::getResoureClass);
	}

	private static ResourceClass getResoureClass(Class<?> clasz) {
		System.out.println("In getResoureClass!");
		ResourceClass resourceClass = RESOURCES.get(clasz);
		if(resourceClass == null) {
			resourceClass = ResourceBuilder.rootResourceFromAnnotations(clasz);
			RESOURCES.put(clasz, resourceClass);
		}
		return resourceClass;
	}

	private static ResourceMethod getResourceMethod(ResourceClass resourceClass, Method method) {
		System.out.println("In getResourceMethod!");
		for(ResourceMethod resourceMethod : resourceClass.getResourceMethods()) {
			if(resourceMethod.getAnnotatedMethod().equals(method)) {
				return resourceMethod;
			}
		}
		for(ResourceLocator locator : resourceClass.getResourceLocators()) {
			ResourceMethod resourceMethod = getResourceMethod(
				getResoureClass(locator.getReturnType()),
				method
			);
			if(resourceMethod != null) return resourceMethod;
		}
		return null;
	}

	public static ResourceMethod getResourceMethod(Method method) {
		System.out.println("In getResourceMethod!");

		ResourceClass resourceClass = RESOURCES.get(method.getDeclaringClass());
		if(resourceClass == null) {
			resourceClass = getResoureClass(method.getDeclaringClass());
			RESOURCES.put(method.getDeclaringClass(), resourceClass);
		}

		return getResourceMethod(resourceClass, method);
	}

	private static void getFullPaths(ResourceMethod a, ResourceClass resourceClass, String basePath, Consumer<String> consumer) {
		System.out.printf("GetFullPaths %s %s %s %s", a, resourceClass, basePath, consumer);
		if(!resourceClass.getClazz().isAnnotationPresent(Path.class) && basePath.isEmpty()) {
			// Skip base cases without Path annotations
			return;
		}
		for(ResourceMethod resourceMethod : resourceClass.getResourceMethods()) {
			if(resourceMethod.equals(a)) {
				consumer.accept(basePath  + "/" + resourceMethod.getFullpath());
			}
		}
		for(ResourceLocator locator : resourceClass.getResourceLocators()) {
			getFullPaths(
				a,
				getResoureClass(locator.getReturnType()),
				basePath + "/" + locator.getFullpath(),
				consumer
			);
		}
	}

	public static List<String> getFullPaths(ResourceMethod resourceMethod) {
		System.out.println("In getFullPaths!");
		List<String> paths = new ArrayList<>();
		for(ResourceClass resourceClass : Lists.newArrayList(RESOURCES.values())) {
			getFullPaths(resourceMethod, resourceClass, "", paths::add);
		}
		return paths;
	}

	private static Map<String, Class<?>> PRIMITIES = ImmutableList.of(
		byte.class, short.class, int.class, long.class, float.class, double.class
	).stream().collect(Collectors.toMap(Class::getName, Function.identity()));

	public static Class<?> getClassFor(Type type) {
		System.out.println("In getClassFor!");

		Class<?> valueType;
		if(type.isPrimitive()) {
			valueType = PRIMITIES.get(type.typeName());
		}
		else {
			try {
				String qualifiedTypeName = fixQualifiedClassName(type.qualifiedTypeName());
				valueType = Class.forName(qualifiedTypeName);
			}
			catch (RuntimeException e) {
				throw e;
			}
			catch (Exception e) {
				throw new RuntimeException(e);
			}
		}

		if(!Strings.isNullOrEmpty(type.dimension())) {
			return Array.newInstance(valueType, type.dimension().length() / 2).getClass();
		}

		return valueType;
	}

	private static String fixQualifiedClassName(String typeName) {
		System.out.println("In fixQualifiedClassName!");
		String[] parts = typeName.split("\\.");

		String qualifiedTypeName = "";
		boolean inner = false;
		for(String part : parts) {
			if(!qualifiedTypeName.isEmpty()) qualifiedTypeName += inner ? "$" : ".";
			inner = inner || !part.toLowerCase().equals(part);
			qualifiedTypeName += part;
		}
		return qualifiedTypeName;
	}

	private static Method getMethodFor(MethodDoc methodDoc) throws NoSuchMethodException {
		System.out.println("In getMethodFor!");
		Class<?>[] paramTypes = Stream.of(methodDoc.parameters())
			.map(Parameter::type)
			.map(JAXRSTaglet::getClassFor)
			.toArray(Class<?>[]::new);
		return getClassFor(methodDoc.containingClass())
			.getMethod(methodDoc.name(), paramTypes);
	}

	public static String writeTestData(Object object) throws JsonProcessingException {
		System.out.println("In writeTestData!");
		return OBJECT_MAPPER.writerWithDefaultPrettyPrinter()
			.withoutFeatures(SerializationFeature.FAIL_ON_EMPTY_BEANS)
			.writeValueAsString(object);
	}


	public String doMagic(ResourceMethod method) {
		try {
			System.out.println("In doMagic!");
			StringBuilder stringBuilder = new StringBuilder();
			writeExampleRequest(method, stringBuilder);
			writeExampleResponse(method, stringBuilder);
			return stringBuilder.toString();
		}
		catch (Exception e){
			e.printStackTrace();
			throw new RuntimeException(e.getMessage(), e);
		}
	}

	private void writeExampleRequest(ResourceMethod method, StringBuilder stringBuilder) throws IllegalAccessException, InstantiationException, NoSuchFieldException, JsonProcessingException {

		System.out.println("In writeExampleRequest!");
		stringBuilder.append("<dt>Example request:</dt>\n");
		stringBuilder.append("<dd><code><pre>\n");

		for (String fullPath : getFullPaths(method)) {
			for (String httpMethod : method.getHttpMethods()) {
				stringBuilder.append(httpMethod).append(" ").append(fullPath).append("\n");
			}
		}

		for (MethodParameter param : method.getParams()) {
			if (!param.getParamType().equals(ParamType.MESSAGE_BODY)) {
				continue;
			}
			if (!Lists.newArrayList(method.getConsumes()).contains(MediaType.APPLICATION_JSON_TYPE)) {
				continue;
			}

			stringBuilder.append("\n");
			Object obj = POJO_INITIALIZER.initializeTestData(param.getGenericType());
			stringBuilder.append(writeTestData(obj));
		}

		stringBuilder.append("\n</pre></code></dd>\n");
	}


	private void writeExampleResponse(ResourceMethod method, StringBuilder stringBuilder) throws JsonProcessingException, IllegalAccessException, InstantiationException, NoSuchFieldException {

		System.out.println("In writeExampleResponse!");
		java.lang.reflect.Type returnType = method.getGenericReturnType();
		Class<?> rawReturnType = method.getReturnType();
		if (!Response.class.equals(rawReturnType) &&
			!Void.class.equals(rawReturnType) &&
			!void.class.equals(rawReturnType) &&
			Lists.newArrayList(method.getProduces()).contains(MediaType.APPLICATION_JSON_TYPE)) {

			stringBuilder.append("<dt>Example response:</dt>\n");
			stringBuilder.append("<dd><code><pre>\n").append(writeTestData(POJO_INITIALIZER.initializeTestData(returnType))).append("\n</pre></code></dd>\n");
		}
	}

	volatile boolean initialized = false;

	public synchronized String toString(Doc holder) {
		System.out.println("In toString!");
		if(holder.isMethod()) {
			MethodDoc doc = (MethodDoc) holder;
			try {
				Method method = getMethodFor(doc);
				if(!initialized) {
					scanResourceClasses(method);
					initialized = true;
				}
				ResourceMethod resourceMethod = getResourceMethod(method);
				if(resourceMethod != null)
					return doMagic(resourceMethod);
			}
			catch (RuntimeException e) {
				throw e;
			}
			catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
		return null;
	}

	@Override
	public Content getTagletOutput(Tag var1, TagletWriter var2) throws IllegalArgumentException {
		System.out.println("In getTagletOutput!");
		Content var3 = var2.getOutputInstance();
		var3.addContent(new RawHtml(toString(var1.holder())));
		return var3;
	}

	@Override
	public Content getTagletOutput(Doc var1, TagletWriter var2) throws IllegalArgumentException {
		System.out.println("In getTagletOutput!");
		Content var3 = var2.getOutputInstance();
		Tag[] var4 = var1.tags(this.getName());
		Stream.of(var4).map(Tag::holder)
			.map(this::toString)
			.filter(Objects::nonNull)
			.map(RawHtml::new)
			.forEach(var3::addContent);
		return var3;
	}

	@Override
	public String getName() {
		return NAME;
	}

	@Override
	public boolean inField() {
		return true;
	}

	@Override
	public boolean inConstructor() {
		return true;
	}

	@Override
	public boolean inMethod() {
		return true;
	}

	@Override
	public boolean inOverview() {
		return true;
	}

	@Override
	public boolean inPackage() {
		return true;
	}

	@Override
	public boolean inType() {
		return true;
	}

	@Override
	public boolean isInlineTag() {
		return false;
	}
}
