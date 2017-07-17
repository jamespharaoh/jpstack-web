package wbs.console.forms.basic;

import static wbs.utils.etc.OptionalUtils.optionalAbsent;
import static wbs.utils.etc.OptionalUtils.optionalFromNullable;
import static wbs.utils.etc.OptionalUtils.optionalOf;
import static wbs.utils.etc.OptionalUtils.optionalOr;

import com.google.common.base.Optional;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

import org.apache.commons.lang3.tuple.Pair;

import wbs.console.forms.types.FormFieldAccessor;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.database.NestedTransaction;
import wbs.framework.database.Transaction;
import wbs.framework.logging.LogContext;

@Accessors (fluent = true)
@PrototypeComponent ("pairFormFieldAccessor")
public
class PairFormFieldAccessor <Container, Left, Right>
	implements FormFieldAccessor <Container, Pair <Left, Right>> {

	// singleton dependencies

	@ClassSingletonDependency
	LogContext logContext;

	// properties

	@Getter @Setter
	FormFieldAccessor <Container, Left> leftAccessor;

	@Getter @Setter
	FormFieldAccessor <Container, Right> rightAccessor;

	// implementation

	@Override
	public
	Optional <Pair <Left, Right>> read (
			@NonNull Transaction parentTransaction,
			@NonNull Container container) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"read");

		) {

			// special case for null container

			/*
			if (container == null) {
				return Optional.<Pair<Left,Right>>absent ();
			}
			*/

			// get native values

			Optional <Left> leftValue =
				leftAccessor.read (
					transaction,
					container);

			Optional<Right> rightValue =
				rightAccessor.read (
					transaction,
					container);

			// return as pair

			if (
				! leftValue.isPresent ()
				&& ! rightValue.isPresent ()
			) {
				return optionalAbsent ();
			}

			return optionalOf (
				Pair.of (
					leftValue.orNull (),
					rightValue.orNull ()));

		}

	}

	@Override
	public
	Optional <String> write (
			@NonNull Transaction parentTransaction,
			@NonNull Container container,
			@NonNull Optional <Pair <Left, Right>> nativeValueOptional) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"write");

		) {

			// special case for null

			Pair <Left, Right> nativeValue =
				optionalOr (
					nativeValueOptional,
					Pair.of (null, null));

			// write values

			leftAccessor.write (
				transaction,
				container,
				optionalFromNullable (
					nativeValue.getLeft ()));

			rightAccessor.write (
				transaction,
				container,
				optionalFromNullable (
					nativeValue.getRight ()));

			// return

			return optionalAbsent ();

		}

	}

}
