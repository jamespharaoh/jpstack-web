package wbs.platform.currency.console;

import static wbs.utils.etc.Misc.isNull;
import static wbs.utils.etc.OptionalUtils.optionalAbsent;
import static wbs.utils.etc.OptionalUtils.optionalIsNotPresent;
import static wbs.utils.etc.ResultUtils.errorResult;
import static wbs.utils.etc.ResultUtils.successResult;
import static wbs.utils.etc.ResultUtils.successResultAbsent;
import static wbs.utils.string.StringUtils.stringFormat;

import java.util.Map;

import com.google.common.base.Optional;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

import wbs.console.forms.FormFieldInterfaceMapping;

import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.logging.TaskLogger;
import wbs.framework.object.ObjectManager;

import wbs.platform.currency.logic.CurrencyLogic;
import wbs.platform.currency.model.CurrencyRec;

import fj.data.Either;

@Accessors (fluent = true)
@PrototypeComponent ("currencyFormFieldInterfaceMapping")
public
class CurrencyFormFieldInterfaceMapping <Container>
	implements FormFieldInterfaceMapping <Container, Long, String> {

	// singleton dependencies

	@SingletonDependency
	CurrencyLogic currencyLogic;

	@SingletonDependency
	ObjectManager objectManager;

	// properties

	@Getter @Setter
	String currencyPath;

	@Getter @Setter
	Boolean blankIfZero = false;

	// implementation

	@Override
	public
	Either <Optional <Long>, String> interfaceToGeneric (
			@NonNull Container container,
			@NonNull Map <String, Object> hints,
			@NonNull Optional <String> interfaceValue) {

		if (! interfaceValue.isPresent ()) {

			return successResult (
				optionalAbsent ());

		}

		if (interfaceValue.get ().isEmpty ()) {

			return successResult (
				optionalAbsent ());

		}

		CurrencyRec currency =
			(CurrencyRec)
			objectManager.dereferenceObsolete (
				container,
				currencyPath,
				hints);

		if (
			isNull (
				currency)
		) {

			try {

				return successResult (
					Optional.of (
						Long.parseLong (
							interfaceValue.get ())));

			} catch (NumberFormatException exception) {

				return errorResult (
					"This currency value must be a whole number");

			}

		}

		Optional<Long> parseResult =
			currencyLogic.parseText (
				currency,
				interfaceValue.get ());

		if (
			optionalIsNotPresent (
				parseResult)
		) {

			return errorResult (
				stringFormat (
					"A currency value must be numeric and include the ",
					"appropriate decimal places"));

		}

		return successResult (
			Optional.of (
				currencyLogic.parseTextRequired (
					currency,
					interfaceValue.get ())));

	}

	@Override
	public
	Either <Optional <String>, String> genericToInterface (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull Container container,
			@NonNull Map <String, Object> hints,
			@NonNull Optional <Long> genericValue) {

		if (
			optionalIsNotPresent (
				genericValue)
		) {

			return successResultAbsent ();

		}

		CurrencyRec currency =
			(CurrencyRec)
			objectManager.dereferenceObsolete (
				container,
				currencyPath,
				hints);

		if (genericValue.get () == 0 && blankIfZero) {

			return successResult (
				Optional.of (
					""));

		}

		if (currency != null) {

			return successResult (
				Optional.of (
					currencyLogic.formatText (
						currency,
						genericValue.get ())));

		} else {

			return successResult (
				Optional.of (
					Long.toString (
						genericValue.get ())));

		}


	}

}
