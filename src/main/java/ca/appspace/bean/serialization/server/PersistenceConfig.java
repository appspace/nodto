package ca.appspace.bean.serialization.server;

import java.io.Serializable;
import java.util.Collection;

public interface PersistenceConfig {

	public boolean isPersistedEntity(Object entity);

	public Class<?> getClassWithoutInitializingProxy(Object value);

	public <X extends Object> X deproxify(X proxy);
	
	public String getIdFieldName(Class<?> entityClass);

	boolean isLazyCollection(Collection<?> collection);

	Class<? extends Collection> getCollectionUnderlyingType(
			Collection<? extends Serializable> values);

}
