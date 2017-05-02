package wbs.console.forms;

import static wbs.utils.etc.OptionalUtils.optionalAbsent;
import static wbs.utils.etc.OptionalUtils.optionalGetRequired;
import static wbs.utils.etc.OptionalUtils.optionalIsNotPresent;
import static wbs.utils.etc.OptionalUtils.optionalOf;
import static wbs.utils.etc.ResultUtils.errorResult;
import static wbs.utils.etc.ResultUtils.getError;
import static wbs.utils.etc.ResultUtils.isError;
import static wbs.utils.etc.ResultUtils.resultValueRequired;
import static wbs.utils.etc.ResultUtils.successResult;

import java.util.Map;

import com.google.common.base.Optional;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

import org.apache.commons.lang3.Range;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.database.NestedTransaction;
import wbs.framework.database.Transaction;
import wbs.framework.logging.LogContext;

import fj.data.Either;

@Accessors (fluent = true)
@PrototypeComponent ("rangeFormFieldInterfaceMapping")
public
class RangeFormFieldInterfaceMapping <
	Container,
	Generic extends Comparable <Generic>,
	Interface extends Comparable <Interface>
>
	implements FormFieldInterfaceMapping <
		Container,
		Range <Generic>,
		Range <Interface>
	> {

	// singleton depdendencies

	@ClassSingletonDependency
	LogContext logContext;

	// properties

	@Getter @Setter
	FormFieldInterfaceMapping <Container, Generic, Interface> itemMapping;

	// implementation

	@Override
	public
	Either <Optional <Range <Interface>>, String> genericToInterface (
			@NonNull Transaction parentTransaction,
			@NonNull Container container,
			@NonNull Map <String, Object> hints,
			@NonNull Optional <Range <Generic>> genericValue) {

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

				return successResult (
					optionalAbsent ());

			}

			// get minimum

			Either <Optional <Interface>, String> leftResult =
				itemMapping.genericToInterface (
					transaction,
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
					resultValueRequired (
						leftResult))
			) {

				return successResult (
					optionalAbsent ());

			}

			// get maximum

			Either <Optional <Interface>, String> rightResult =
				itemMapping.genericToInterface (
					transaction,
					container,
					hints,
					optionalOf (
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
					resultValueRequired (
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
							resultValueRequired (
								leftResult)),
						optionalGetRequired (
							resultValueRequired (
								rightResult)))));

		}

	}

	@Override
	public
	Either <Optional <Range <Generic>>, String> interfaceToGeneric (
			@NonNull Transaction parentTransaction,
			@NonNull Container container,
			@NonNull Map <String, Object> hints,
			@NonNull Optional <Range <Interface>> interfaceValue) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"interfaceToGeneric");

		) {

			if (
				optionalIsNotPresent (
					interfaceValue)
			) {

				return successResult (
					Optional.absent ());

			}

			// get minimum

			Either <Optional <Generic>, String> leftResult =
				itemMapping.interfaceToGeneric (
					transaction,
					container,
					hints,
					optionalOf (
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
				optionalIsNotPresent (
					resultValueRequired (
						leftResult))
			) {

				return successResult (
					Optional.absent ());

			}

			// get maximum

			Either <Optional <Generic>, String> rightResult =
				itemMapping.interfaceToGeneric (
					transaction,
					container,
					hints,
					optionalOf (
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
				optionalIsNotPresent (
					resultValueRequired (
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
					resultValueRequired (
						leftResult)),

				optionalGetRequired (
					resultValueRequired (
						rightResult))

			)));

		}

	}

}
