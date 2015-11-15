package nl.tudelft.ewi.javax;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.googlecode.gentyref.GenericTypeReflector;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

/*
 * READ ALERT
 *    THE CODE BELOW IS TERRIBLE. I AM NOT RESPONSIBLE FOR ANY EYE INJURIES.
 *    FEEL FREE TO THROW IT AWAY AND SUBMIT PATCHES.
 */


/**
 * A class that initializes POJO's with some fields predefined.
 *
 * @author Jan-Willem Gmelig Meyling
 */
public class POJOInitializer {

	private static final List<Class<?>> FLOATY = Arrays.asList(new Class<?>[]{float.class, double.class, Float.class, Double.class});

	private final Map<Type, Object> instances = new HashMap<>();

	/**
	 * Create an instance of Type with some values set.
	 * @param type {@code Type} to set the information for.
	 * @return The instantiated object.
	 * @throws IllegalAccessException
	 * @throws InstantiationException
	 * @throws NoSuchFieldException
	 */
	public Object initializeTestData(Type type) throws IllegalAccessException, InstantiationException, NoSuchFieldException {
		System.out.println("POJOInitializer#initializeTestData " + type);
		Object instance = null;
		Class<?> clasz;
		if(type instanceof ParameterizedType) {
			clasz = (Class<?>) ((ParameterizedType) type).getRawType();
		}
		else if (type instanceof Class) {
			clasz = (Class<?>) type;
		}
		else if (type instanceof TypeVariable) {
			return null;
		}
		else {
			clasz = (Class<?>) type;
		}

		if(!clasz.equals(Object.class)) {
			try {
				if(Enum.class.isAssignableFrom(clasz)) {
					Enum<?>[] enumConstants = ((Class<? extends Enum<?>>) clasz).getEnumConstants();
					instance = enumConstants != null && enumConstants.length > 0 ? enumConstants[0] : null;
				}
				else if(Collection.class.isAssignableFrom(clasz)) {
					instance = createCollection(type);
				}
				else if (Map.class.isAssignableFrom(clasz)) {
					instance = createMap(type);
				}
				else if(float.class.equals(clasz) || Float.class.equals(clasz)) { instance =  (float) (Math.random() * 100); }
				else if(double.class.equals(clasz) || Double.class.equals(clasz)) { instance =  (Math.random() * 100); }
				else if (int.class.equals(clasz) || Integer.class.equals(clasz)) { instance = 1; }
				else if (byte.class.equals(clasz) || Byte.class.equals(clasz)) { instance = (byte) 1; }
				else if (short.class.equals(clasz) || Short.class.equals(clasz)) { instance = (short) 1; }
				else if (long.class.equals(clasz) || Long.class.equals(clasz)) { instance = 1l; }
				else if (Date.class.equals(clasz)) { return new Date(); }
				else if (String.class.equals(clasz)) {
					instance = "lupa";
				}
				else {
					if(instances.containsKey(type)) {
						return instances.get(type);
					}
					else if(clasz.getName().startsWith("java")) {
						instance = null;
					}
					else if (!Modifier.isAbstract(clasz.getModifiers())){
						instance = createPOJO(clasz);
					}
					else {
						instance = createAbstractPOJO(clasz);
					}
				}
			}
			catch (Throwable t) {
				t.printStackTrace();
			}
		}

		instances.put(type, instance);
		return instance;
	}

	@SuppressWarnings("unchecked")
	private Object createMap(java.lang.reflect.Type type) throws IllegalAccessException, InstantiationException, NoSuchFieldException {
		Map map = new HashMap();
		if(type instanceof ParameterizedType) {
			ParameterizedType parameterizedType = (ParameterizedType) type;
			java.lang.reflect.Type[] typeArguments = parameterizedType.getActualTypeArguments();
			if(!typeArguments[0].equals(Object.class) && !typeArguments[1].equals(Object.class)) {
				map.put(initializeTestData(typeArguments[0]), initializeTestData(typeArguments[1]));
			}
		}
		return map;
	}

	@SuppressWarnings("unchecked")
	private Object createCollection(java.lang.reflect.Type type) throws IllegalAccessException, InstantiationException, NoSuchFieldException {
		Collection collection;
		if (type instanceof ParameterizedType) {
			ParameterizedType parameterizedType = (ParameterizedType) type;
			collection = Set.class.isAssignableFrom((Class<?>) parameterizedType.getRawType()) ? new HashSet() : new ArrayList();
			java.lang.reflect.Type elementType = parameterizedType.getActualTypeArguments()[0];
			collection.add(initializeTestData(elementType));
			if (FLOATY.contains(elementType)) {
				collection.add(initializeTestData(elementType));
				collection.add(initializeTestData(elementType));
			}
		} else {
			collection = Set.class.isAssignableFrom((Class<?>) type) ? new HashSet() : new ArrayList();
		}
		return collection;
	}

	private final Stack<Class<?>> seenSubTypes = new Stack<>();

	private Object createAbstractPOJO(Class<?> clasz) throws IllegalAccessException, InstantiationException, NoSuchFieldException {
		Object instance = null;
		for(Class<?> annotatedClasz = clasz; annotatedClasz != null && !annotatedClasz.equals(Object.class); annotatedClasz = annotatedClasz.getSuperclass()) {
			JsonSubTypes jsonSubTypes = annotatedClasz.getAnnotation(JsonSubTypes.class);
			if(jsonSubTypes != null) {
				for(JsonSubTypes.Type jsonSubType : jsonSubTypes.value()) {
					Class<?> jsonSubTypeClass = jsonSubType.value();
					if(!seenSubTypes.contains(jsonSubTypeClass)) {
						if (clasz.isAssignableFrom(jsonSubTypeClass)) {
							// Ignore equal classes to prevent infite looping in recursive strategies:
							// Feature -> Feature -> Feature -> ...  => Feature -> Polygon
							seenSubTypes.push(jsonSubTypeClass);
							instance = createPOJO(jsonSubTypeClass);
							seenSubTypes.pop();
							break;
						}
					}
				}
			}
		}

		return instance;
	}

	private Object createPOJO(final Class<?> clasz) throws IllegalAccessException, InstantiationException {
		Object instance = clasz.newInstance();
		instances.put(clasz, instance); // Prevent creating dupes...
		for(Class<?> finger = clasz; finger != null && !finger.equals(Object.class); finger = finger.getSuperclass()) {
			for(Field field : finger.getDeclaredFields()) {
				if (Modifier.isStatic(field.getModifiers()) || field.isAnnotationPresent(JsonIgnore.class)) {
					continue;
				}
				try {
					field.setAccessible(true);
					Type type = GenericTypeReflector.getExactFieldType(field, clasz);
					field.set(instance, initializeTestData(type));
				}
				catch (Exception e) {
					// Swallow exceptions here...
					e.printStackTrace();
				}
			}
		}
		return instance;
	}

}
