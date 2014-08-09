package wbs.framework.utils;

import java.util.HashMap;
import java.util.Map;

public
class StringSubstituter
	extends AbstractStringSubstituter {

	private final
	Map<String,String> params =
		new HashMap<String,String> ();

	public
	StringSubstituter param (
			String name,
			String value) {

		params.put (name, value);

		return this;

	}

	@Override
	protected
	String getSubstitute (
			String name) {

		return params.get (name);

	}

}
