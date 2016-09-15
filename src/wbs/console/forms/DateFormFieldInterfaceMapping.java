package wbs.console.forms;

import static wbs.utils.etc.Misc.errorResult;
import static wbs.utils.etc.Misc.successResult;
import static wbs.utils.etc.OptionalUtils.optionalIsNotPresent;
import static wbs.utils.string.StringUtils.stringFormat;
import static wbs.utils.string.StringUtils.stringIsEmpty;

import java.util.Map;

import com.google.common.base.Optional;

import lombok.NonNull;

import org.joda.time.LocalDate;

import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.utils.time.TimeFormatter;

import fj.data.Either;

@PrototypeComponent ("dateFormFieldInterfaceMapping")
public
class DateFormFieldInterfaceMapping <Container>
	implements FormFieldInterfaceMapping <Container, LocalDate, String> {

	// singleton dependencies

	@SingletonDependency
	TimeFormatter timeFormatter;

	// implementation

	@Override
	public
	Either <Optional <String>, String> genericToInterface (
			@NonNull Container container,
			@NonNull Map <String, Object> hints,
			@NonNull Optional <LocalDate> genericValue) {

		// handle not present

		if (
			optionalIsNotPresent (
				genericValue)
		) {

			return successResult (
				Optional.absent ());

		}

		// format date

		return successResult (
			Optional.of (
				timeFormatter.dateString (
					genericValue.get ())));

	}

	@Override
	public
	Either<Optional<LocalDate>,String> interfaceToGeneric (
			@NonNull Container container,
			@NonNull Map<String,Object> hints,
			@NonNull Optional<String> interfaceValue) {

		// handle not present and empty

		if (

			optionalIsNotPresent (
				interfaceValue)

			|| stringIsEmpty (
				interfaceValue.get ().trim ())

		) {

			return successResult (
				Optional.<LocalDate>absent ());

		}

		// parse date

		Optional<LocalDate> genericValue =
			timeFormatter.dateStringToLocalDate (
				interfaceValue.get ());

		if (
			optionalIsNotPresent (
				genericValue)
		) {

			return errorResult (
				stringFormat (
					"You must enter a valid date in yyyy-mm-dd format"));

		}

		return successResult (
			genericValue);

	}

}
