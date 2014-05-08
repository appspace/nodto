package ca.appspace.bean.serialization.server.handler;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

import ca.appspace.bean.serialization.server.CodeStructureConfig;
import ca.appspace.bean.serialization.server.PersistenceConfig;
import ca.appspace.bean.serialization.server.SerializationHandler;
import ca.appspace.bean.serialization.server.SerializationPreprocessor;
import ca.appspace.bean.serialization.server.SerializationProcessingException;
import ca.appspace.bean.serialization.shared.model.EntityReference;
import ca.appspace.bean.serialization.shared.model.Identifier;
import ca.appspace.bean.serialization.shared.model.Transport;

public class EntityHandler implements SerializationHandler {

	private final SerializationPreprocessor _serializationService;
	private final PersistenceConfig _persistenceConfig;
	private final CodeStructureConfig _codeStructureConfig;

	public EntityHandler(SerializationPreprocessor serializationService, PersistenceConfig persistenceConfig, 
			CodeStructureConfig codeStructureConfig) {
		_serializationService = serializationService;
		_persistenceConfig = persistenceConfig;
		_codeStructureConfig = codeStructureConfig;
	}

	@Override
	public boolean accepts(Object value) {
		return _persistenceConfig.isPersistedEntity(value);
	}

	@Override
	public <E extends Serializable> void handle(Transport<E> unwrappedRoot, Set<String> includesPath, Method setter, Serializable value) throws SerializationProcessingException {
		if (includesPath==null || includesPath.isEmpty()) {	//Don't bother - nobody wanted to see this value on client side
			return;
		}
		String fieldName = createFieldNameFromSetter(setter);
		boolean handled = false;
		for (String cachePathEntry : includesPath) {
			if (cachePathEntry.equalsIgnoreCase(fieldName))	 { //For first level fetches, like "address" 
				continueSerialialization(unwrappedRoot.getEntity(), setter, value, null);
				handled = true;
				break;
			} else if (cachePathEntry.startsWith(fieldName+".")) {	//For N-level fetches like "address.somethingElse.moreDeep" 						
				Set<String> nextSerializationPath = createNextPath(includesPath, fieldName);
				continueSerialialization(unwrappedRoot.getEntity(), setter, value, nextSerializationPath);
				handled = true;
				break;
			}
		}
		if (!handled) {
			EntityReference<?> reference = createReference(value);
			unwrappedRoot.addReference(fieldName, reference);	//TODO: key under which reference is
			
		}
	}

	private <E extends Serializable> void continueSerialialization(Serializable rootObject, Method setter, E value, Set<String> cacheResources) throws SerializationProcessingException {
		Transport<E> newTransport = _serializationService.prepare(value, cacheResources);
		E unwrapped = newTransport.getEntity();
		if (setter!=null) {
			try {
				setter.invoke(rootObject, unwrapped);
			} catch (Exception e) {
				throw new SerializationProcessingException("Unable to invoke setter", e);
			}
		}
	}

	private EntityReference<?> createReference(Object entity) throws SerializationProcessingException {
		Method identifierGetter = null;
		EntityReference<Number> reference = new EntityReference<Number>();
		Class<?> entityClass = _persistenceConfig.getClassWithoutInitializingProxy(entity);
		reference.setClassPath(entityClass.getCanonicalName());
		String idGetterName = _persistenceConfig.getIdFieldName(entityClass);
		if (idGetterName==null) {
			throw new SerializationProcessingException("Id field name for entity "+entityClass.getCanonicalName()+" is not specified in persistence configuration");
		}
		idGetterName = _codeStructureConfig.getDefaultGetterPrefix()
				+idGetterName.substring(0, 1).toUpperCase() + idGetterName.substring(1);
		try {
			identifierGetter = entityClass.getMethod(idGetterName, new Class[]{});	
			Number id = (Number) identifierGetter.invoke(entity, new Object[]{});
			reference.setIdentifier(new Identifier<Number>(id));
		} catch (Exception e) {
			throw new SerializationProcessingException("Unable to find or access Id getter with name "+idGetterName 
					+" of entity "+entityClass.getCanonicalName(), e);
		}
		return reference;
	}
	
	private Set<String> createNextPath(Set<String> previousPath, String fieldName) {
		Set<String> newPath = new HashSet<String>();
		for (String path : previousPath) {
			if (path.startsWith(fieldName+".")) {
				newPath.add(path.substring(fieldName.length()+1));
			}
		}
		return newPath;
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
