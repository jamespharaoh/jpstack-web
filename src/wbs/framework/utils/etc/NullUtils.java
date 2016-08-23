package wbs.framework.utils.etc;

import java.util.function.Supplier;

import lombok.NonNull;

public
class NullUtils {

	@SafeVarargs
	public static <Type>
	Type ifNull (
			@NonNull Type... values) {

		for (Type value : values) {

			if (value != null)
				return value;

		}

		return null;

	}

	public static <Type>
	Type ifNull (
			Type input,
			Type ifNull) {

		return input == null
			? ifNull
			: input;

	}

	@SafeVarargs
	public static <Type>
	Type ifNullThenRequired (
			@NonNull Supplier <? extends Type> ... valueSuppliers) {

		for (
			Supplier <? extends Type> valueSupplier
				: valueSuppliers
		) {

			Type value =
				valueSupplier.get ();

			if (value != null) {
				return value;
			}

		}

		throw new NullPointerException ();

	}

	public static
	<Type> Type nullIf (
			Type input,
			Type nullIf) {

		if (input == null)
			return null;

		if (nullIf != null && input.equals (nullIf))
			return null;

		return input;

	}

}
