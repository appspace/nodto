package ca.appspace.bean.serialization.server.handler;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.Set;

import ca.appspace.bean.serialization.server.PersistenceConfig;
import ca.appspace.bean.serialization.server.SerializationHandler;
import ca.appspace.bean.serialization.server.SerializationProcessingException;
import ca.appspace.bean.serialization.shared.model.Transport;

public class SimpleSerializableHanlder implements SerializationHandler {

	private final PersistenceConfig _persistenceConfig;

	public SimpleSerializableHanlder(PersistenceConfig persistenceConfig) {
		_persistenceConfig = persistenceConfig;
	}

	@Override
	public boolean accepts(Object value) {
		return value instanceof Serializable && 
				!_persistenceConfig.isPersistedEntity(value);
	}

	@Override
	public <E extends Serializable> void handle(Transport<E> unwrappedRoot, Set<String> includesPath, Method setter, Serializable value) throws SerializationProcessingException {
		try {
			setter.invoke(unwrappedRoot.getEntity(), value);
		} catch (Exception e) {
			throw new SerializationProcessingException("Unable to invoke setter "+setter.getName()+" of "+unwrappedRoot.getClass().getSimpleName(), e);
		}
	}

}
