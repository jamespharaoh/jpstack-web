package wbs.framework.utils.etc;

import java.util.Map;
import java.util.Properties;

import lombok.NonNull;

public
class CollectionUtils {

	public static
	Properties toProperties (
			@NonNull Map <String, String> propertiesMap) {

		Properties properties =
			new Properties ();

		properties.putAll (
			propertiesMap);

		return properties;

	}

}
