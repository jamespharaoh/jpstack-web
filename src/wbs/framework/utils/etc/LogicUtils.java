package wbs.framework.utils.etc;

import java.util.List;
import java.util.function.Supplier;

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

}
