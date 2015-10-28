package wbs.console.forms;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

import org.apache.commons.lang3.tuple.Pair;

import com.google.common.base.Optional;

import wbs.framework.application.annotations.PrototypeComponent;

@Accessors (fluent = true)
@PrototypeComponent ("pairFormFieldAccessor")
public
class PairFormFieldAccessor<Container,Left,Right>
	implements FormFieldAccessor<Container,Pair<Left,Right>> {

	// properties

	@Getter @Setter
	FormFieldAccessor<Container,Left> leftAccessor;

	@Getter @Setter
	FormFieldAccessor<Container,Right> rightAccessor;

	// implementation

	@Override
	public
	Optional<Pair<Left,Right>> read (
			@NonNull Container container) {

		// special case for null container

		/*
		if (container == null) {
			return Optional.<Pair<Left,Right>>absent ();
		}
		*/

		// get native values

		Optional<Left> leftValue =
			leftAccessor.read (
				container);

		Optional<Right> rightValue =
			rightAccessor.read (
				container);

		// return as pair

		if (
			! leftValue.isPresent ()
			&& ! rightValue.isPresent ()
		) {
			return Optional.<Pair<Left,Right>>absent ();
		}

		return Optional.of (
			Pair.of (
				leftValue.orNull (),
				rightValue.orNull ()));

	}

	@Override
	public
	void write (
			@NonNull Container container,
			@NonNull Optional<Pair<Left,Right>> nativeValue) {

		// special case for null

		if (! nativeValue.isPresent ()) {

			nativeValue =
				Optional.of (
					Pair.<Left,Right>of (
						null,
						null));

		}

		// write values

		leftAccessor.write (
			container,
			Optional.fromNullable (
				nativeValue.get ().getLeft ()));

		rightAccessor.write (
			container,
			Optional.fromNullable (
				nativeValue.get ().getRight ()));

	}

}
