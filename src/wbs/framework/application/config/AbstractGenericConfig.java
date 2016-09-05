package wbs.framework.application.config;

import static wbs.framework.utils.etc.StringUtils.stringEqualSafe;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

@Accessors (fluent = true)
public
class AbstractGenericConfig <Config extends AbstractGenericConfig <Config>>{

	// state

	@Getter @Setter
	GenericConfigSpec genericConfigSpec;

	// implementation

	public
	void forEach (
			@NonNull String type,
			@NonNull Consumer <Map <String, String>> closure) {

		forType (
			type)

			.stream ()

			.map (
				configItem ->
					configItem.params ())

			.forEach (
				closure);

	}

	public
	List <GenericConfigItemSpec> forType (
			@NonNull String type) {

		return genericConfigSpec.items ().stream ()

			.filter (
				configItem ->
					stringEqualSafe (
						configItem.type (),
						type))

			.collect (
				Collectors.toList ());

	}

}
