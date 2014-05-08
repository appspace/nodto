package ca.appspace.bean.serialization.server;


public class DefaultCodeStructureConfig implements CodeStructureConfig {

	private final static String GET = "get";
	private final static String IS = "is";
	private final static String SETTER_NAME_PREFIX = "set";
	private final static String[] GETTER_NAME_PREFIXES = new String[]{GET, IS};

	@Override
	public String getSetterNamePrefix() {
		return SETTER_NAME_PREFIX;
	}

	@Override
	public String[] getGetterNamePrefixes() {
		return GETTER_NAME_PREFIXES;
	}

	@Override
	public String getDefaultGetterPrefix() {
		return GET;
	}

}
