package wbs.console.forms.time;

import static wbs.utils.etc.OptionalUtils.optionalAbsent;
import static wbs.utils.etc.OptionalUtils.optionalCast;
import static wbs.utils.etc.OptionalUtils.optionalFromNullable;
import static wbs.utils.etc.OptionalUtils.optionalGetRequired;
import static wbs.utils.etc.OptionalUtils.optionalIsNotPresent;
import static wbs.utils.etc.OptionalUtils.optionalIsPresent;
import static wbs.utils.etc.OptionalUtils.optionalMapOptional;
import static wbs.utils.etc.OptionalUtils.optionalMapRequiredOrDefault;
import static wbs.utils.etc.ResultUtils.errorResultFormat;
import static wbs.utils.etc.ResultUtils.successResult;
import static wbs.utils.etc.ResultUtils.successResultAbsent;
import static wbs.utils.etc.ResultUtils.successResultPresent;
import static wbs.utils.string.StringUtils.stringIsEmpty;

import java.util.Map;

import com.google.common.base.Optional;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import wbs.console.forms.types.FormFieldInterfaceMapping;
import wbs.console.helper.manager.ConsoleObjectManager;
import wbs.console.misc.ConsoleUserHelper;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.NestedTransaction;
import wbs.framework.database.Transaction;
import wbs.framework.logging.LogContext;

import wbs.utils.time.core.TimeFormatter;

import fj.data.Either;

