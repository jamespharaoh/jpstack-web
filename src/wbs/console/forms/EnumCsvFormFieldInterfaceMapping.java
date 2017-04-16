package wbs.console.forms;

import static wbs.utils.etc.OptionalUtils.optionalIsNotPresent;
import static wbs.utils.etc.OptionalUtils.optionalOf;
import static wbs.utils.etc.ResultUtils.successResult;
import static wbs.utils.string.StringUtils.camelToHyphen;

import java.util.Map;

import com.google.common.base.Optional;

import lombok.NonNull;

import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.logging.TaskLogger;

import fj.data.Either;

@PrototypeComponent ("enumCsvFormFieldInterfaceMapping")
public
class EnumCsvFormFieldInterfaceMapping <
	Container,
	Generic extends Enum <Generic>
>
	implements FormFieldInterfaceMapping <
		Container,
		Generic,
		String
	> {

	@Override
	public
	Either <Optional <String>, String> genericToInterface (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull Container container,
			@NonNull Map <String, Object> hints,
			@NonNull Optional <Generic> genericValue) {

		if (
			optionalIsNotPresent (
				genericValue)
		) {

			return successResult (
				optionalOf (
					""));

		} else {

			return successResult (
				optionalOf (
					camelToHyphen (
						genericValue.get ().toString ())));

		}

	}

}
