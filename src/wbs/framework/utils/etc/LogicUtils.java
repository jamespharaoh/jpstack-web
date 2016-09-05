package wbs.framework.utils.etc;

import static wbs.framework.utils.etc.StringUtils.stringFormat;

import java.util.List;
import java.util.function.Supplier;

import com.google.common.base.Optional;

import lombok.NonNull;

public
class LogicUtils {

	@SafeVarargs
	public static
	boolean allOf (
			@NonNull Supplier<Boolean>... conditions) {

		for (
			Supplier<Boolean> condition
				: conditions
		) {

			if (! condition.get ()) {
				return false;
			}

		}

		return true;

	}

	@SafeVarargs
	public static
	boolean anyOf (
			@NonNull Supplier<Boolean>... conditions) {

		for (
			Supplier<Boolean> condition
				: conditions
		) {

			if (condition.get ()) {
				return true;
			}

		}

		return false;

	}

	@SafeVarargs
	public static
	boolean noneOf (
			@NonNull Supplier<Boolean>... conditions) {

		for (
			Supplier<Boolean> condition
				: conditions
		) {

			if (condition.get ()) {
				return false;
			}

		}

		return true;

	}

	public static
	boolean allAreTrue (
			List<Boolean> values) {

		for (
			Boolean value
				: values
		) {

			if (! value)
				return false;

		}

		return true;

	}

	public static
	boolean anyIsTrue (
			List<Boolean> values) {

		for (
			Boolean value
				: values
		) {

			if (value)
				return true;

		}

		return false;

	}

	public static
	boolean not (
			boolean value) {

		return ! value;

	}

	public static <Type>
	Type ifThenElse (
			boolean condition,
			@NonNull Supplier <Type> trueValue,
			@NonNull Supplier <Type> falseValue) {

		if (condition) {

			return trueValue.get ();

		} else {

			return falseValue.get ();

		}

	}

	public static <Type>
	Type ifThenElse (
			@NonNull Supplier <Boolean> condition0,
			@NonNull Supplier <Type> condition0Value,
			@NonNull Supplier <Boolean> condition1,
			@NonNull Supplier <Type> condition1Value,
			@NonNull Supplier <Type> elseValue) {

		if (condition0.get ()) {

			return condition0Value.get ();

		} else if (condition1.get ()) {

			return condition1Value.get ();

		} else {

			return elseValue.get ();

		}

	}

	public static
	boolean noneIsTrue (
			List<Boolean> values) {

		for (
			Boolean value
				: values
		) {

			if (value)
				return false;

		}

		return true;

	}

	public static
	boolean equalSafe (
			@NonNull Object object0,
			@NonNull Object object1) {

		if (object0.getClass () != object1.getClass ()) {

			throw new ClassCastException (
				stringFormat (
					"Tried to compare a %s to a %s",
					object0.getClass ().getSimpleName (),
					object1.getClass ().getSimpleName ()));

		}

		return object0.equals (
			object1);

	}

	public static
	boolean notEqualSafe (
			@NonNull Object object0,
			@NonNull Object object1) {

		if (object0.getClass () != object1.getClass ()) {

			throw new ClassCastException (
				stringFormat (
					"Tried to compare a %s to a %s",
					object0.getClass ().getSimpleName (),
					object1.getClass ().getSimpleName ()));

		}

		return ! object0.equals (
			object1);

	}

	public static <Type>
	boolean equalWithClass (
			@NonNull Class <Type> objectClass,
			@NonNull Type object0,
			@NonNull Type object1) {

		return object0.equals (
			object1);

	}

	public static <Type>
	boolean notEqualWithClass (
			@NonNull Class <Type> objectClass,
			@NonNull Type object0,
			@NonNull Type object1) {

		return ! object0.equals (
			object1);

	}

	public static <Type>
	boolean referenceEqualUnsafe (
			@NonNull Type object1,
			@NonNull Type object2) {

		return object1 == object2;

	}

	public static <Type>
	boolean referenceNotEqualUnsafe (
			@NonNull Type object1,
			@NonNull Type object2) {

		return object1 != object2;

	}

	public static <Type>
	boolean referenceEqualSafe (
			@NonNull Object object0,
			@NonNull Object object1) {

		if (object0.getClass () != object1.getClass ()) {

			throw new ClassCastException (
				stringFormat (
					"Tried to compare a %s to a %s",
					object0.getClass ().getSimpleName (),
					object1.getClass ().getSimpleName ()));

		}

		return object0 == object1;

	}

	public static
	boolean referenceNotEqualSafe (
			@NonNull Object object0,
			@NonNull Object object1) {

		if (object0.getClass () != object1.getClass ()) {

			throw new ClassCastException (
				stringFormat (
					"Tried to compare a %s to a %s",
					object0.getClass ().getSimpleName (),
					object1.getClass ().getSimpleName ()));

		}

		return object0 != object1;

	}

	public static <Type>
	boolean referenceEqualWithClass (
			@NonNull Class <?> objectClass,
			@NonNull Type object1,
			@NonNull Type object2) {

		return object1 == object2;

	}

	public static <Type>
	boolean referenceNotEqualWithClass (
			@NonNull Class <?> objectClass,
			@NonNull Type object1,
			@NonNull Type object2) {

		return object1 != object2;

	}

	public static
	boolean booleanEqual (
			boolean boolean0,
			boolean boolean1) {

		return boolean0 == boolean1;

	}

	public static
	boolean booleanNotEqual (
			boolean boolean0,
			boolean boolean1) {

		return boolean0 != boolean1;

	}

	public static
	Optional <Boolean> parseBooleanYesNo (
			@NonNull String string) {

		if (string.isEmpty ()) {
			return Optional.absent ();
		}

		if (
			string.equals (
				"yes")
		) {

			return Optional.of (
				true);

		}

		if (
			string.equals (
				"no")
		) {

			return Optional.of (
				false);

		}

		throw new IllegalArgumentException ();

	}

	public static
	Optional <Boolean> parseBooleanTrueFalse (
			@NonNull String string) {

		if (string.isEmpty ()) {
			return Optional.absent ();
		}

		if (
			string.equals (
				"true")
		) {

			return Optional.of (
				true);

		}

		if (
			string.equals (
				"false")
		) {

			return Optional.of (
				false);

		}

		throw new IllegalArgumentException ();

	}

	public static
	boolean parseBooleanTrueFalseRequired (
			@NonNull String string) {

		if (
			string.equals (
				"true")
		) {
			return true;
		}

		if (
			string.equals (
				"false")
		) {
			return true;
		}

		throw new IllegalArgumentException ();

	}

}
