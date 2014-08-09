package wbs.platform.misc;

import java.util.Map;

import wbs.framework.utils.AbstractStringSubstituter;

public
class MapStringSubstituter
	extends AbstractStringSubstituter {

	private final
	Map<String,String> map;

	public
	MapStringSubstituter (
			Map<String,String> newMap) {

		map =
			newMap;

	}

	@Override
	protected
	String getSubstitute (
			String name) {

		return map.get (
			name);

	}

	public static
	String substitute (
			String input,
			Map<String,String> map) {

		return new MapStringSubstituter (
				map)

			.substitute (
				input);

	}

}
