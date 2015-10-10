package wbs.console.forms;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import org.apache.commons.lang3.tuple.Pair;

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
	Pair<Left,Right> read (
			Container container) {

		// special case for null container

		if (container == null)
			return null;

		// get native values

		Left leftValue =
			leftAccessor.read (
				container);

		Right rightValue =
			rightAccessor.read (
				container);

		// return as pair

		if (leftValue == null && rightValue == null)
			return null;

		return Pair.of (
			leftValue,
			rightValue);

	}

	@Override
	public
	void write (
			Container container,
			Pair<Left,Right> nativeValue) {

		// special case for null

		if (nativeValue == null)
			nativeValue = Pair.of (null, null);

		// write values

		leftAccessor.write (
			container,
			nativeValue.getLeft ());

		rightAccessor.write (
			container,
			nativeValue.getRight ());

	}

}
