package wbs.console.forms;

import static wbs.utils.etc.OptionalUtils.optionalFromNullable;
import static wbs.utils.etc.OptionalUtils.optionalOr;

import com.google.common.base.Optional;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

import org.apache.commons.lang3.tuple.Pair;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.TaskLogger;

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
			@NonNull TaskLogger parentTaskLogger,
			@NonNull Container container) {

		TaskLogger taskLogger =
			logContext.nestTaskLogger (
				parentTaskLogger,
				"read");

		// special case for null container

		/*
		if (container == null) {
			return Optional.<Pair<Left,Right>>absent ();
		}
		*/

		// get native values

		Optional <Left> leftValue =
			leftAccessor.read (
				taskLogger,
				container);

		Optional<Right> rightValue =
			rightAccessor.read (
				taskLogger,
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
			@NonNull TaskLogger parentTaskLogger,
			@NonNull Container container,
			@NonNull Optional <Pair <Left, Right>> nativeValueOptional) {

		TaskLogger taskLogger =
			logContext.nestTaskLogger (
				parentTaskLogger,
				"write");

		// special case for null

		Pair <Left, Right> nativeValue =
			optionalOr (
				nativeValueOptional,
				Pair.of (null, null));

		// write values

		leftAccessor.write (
			taskLogger,
			container,
			optionalFromNullable (
				nativeValue.getLeft ()));

		rightAccessor.write (
			taskLogger,
			container,
			optionalFromNullable (
				nativeValue.getRight ()));

	}

}
