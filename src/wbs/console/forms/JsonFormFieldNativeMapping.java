package wbs.console.forms;

import static wbs.utils.etc.OptionalUtils.optionalIsNotPresent;

import com.google.common.base.Optional;

import lombok.NonNull;
import lombok.experimental.Accessors;

import org.json.simple.JSONValue;

import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.logging.TaskLogger;

@Accessors (fluent = true)
@PrototypeComponent ("jsonFormFieldNativeMapping")
public
class JsonFormFieldNativeMapping<Container>
	implements FormFieldNativeMapping<Container,Object,String> {

	@Override
	public
	Optional<Object> nativeToGeneric (
			@NonNull Container container,
			@NonNull Optional<String> nativeValue) {

		if (
			optionalIsNotPresent (
				nativeValue)
		) {
			return Optional.absent ();
		}

		return Optional.of (
			JSONValue.parse (
				nativeValue.get ()));

	}

	@Override
	public
	Optional <String> genericToNative (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull Container container,
			@NonNull Optional <Object> genericValue) {

		if (
			optionalIsNotPresent (
				genericValue)
		) {
			return Optional.absent ();
		}

		return Optional.of (
			JSONValue.toJSONString (
				genericValue.get ()));

	}

}
