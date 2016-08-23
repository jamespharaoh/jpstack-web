package wbs.console.forms;

import static wbs.framework.utils.etc.Misc.errorResult;
import static wbs.framework.utils.etc.Misc.getError;
import static wbs.framework.utils.etc.Misc.getValue;
import static wbs.framework.utils.etc.Misc.isError;
import static wbs.framework.utils.etc.Misc.successResult;

import java.util.Map;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

import org.apache.commons.lang3.Range;

import com.google.common.base.Optional;

import fj.data.Either;

import wbs.framework.application.annotations.PrototypeComponent;
import static wbs.framework.utils.etc.OptionalUtils.isNotPresent;
import static wbs.framework.utils.etc.OptionalUtils.optionalGetRequired;

@Accessors (fluent = true)
@PrototypeComponent ("rangeFormFieldInterfaceMapping")
public
class RangeFormFieldInterfaceMapping<
		Container,
		Generic extends Comparable <Generic>,
		Interface extends Comparable <Interface>
	> implements FormFieldInterfaceMapping<
		Container,
		Range<Generic>,
		Range<Interface>
	> {

	// properties

	@Getter @Setter
	FormFieldInterfaceMapping<Container,Generic,Interface> left;

	@Getter @Setter
	FormFieldInterfaceMapping<Container,Generic,Interface> right;

	// implementation

	@Override
	public
	Either<Optional<Range<Interface>>,String> genericToInterface (
			@NonNull Container container,
			@NonNull Map<String,Object> hints,
			@NonNull Optional<Range<Generic>> genericValue) {

		if (
			isNotPresent (
				genericValue)
		) {

			return successResult (
				Optional.absent ());

		}

		// get minimum

		Either<Optional<Interface>,String> leftResult =
			left.genericToInterface (
				container,
				hints,
				Optional.of (
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
			isNotPresent (
				getValue (
					leftResult))
		) {

			return successResult (
				Optional.absent ());

		}

		// get maximum

		Either<Optional<Interface>,String> rightResult =
			left.genericToInterface (
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
			isNotPresent (
				getValue (
					rightResult))
		) {

			return successResult (
				Optional.absent ());

		}

		// return

		return successResult (
			Optional.of (
				Range.between (
					optionalGetRequired (
						getValue (
							leftResult)),
					optionalGetRequired (
						getValue (
							rightResult)))));

	}

	@Override
	public
	Either<Optional<Range<Generic>>,String> interfaceToGeneric (
			@NonNull Container container,
			@NonNull Map<String,Object> hints,
			@NonNull Optional<Range<Interface>> interfaceValue) {

		if (
			isNotPresent (
				interfaceValue)
		) {

			return successResult (
				Optional.absent ());

		}

		// get minimum

		Either<Optional<Generic>,String> leftResult =
			left.interfaceToGeneric (
				container,
				hints,
				Optional.of (
					interfaceValue.get ().getMinimum ()));

		if (
			isError (
				leftResult)
		) {

			return errorResult (
				getError (
					leftResult));

		}

		if (
			isNotPresent (
				getValue (
					leftResult))
		) {

			return successResult (
				Optional.absent ());

		}

		// get maximum

		Either<Optional<Generic>,String> rightResult =
			left.interfaceToGeneric (
				container,
				hints,
				Optional.of (
					interfaceValue.get ().getMaximum ()));

		if (
			isError (
				rightResult)
		) {

			return errorResult (
				getError (
					rightResult));

		}

		if (
			isNotPresent (
				getValue (
					rightResult))
		) {

			return successResult (
				Optional.absent ());

		}

		// return

		return successResult (
			Optional.of (
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
