package wbs.console.forms;

import static wbs.framework.utils.etc.EnumUtils.enumNotEqualSafe;
import static wbs.framework.utils.etc.Misc.errorResult;
import static wbs.framework.utils.etc.Misc.successResult;
import static wbs.framework.utils.etc.OptionalUtils.optionalGetRequired;
import static wbs.framework.utils.etc.OptionalUtils.optionalIsNotPresent;
import static wbs.framework.utils.etc.StringUtils.stringFormat;
import static wbs.framework.utils.etc.StringUtils.stringIsEmpty;

import java.util.Map;

import com.google.common.base.Optional;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

import org.joda.time.Instant;

import wbs.console.misc.ConsoleUserHelper;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;

import fj.data.Either;

@Accessors (fluent = true)
@PrototypeComponent ("timestampFormFieldInterfaceMapping")
public
class TimestampFormFieldInterfaceMapping <Container>
	implements FormFieldInterfaceMapping <Container, Instant, String> {

	// singleton dependencies

	@SingletonDependency
	ConsoleUserHelper preferences;

	// properties

	@Getter @Setter
	String name;

	@Getter @Setter
	TimestampFormFieldSpec.Format format;

	// implementation

	@Override
	public
	Either <Optional <Instant>, String> interfaceToGeneric (
			@NonNull Container container,
			@NonNull Map <String, Object> hints,
			@NonNull Optional <String> interfaceValue) {

		if (
			enumNotEqualSafe (
				format,
				TimestampFormFieldSpec.Format.timestamp)
		) {

			throw new RuntimeException ();

		} else if (

			optionalIsNotPresent (
				interfaceValue)

			|| stringIsEmpty (
				optionalGetRequired (
					interfaceValue))

		) {

			return successResult (
				Optional.absent ());

		} else {

			try {

				return successResult (
					Optional.of (
						preferences.timestampStringToInstant (
							interfaceValue.get ())));

			} catch (IllegalArgumentException exception) {

				return errorResult (
					stringFormat (
						"Please enter a valid timestamp for %s",
						name ()));

			}

		}

	}

	@Override
	public
	Either<Optional<String>,String> genericToInterface (
			@NonNull Container container,
			@NonNull Map<String,Object> hints,
			@NonNull Optional<Instant> genericValue) {

		if (
			optionalIsNotPresent (
				genericValue)
		) {

			return successResult (
				Optional.of (
					""));

		}

		switch (format) {

		case timestamp:

			return successResult (
				Optional.of (
					preferences.timestampWithTimezoneString (
						genericValue.get ())));

		case date:

			return successResult (
				Optional.of (
					preferences.dateStringShort (
						genericValue.get ())));

		case time:

			return successResult (
				Optional.of (
					preferences.timeString (
						genericValue.get ())));

		default:

			throw new RuntimeException ();

		}

	}

}
