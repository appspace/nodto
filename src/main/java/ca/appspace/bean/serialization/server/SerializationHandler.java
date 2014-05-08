package ca.appspace.bean.serialization.server;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.Set;

import ca.appspace.bean.serialization.shared.model.Transport;

public interface SerializationHandler {
	public boolean accepts(Object value);
	public <E extends Serializable> void handle(Transport<E> unwrappedRoot, Set<String> includesPath, Method setter, Serializable value) throws SerializationProcessingException;
}
