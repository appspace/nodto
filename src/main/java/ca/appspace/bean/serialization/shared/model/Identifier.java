package ca.appspace.bean.serialization.shared.model;

import java.io.Serializable;

public class Identifier<I extends Serializable> implements Serializable {

	/**
	 * Wrapper for entity database identifier. 
	 */
	private static final long serialVersionUID = 1L;

	private I _value;

	public Identifier(I id) {
		this();
		_value = id;
	}
	
	public Identifier() {}
	
	public I getValue() {
		return _value;
	}

	public void setValue(I value) {
		_value = value;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((_value == null) ? 0 : _value.hashCode());
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
		Identifier<?> other = (Identifier<?>) obj;
		if (_value == null) {
			if (other._value != null)
				return false;
		} else if (!_value.equals(other._value))
			return false;
		return true;
	}

}
