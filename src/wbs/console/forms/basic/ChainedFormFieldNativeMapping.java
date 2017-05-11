package wbs.console.forms.basic;

import com.google.common.base.Optional;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

import wbs.console.forms.types.FormFieldNativeMapping;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.database.NestedTransaction;
import wbs.framework.database.Transaction;
import wbs.framework.logging.LogContext;

@Accessors (fluent = true)
@PrototypeComponent ("chainedFormFieldNativeMapping")
public
class ChainedFormFieldNativeMapping <Container, Generic, Temporary, Native>
	implements FormFieldNativeMapping <Container, Generic, Native> {

	// singleton components

	@ClassSingletonDependency
	LogContext logContext;

	// properties

	@Getter @Setter
	FormFieldNativeMapping <Container, Generic, Temporary> previousMapping;

	@Getter @Setter
	FormFieldNativeMapping <Container, Temporary, Native> nextMapping;

	// implementation

	@Override
	public
	Optional <Generic> nativeToGeneric (
			@NonNull Transaction parentTransaction,
			@NonNull Container container,
			@NonNull Optional <Native> nativeValue) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"nativeToGeneric");

		) {

			Optional <Temporary> temporaryValue =
				nextMapping.nativeToGeneric (
					transaction,
					container,
					nativeValue);

			Optional<Generic> genericValue =
				previousMapping.nativeToGeneric (
					transaction,
					container,
					temporaryValue);

			return genericValue;

		}

	}

	@Override
	public
	Optional <Native> genericToNative (
			@NonNull Transaction parentTransaction,
			@NonNull Container container,
			@NonNull Optional <Generic> genericValue) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"genericToNative");

		) {

			Optional <Temporary> temporaryValue =
				previousMapping.genericToNative (
					transaction,
					container,
					genericValue);

			Optional <Native> nativeValue =
				nextMapping.genericToNative (
					transaction,
					container,
					temporaryValue);

			return nativeValue;

		}

	}

}
