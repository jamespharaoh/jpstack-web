package wbs.framework.fixtures;

import java.util.Map;

public
interface FixtureMappingPlugin {

	String name ();

	String map (
			Map <String, Object> hints,
			String inputValue);

}
