package wbs.console.forms;

import static wbs.utils.string.StringUtils.stringFormat;

import java.util.Map;

import com.google.common.base.Optional;

import lombok.NonNull;

import fj.data.Either;

public
interface FormFieldInterfaceMapping <Container, Generic, Interface> {

	default
	Either <Optional <Generic>, String> interfaceToGeneric (
			@NonNull Container container,
			@NonNull Map <String, Object> hints,
			@NonNull Optional <Interface> interfaceValue) {

		throw new UnsupportedOperationException (
			stringFormat (
				"%s.%s",
				getClass ().getSimpleName (),
				"interfaceToGeneric (...)"));

	}

	default
	Either <Optional <Interface>, String> genericToInterface (
			@NonNull Container container,
			@NonNull Map <String, Object> hints,
			@NonNull Optional <Generic> genericValue) {

		throw new UnsupportedOperationException (
			stringFormat (
				"%s.%s",
				getClass ().getSimpleName (),
				"genericToInterface (...)"));

	}

}
