package wbs.console.forms;

import static wbs.utils.etc.OptionalUtils.optionalAbsent;
import static wbs.utils.etc.OptionalUtils.optionalIsNotPresent;
import static wbs.utils.etc.OptionalUtils.optionalOf;
import static wbs.utils.etc.OptionalUtils.optionalOfFormat;

import com.google.common.base.Optional;

import lombok.NonNull;
import lombok.experimental.Accessors;

import org.json.simple.JSONValue;

import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.database.Transaction;

@Accessors (fluent = true)
@PrototypeComponent ("jsonFormFieldNativeMapping")
public
class JsonFormFieldNativeMapping<Container>
	implements FormFieldNativeMapping<Container,Object,String> {

	@Override
	public
	Optional <Object> nativeToGeneric (
			@NonNull Transaction parentTransaction,
			@NonNull Container container,
			@NonNull Optional <String> nativeValue) {

		if (
			optionalIsNotPresent (
				nativeValue)
		) {
			return optionalAbsent ();
		}

		return optionalOf (
			JSONValue.parse (
				nativeValue.get ()));

	}

	@Override
	public
	Optional <String> genericToNative (
			@NonNull Transaction parentTransaction,
			@NonNull Container container,
			@NonNull Optional <Object> genericValue) {

		if (
			optionalIsNotPresent (
				genericValue)
		) {
			return optionalAbsent ();
		}

		return optionalOfFormat (
			JSONValue.toJSONString (
				genericValue.get ()));

	}

}
