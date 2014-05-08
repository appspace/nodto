package ca.appspace.bean.serialization.server.handler;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Set;

import ca.appspace.bean.serialization.server.CodeStructureConfig;
import ca.appspace.bean.serialization.server.PersistenceConfig;
import ca.appspace.bean.serialization.server.SerializationHandler;
import ca.appspace.bean.serialization.server.SerializationPreprocessor;
import ca.appspace.bean.serialization.server.SerializationProcessingException;
import ca.appspace.bean.serialization.shared.model.Transport;

public class CollectionsHandler implements SerializationHandler {

	private final SerializationPreprocessor _serializationService;
	private final CodeStructureConfig _codeStructureConfig;
	private final PersistenceConfig _persistenceConfig; 
	
	public CollectionsHandler(SerializationPreprocessor serializationService, PersistenceConfig persistenceConfig, CodeStructureConfig codeStructureConfig) {
		_serializationService = serializationService;
		_persistenceConfig = persistenceConfig;
		_codeStructureConfig = codeStructureConfig;
	}

	@Override
	public boolean accepts(Object value) {
		return value instanceof Collection;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <E extends Serializable> void handle(Transport<E> unwrappedRoot, Set<String> includesPath, Method setter, Serializable value) 
			throws SerializationProcessingException {
		if (includesPath==null || includesPath.isEmpty()) {
			return;
		}
		String fieldName = createFieldNameFromSetter(setter);
		for (String cachePathEntry : includesPath) {
			if (cachePathEntry.equalsIgnoreCase(fieldName))	 { //For first level fetches, like "address" 
				continueSerialialization(unwrappedRoot.getEntity(), setter, (Collection<? extends Serializable>) value, null);
				break;
			} else if (cachePathEntry.startsWith(fieldName+".")) {	//For N-level fetches like "address.somethingElse.moreDeep" 						
				Set<String> nextSerializationPath = null;	//createNextPath(includesPath, fieldName);	//TODO
				continueSerialialization(unwrappedRoot.getEntity(), setter, (Collection<? extends Serializable>) value, nextSerializationPath);
				break;
			}
		}
	}

	@SuppressWarnings("unchecked")
	private void continueSerialialization(Serializable rootObject, Method setter, 
			Collection<? extends Serializable> values, Set<String> cacheResources) throws SerializationProcessingException {
		Collection<Serializable> unwrappedList = null;
		if (setter==null) {
			return;
		}
		try {
			if (_persistenceConfig.isLazyCollection(values)) {
				@SuppressWarnings("rawtypes")
				Class<? extends Collection> unwrappedListType = _persistenceConfig.getCollectionUnderlyingType(values);
				if (unwrappedListType==null) {
					throw new SerializationProcessingException("Unable to unwrap persistent collection "+values.getClass());
				}
				unwrappedList = unwrappedListType.newInstance();
			} else {
				unwrappedList = values.getClass().newInstance();
			}
		} catch (Exception e) {
			throw new SerializationProcessingException("Unable to instantiate collection of type "+values.getClass());
		}
		if (values!=null && !values.isEmpty()) {
			for (Object value : values) {
				if (_persistenceConfig.isPersistedEntity(value)) {
					unwrappedList.add(_serializationService.prepare((Serializable)value, (Set)null).getEntity());
				} else if (value instanceof Serializable && !(value instanceof Collection)) {
					unwrappedList.add((Serializable) value);
				}
			}
		}
		try {
			setter.invoke(rootObject, unwrappedList);
		} catch (Exception e) {
			throw new SerializationProcessingException("Unable to invoke setter", e);
		}
	}
	
	private String createFieldNameFromSetter(Method setter) {
		String setterName = setter.getName();
		setterName = setterName.substring(_codeStructureConfig.getSetterNamePrefix().length());
		while (setterName.startsWith("_")) {
			setterName = setterName.substring(1);
		}
		setterName = setterName.substring(0, 1).toLowerCase() + setterName.substring(1);
		return setterName;
	}
}