@Accessors (fluent = true)
@PrototypeComponent ("timestampFormFieldInterfaceMapping")
public
class TimestampFormFieldInterfaceMapping <Container>
	implements FormFieldInterfaceMapping <Container, DateTime, String> {

	// singleton dependencies

	@SingletonDependency
	ConsoleUserHelper consoleUserHelper;

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	ConsoleObjectManager objectManager;

	// properties

	@Getter @Setter
	String name;

	@Getter @Setter
	TimeFormatter timeFormatter;

	@Getter @Setter
	TimestampFormFieldFormat format;

	@Getter @Setter
	String timezonePath;

	// implementation

	@Override
	public
	Either <Optional <DateTime>, String> interfaceToGeneric (
			@NonNull Transaction parentTransaction,
			@NonNull Container container,
			@NonNull Map <String, Object> hints,
			@NonNull Optional <String> interfaceValue) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"interfaceToGeneric");

		) {

			if (

				optionalIsNotPresent (
					interfaceValue)

				|| stringIsEmpty (
					optionalGetRequired (
						interfaceValue))

			) {

				return successResult (
					optionalAbsent ());

			}

			Optional <String> timezoneNameOptional =
				optionalCast (
					String.class,
					optionalMapOptional (
						optionalFromNullable (
							timezonePath),
						timezonePath ->
							objectManager.dereference (
								transaction,
								container,
								timezonePath,
								hints)));

			DateTimeZone timezone =
				optionalMapRequiredOrDefault (
					DateTimeZone::forID,
					timezoneNameOptional,
					consoleUserHelper.timezone (
						transaction));

			Optional <DateTime> genericValueOptional =
				timeFormatter.timestampParseAuto (
					timezone,
					interfaceValue.get ());

			if (
				optionalIsPresent (
					genericValueOptional)
			) {

				return successResult (
					genericValueOptional);

			} else {

				return errorResultFormat (
					"Please enter a valid timestamp for %s",
					name ());

			}

		}

	}

	@Override
	public
	Either <Optional <String>, String> genericToInterface (
			@NonNull Transaction parentTransaction,
			@NonNull Container container,
			@NonNull Map <String, Object> hints,
			@NonNull Optional <DateTime> genericValue) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"genericToInterface");

		) {

			if (
				optionalIsNotPresent (
					genericValue)
			) {

				return successResultAbsent ();

			}

			Optional <String> timezoneNameOptional =
				optionalCast (
					String.class,
					optionalMapOptional (
						optionalFromNullable (
							timezonePath),
						timezonePath ->
							objectManager.dereference (
								transaction,
								container,
								timezonePath,
								hints)));

			if (
				optionalIsPresent (
					timezoneNameOptional)
			) {

				DateTimeZone timezone =
					DateTimeZone.forID (
						optionalGetRequired (
							timezoneNameOptional));

				switch (format) {

				case timestampSecond:

					return successResultPresent (
						timeFormatter.timestampSecondString (
							timezone,
							genericValue.get ()));

				case timestampMinute:

					return successResultPresent (
						timeFormatter.timestampMinuteString (
							timezone,
							genericValue.get ()));

				case timestampHour:

					return successResultPresent (
						timeFormatter.timestampHourString (
							timezone,
							genericValue.get ()));

				case timestampTimezoneSecond:

					return successResultPresent (
						timeFormatter.timestampTimezoneSecondString (
							timezone,
							genericValue.get ()));

				case timestampTimezoneMinute:

					return successResultPresent (
						timeFormatter.timestampTimezoneMinuteString (
							timezone,
							genericValue.get ()));

				case timestampTimezoneHour:

					return successResultPresent (
						timeFormatter.timestampTimezoneHourString (
							timezone,
							genericValue.get ()));

				case timestampTimezoneSecondShort:

					return successResultPresent (
						timeFormatter.timestampTimezoneSecondShortString (
							timezone,
							genericValue.get ()));

				case timestampTimezoneMinuteShort:

					return successResultPresent (
						timeFormatter.timestampTimezoneMinuteShortString (
							timezone,
							genericValue.get ()));

				case timestampTimezoneHourShort:

					return successResultPresent (
						timeFormatter.timestampTimezoneHourShortString (
							timezone,
							genericValue.get ()));

				case timestampTimezoneSecondLong:

					return successResultPresent (
						timeFormatter.timestampTimezoneSecondLongString (
							timezone,
							genericValue.get ()));

				case timestampTimezoneMinuteLong:

					return successResultPresent (
						timeFormatter.timestampTimezoneMinuteLongString (
							timezone,
							genericValue.get ()));

				case timestampTimezoneHourLong:

					return successResultPresent (
						timeFormatter.timestampTimezoneHourLongString (
							timezone,
							genericValue.get ()));

				case dateLong:

					return successResultPresent (
						timeFormatter.dateStringLong (
							timezone,
							genericValue.get ()));

				case dateShort:

					return successResultPresent (
						timeFormatter.dateStringShort (
							timezone,
							genericValue.get ()));

				case time:

					return successResultPresent (
						timeFormatter.timeString (
							timezone,
							genericValue.get ()));

				default:

					throw new RuntimeException ();

				}

			} else {

				switch (format) {

				case timestampSecond:

					return successResultPresent (
						timeFormatter.timestampSecondString (
							genericValue.get ()));

				case timestampMinute:

					return successResultPresent (
						timeFormatter.timestampMinuteString (
							genericValue.get ()));

				case timestampHour:

					return successResultPresent (
						timeFormatter.timestampHourString (
							genericValue.get ()));

				case timestampTimezoneSecond:

					return successResultPresent (
						timeFormatter.timestampTimezoneSecondString (
							genericValue.get ()));

				case timestampTimezoneMinute:

					return successResultPresent (
						timeFormatter.timestampTimezoneMinuteString (
							genericValue.get ()));

				case timestampTimezoneHour:

					return successResultPresent (
						timeFormatter.timestampTimezoneHourString (
							genericValue.get ()));

				case dateLong:

					return successResultPresent (
						timeFormatter.dateStringLong (
							genericValue.get ()));

				case dateShort:

					return successResultPresent (
						timeFormatter.dateStringShort (
							genericValue.get ()));

				case time:

					return successResultPresent (
						timeFormatter.timeString (
							genericValue.get ()));

				default:

					throw new RuntimeException ();

				}

			}

		}

	}

}
