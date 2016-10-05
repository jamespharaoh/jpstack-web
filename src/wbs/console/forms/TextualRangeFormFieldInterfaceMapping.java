package wbs.console.forms;

import static wbs.utils.collection.CollectionUtils.collectionDoesNotHaveTwoElements;
import static wbs.utils.collection.CollectionUtils.listFirstElementRequired;
import static wbs.utils.collection.CollectionUtils.listSecondElementRequired;
import static wbs.utils.etc.Misc.errorResult;
import static wbs.utils.etc.Misc.getError;
import static wbs.utils.etc.Misc.getValue;
import static wbs.utils.etc.Misc.isError;
import static wbs.utils.etc.Misc.successResult;
import static wbs.utils.etc.OptionalUtils.optionalAbsent;
import static wbs.utils.etc.OptionalUtils.optionalGetRequired;
import static wbs.utils.etc.OptionalUtils.optionalIsNotPresent;
import static wbs.utils.etc.OptionalUtils.optionalOf;
import static wbs.utils.string.StringUtils.stringFormat;
import static wbs.utils.string.StringUtils.stringSplitColon;
import static wbs.utils.string.StringUtils.stringSplitSimple;

import java.util.List;
import java.util.Map;

import com.google.common.base.Optional;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

import org.apache.commons.lang3.Range;

import org.apache.tomcat.util.http.Parameters.FailReason;

import wbs.framework.component.annotations.PrototypeComponent;

import fj.data.Either;

@Accessors (fluent = true)
@PrototypeComponent ("textualRangeFormFieldInterfaceMapping")
public
class TextualRangeFormFieldInterfaceMapping <
	Container,
	Generic extends Comparable <Generic>
>
	implements FormFieldInterfaceMapping <
		Container,
		Range <Generic>,
		String
	> {

	// properties

	@Getter @Setter
	FormFieldInterfaceMapping <Container, Generic, String> itemMapping;

	// implementation

	@Override
	public
	Either <Optional <String>, String> genericToInterface (
			@NonNull Container container,
			@NonNull Map <String, Object> hints,
			@NonNull Optional <Range <Generic>> genericValue) {

		if (
			optionalIsNotPresent (
				genericValue)
		) {

			return successResult (
				Optional.absent ());

		}

		// get minimum

		Either <Optional <String>, String> leftResult =
			itemMapping.genericToInterface (
				container,
				hints,
				optionalOf (
					genericValue.get ().getMinimum ()));

		if (
			isError (
				leftResult)
		) {

			return errorResult (
				getError (
					leftResult));

		}

		if (
			optionalIsNotPresent (
				getValue (
					leftResult))
		) {

			return successResult (
				Optional.absent ());

		}

		// get maximum

		Either <Optional <String>, String> rightResult =
			itemMapping.genericToInterface (
				container,
				hints,
				Optional.of (
					genericValue.get ().getMaximum ()));

		if (
			isError (
				rightResult)
		) {

			return errorResult (
				getError (
					rightResult));

		}

		if (
			optionalIsNotPresent (
				getValue (
					rightResult))
		) {

			return successResult (
				Optional.absent ());

		}

		// return

		return successResult (
			optionalOf (
				stringFormat (
					"%s to %s",
					optionalGetRequired (
						getValue (
							leftResult)),
					optionalGetRequired (
						getValue (
							rightResult)))));

	}

	@Override
	public
	Either <Optional <Range <Generic>>, String> interfaceToGeneric (
			@NonNull Container container,
			@NonNull Map <String, Object> hints,
			@NonNull Optional <String> interfaceValue) {

		if (
			optionalIsNotPresent (
				interfaceValue)
		) {

			return successResult (
				Optional.absent ());

		}

		// split minimum and maximum

		List <String> interfaceParts =
			stringSplitSimple (
				" to ",
				interfaceValue.get ());

		if (
			collectionDoesNotHaveTwoElements (
				interfaceParts)
		) {

			return errorResult (
				"Please enter a valid range, eg \"min to max\"");

		}

		// get minimum

		Either <Optional <Generic>, String> leftResult =
			itemMapping.interfaceToGeneric (
				container,
				hints,
				optionalOf (
					listFirstElementRequired (
						interfaceParts)));

		if (
			isError (
				leftResult)
		) {

			return errorResult (
				getError (
					leftResult));

		}

		if (
			optionalIsNotPresent (
				getValue (
					leftResult))
		) {

			return successResult (
				optionalAbsent ());

		}

		// get maximum

		Either <Optional <Generic>, String> rightResult =
			itemMapping.interfaceToGeneric (
				container,
				hints,
				optionalOf (
					listSecondElementRequired (
						interfaceParts)));

		if (
			isError (
				rightResult)
		) {

			return errorResult (
				getError (
					rightResult));

		}

		if (
			optionalIsNotPresent (
				getValue (
					rightResult))
		) {

			return successResult (
				optionalAbsent ());

		}

		// return

		return successResult (
			optionalOf (
				Range.between (

			optionalGetRequired (
				getValue (
					leftResult)),

			optionalGetRequired (
				getValue (
					rightResult))

		)));

	}

}
