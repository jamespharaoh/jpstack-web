package wbs.framework.fixtures;

import static wbs.utils.string.CodeUtils.simplifyToCodeRequired;

import java.util.Map;

import lombok.NonNull;

import wbs.framework.component.annotations.SingletonComponent;

@SingletonComponent ("codifyFixtureMappingPlugin")
public
class CodifyFixtureMappingPlugin
	implements FixtureMappingPlugin {

	// details

	@Override
	public
	String name () {
		return "codify";
	}

	// public implementation

	@Override
	public
	String map (
			@NonNull Map <String, Object> hints,
			@NonNull String inputValue) {

		return simplifyToCodeRequired (
			inputValue);

	}

}
