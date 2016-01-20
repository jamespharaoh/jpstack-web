package wbs.console.forms;

import static wbs.framework.utils.etc.Misc.errorResult;
import static wbs.framework.utils.etc.Misc.isEmpty;
import static wbs.framework.utils.etc.Misc.isNotPresent;
import static wbs.framework.utils.etc.Misc.stringFormat;
import static wbs.framework.utils.etc.Misc.successResult;

import javax.inject.Inject;

import lombok.NonNull;

import org.joda.time.LocalDate;

import com.google.common.base.Optional;

import fj.data.Either;

import wbs.console.misc.TimeFormatter;
import wbs.framework.application.annotations.PrototypeComponent;

@PrototypeComponent ("dateFormFieldInterfaceMapping")
public
class DateFormFieldInterfaceMapping<Container>
	implements FormFieldInterfaceMapping<Container,LocalDate,String> {

	// dependencies

	@Inject
	TimeFormatter timeFormatter;

	// implementation

	@Override
	public
	Either<Optional<String>,String> genericToInterface (
			@NonNull Container container,
			@NonNull Optional<LocalDate> genericValue) {

		// handle not present

		if (
			isNotPresent (
				genericValue)
		) {

			return successResult (
				Optional.absent ());

		}

		// format date

		return successResult (
			Optional.of (
				timeFormatter.localDateToDateString (
					genericValue.get ())));

	}

	@Override
	public
	Either<Optional<LocalDate>,String> interfaceToGeneric (
			@NonNull Container container,
			@NonNull Optional<String> interfaceValue) {

		// handle not present and empty

		if (

			isNotPresent (
				interfaceValue)

			|| isEmpty (
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
			isNotPresent (
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
