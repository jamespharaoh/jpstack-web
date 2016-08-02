package wbs.framework.utils.etc;

import static wbs.framework.utils.etc.Misc.stringToBytes;
import lombok.NonNull;

import org.json.simple.JSONValue;

public
class JsonUtils {

	public static
	byte[] jsonToBytes (
			@NonNull Object object) {

		return stringToBytes (
			JSONValue.toJSONString (
				object),
			"utf-8");

	}

}
