package wbs.console.forms;

import static wbs.framework.utils.etc.Misc.camelToHyphen;
import static wbs.framework.utils.etc.Misc.isNotPresent;

import java.util.List;

import lombok.NonNull;

import com.google.common.base.Optional;

import wbs.framework.application.annotations.PrototypeComponent;

@PrototypeComponent ("enumCsvFormFieldInterfaceMapping")
public
class EnumCsvFormFieldInterfaceMapping<Container,Generic extends Enum<Generic>>
	implements FormFieldInterfaceMapping<Container,Generic,String> {

	@Override
	public
	Optional<Generic> interfaceToGeneric (
			@NonNull Container container,
			@NonNull Optional<String> interfaceValue,
			@NonNull List<String> errors) {

		throw new UnsupportedOperationException ();

	}

	@Override
	public
	Optional<String> genericToInterface (
			@NonNull Container container,
			@NonNull Optional<Generic> genericValue) {

		if (
			isNotPresent (
				genericValue)
		) {

			return Optional.of ("");

		} else {

			return Optional.of (
				camelToHyphen (
					genericValue.get ().toString ()));

		}

	}

}
