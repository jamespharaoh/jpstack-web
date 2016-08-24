package wbs.console.forms;

import static wbs.framework.utils.etc.Misc.successResult;
import static wbs.framework.utils.etc.OptionalUtils.optionalIsNotPresent;
import static wbs.framework.utils.etc.StringUtils.camelToHyphen;

import java.util.Map;

import lombok.NonNull;

import com.google.common.base.Optional;

import fj.data.Either;

import wbs.framework.application.annotations.PrototypeComponent;

@PrototypeComponent ("enumCsvFormFieldInterfaceMapping")
public
class EnumCsvFormFieldInterfaceMapping<Container,Generic extends Enum<Generic>>
	implements FormFieldInterfaceMapping<Container,Generic,String> {

	@Override
	public
	Either<Optional<Generic>,String> interfaceToGeneric (
			@NonNull Container container,
			@NonNull Map<String,Object> hints,
			@NonNull Optional<String> interfaceValue) {

		throw new UnsupportedOperationException ();

	}

	@Override
	public
	Either<Optional<String>,String> genericToInterface (
			@NonNull Container container,
			@NonNull Map<String,Object> hints,
			@NonNull Optional<Generic> genericValue) {

		if (
			optionalIsNotPresent (
				genericValue)
		) {

			return successResult (
				Optional.of (
					""));

		} else {

			return successResult (
				Optional.of (
					camelToHyphen (
						genericValue.get ().toString ())));

		}

	}

}
