package wbs.console.forms;

import lombok.NonNull;
import lombok.experimental.Accessors;

import org.json.simple.JSONValue;

import wbs.framework.component.annotations.PrototypeComponent;

import com.google.common.base.Optional;

import static wbs.framework.utils.etc.OptionalUtils.optionalIsNotPresent;

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
	Optional<String> genericToNative (
			@NonNull Container container,
			@NonNull Optional<Object> genericValue) {

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
