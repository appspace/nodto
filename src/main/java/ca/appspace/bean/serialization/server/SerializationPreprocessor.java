package ca.appspace.bean.serialization.server;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import ca.appspace.bean.serialization.server.handler.CollectionsHandler;
import ca.appspace.bean.serialization.server.handler.EntityHandler;
import ca.appspace.bean.serialization.server.handler.SimpleSerializableHanlder;
import ca.appspace.bean.serialization.shared.model.Transport;

public class SerializationPreprocessor {
	
	private final PersistenceConfig _serializationServiceConfig;
	private final CodeStructureConfig _codeStructureConfig;
	private final List<SerializationHandler> _handlers = new LinkedList<SerializationHandler>();

	public SerializationPreprocessor(PersistenceConfig persistenceConfig, CodeStructureConfig codeStructureConfig) {
		_serializationServiceConfig = persistenceConfig;
		_codeStructureConfig = codeStructureConfig;
		
		_handlers.add(new CollectionsHandler(this, _serializationServiceConfig, _codeStructureConfig));
		_handlers.add(new EntityHandler(this, _serializationServiceConfig, _codeStructureConfig));
		_handlers.add(new SimpleSerializableHanlder(_serializationServiceConfig));
	}

	public <E extends Serializable> List<Transport<E>> prepare(List<E> beans, String... includesPath) 
			throws SerializationProcessingException {
		return prepare(beans, new HashSet<>(Arrays.asList(includesPath)));
	}
	
	public <E extends Serializable> List<Transport<E>> prepare(List<E> beans, Set<String> includesPath) 
			throws SerializationProcessingException {
		List<Transport<E>> newCollection = null;
		if (beans==null) {
			return null;
		}
		if (beans.isEmpty()) {
			return Collections.emptyList(); 
		}
		try {
			newCollection = beans.getClass().newInstance();
		} catch (IllegalAccessException e) {
		} catch (InstantiationException e) {
		}
		if (newCollection==null) {
			newCollection = new ArrayList<Transport<E>>();
		}
		for (E bean : beans) {
			newCollection.add(prepare(bean, includesPath));
		}
		return newCollection;
	}

	public <E extends Serializable> Transport<E> prepare(E entity, String... cacheResources) throws SerializationProcessingException {
		return prepare(entity, new HashSet<>(Arrays.asList(cacheResources)));
	}
	
	@SuppressWarnings("unchecked")
	public <E extends Serializable> Transport<E> prepare(E entity, Set<String> cacheResources) throws SerializationProcessingException {
		E unwrapped = null;
		Class<?> entityClass = null;
		if (entity==null) {
			return null;
		}
		Transport<E> result = new Transport<E>();
		entity = _serializationServiceConfig.deproxify(entity);
		entityClass = entity.getClass();
		try {
			unwrapped = (E) entityClass.newInstance();
		} catch (Exception e) {
			e.printStackTrace();
			return null;	//TODO: handle exception
		}
		result.setEntity(unwrapped);

		for (Method potentialGetter : entityClass.getMethods()) {
			String getterPrefix = null;
			Method setter = null;
			Class<?> returnType = null;
			if (!Modifier.isPublic(potentialGetter.getModifiers())) {
				continue;		//If method is not public, continue to the next method
			}
			getterPrefix = findPrefix(potentialGetter);
			if (getterPrefix == null) {
				continue;
			}
			returnType = potentialGetter.getReturnType();
			setter = findSetter(unwrapped, getterPrefix, potentialGetter, returnType);
			if (setter==null) {
				continue;	//If we can get but can't set, this is not a field
			}
			try {
				Object value = potentialGetter.invoke(entity, new Object[]{});
				if (value==null) {
					continue;
				}
				for (SerializationHandler handler : _handlers) {
					if (handler.accepts(value)) {
						handler.handle(result, cacheResources, setter, (Serializable) value);
						break;
					}
				}
			} catch (Exception e) {
				throw new SerializationProcessingException("Unable to prepare result value of getter "+potentialGetter.getName()
						+"; Getter owner "+entityClass.getCanonicalName(), e);
			}
		}
		return result;
	}

	private Method findSetter(Serializable entity, String getterPrefix, Method getter, Class<?> parameterType) {
		String setterName = _codeStructureConfig.getSetterNamePrefix()+getter.getName().substring(getterPrefix.length());
		Method result = null;
		try {
			result = entity.getClass().getMethod(setterName, parameterType);
		} catch (NoSuchMethodException e) {
			return null;
		}
		if (!Modifier.isPublic(result.getModifiers())) {
			result = null;
		}
		return result;
	}
	
	/**
	 * Searches for registered prefix entry in method name
	 * @param method
	 * @return
	 */
	private String findPrefix(Method method) {
		for (String prefix : _codeStructureConfig.getGetterNamePrefixes()) {
			if (method.getName().startsWith(prefix)) {
				return prefix;
			}
		}
		return null;
	}
	
}
