package ca.appspace.bean.serialization.shared.model;

import java.io.Serializable;

public class EntityReference<I extends Number> implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private Identifier<I> _identifier;
	
	private String _classPath;

	public Identifier<I> getIdentifier() {
		return _identifier;
	}

	public void setIdentifier(Identifier<I> identifier) {
		_identifier = identifier;
	}

	public String getClassPath() {
		return _classPath;
	}

	public void setClassPath(String classPath) {
		_classPath = classPath;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((_classPath == null) ? 0 : _classPath.hashCode());
		result = prime * result
				+ ((_identifier == null) ? 0 : _identifier.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		EntityReference<?> other = (EntityReference<?>) obj;
		if (_classPath == null) {
			if (other._classPath != null)
				return false;
		} else if (!_classPath.equals(other._classPath))
			return false;
		if (_identifier == null) {
			if (other._identifier != null)
				return false;
		} else if (!_identifier.equals(other._identifier))
			return false;
		return true;
	}

}
