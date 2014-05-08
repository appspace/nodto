package ca.appspace.bean.serialization.shared.model;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class Transport<E extends Serializable> implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private E _entity;

	private Map<String, EntityReference<?>> _references;

	public E getEntity() {
		return _entity;
	}

	public void setEntity(E entity) {
		_entity = entity;
	}

	public Map<String, EntityReference<?>> getReferences() {
		return _references;
	}

	public void setReferences(Map<String, EntityReference<?>> references) {
		_references = references;
	}

	public boolean containsReference(EntityReference<?> reference) {
		return _references!=null && _references.containsKey(reference);
	}

	public void addReference(String name, EntityReference<?> reference) {
		if (_references==null) {
			_references = new HashMap<String, EntityReference<?>>();
		}
		_references.put(name, reference);
	}

}
