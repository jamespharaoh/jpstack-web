package wbs.framework.utils.etc;

import static wbs.framework.utils.etc.TypeUtils.dynamicCast;
import static wbs.framework.utils.etc.TypeUtils.isInstanceOf;

import java.util.Arrays;
import java.util.function.Function;
import java.util.function.Supplier;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;

import lombok.NonNull;

public
class OptionalUtils {

	public static <Type>
	Optional<Type> optionalToGoogle (
			@NonNull java.util.Optional<Type> javaOptional) {

		if (javaOptional.isPresent ()) {

			return Optional.of (
				javaOptional.get ());

		} else {

			return Optional.absent ();

		}

	}

	public static <Type>
	boolean optionalEquals (
			@NonNull Optional<Type> left,
			@NonNull Type right) {

		if (left.isPresent ()) {

			return left.get ().equals (
				right);

		} else {

			return false;

		}

	}

	@SafeVarargs
	public static <Type>
	boolean optionalIn (
			@NonNull Optional<Type> left,
			@NonNull Type... rights) {

		if (left.isPresent ()) {

			return Misc.in (
				left.get (),
				rights);

		} else {

			return false;

		}

	}

	@SafeVarargs
	public static <Type>
	boolean optionalNotIn (
			@NonNull Optional<Type> left,
			@NonNull Type... rights) {

		if (left.isPresent ()) {

			return ! Misc.in (
				left.get (),
				rights);

		} else {

			return true;

		}

	}

	public static
	boolean isPresent (
			@NonNull Optional<?> optional) {

		return optional.isPresent ();

	}

	public static
	boolean isNotPresent (
			@NonNull Optional<?> optional) {

		return ! optional.isPresent ();

	}

	public static <Type>
	Type optionalRequired (
			@NonNull Optional<Type> optional) {

		return optional.get ();

	}

	public static <Type>
	Type optionalOrNull (
			@NonNull Optional<Type> optional) {

		return optional.orNull ();

	}

	public static <Type>
	Type optionalOrElse (
			@NonNull Optional<Type> optional,
			@NonNull Supplier<Type> orElse) {

		if (optional.isPresent ()) {

			return optional.get ();

		} else {

			return orElse.get ();

		}

	}

	public static <Type>
	Optional<Type> requiredOptional (
			@NonNull Optional<Type> optional) {

		if (! optional.isPresent ()) {
			throw new RuntimeException ();
		}

		return optional;

	}

	public static <Type>
	Iterable<Type> presentInstances (
			@NonNull Iterable<Optional<Type>> collection) {

		return Optional.presentInstances (
			collection);

	}

	public static <Type>
	Iterable<Type> presentInstances () {

		return ImmutableList.<Type>of ();

	}

	public static <Type>
	Iterable<Type> presentInstances (
			@NonNull Optional<Type> argument) {

		return presentInstances (
			ImmutableList.<Optional<Type>>of (
				argument));

	}

	public static <Type>
	Iterable<Type> presentInstances (
			@NonNull Optional<Type> argument0,
			@NonNull Optional<Type> argument1) {

		return presentInstances (
			ImmutableList.<Optional<Type>>of (
				argument0,
				argument1));

	}

	public static <Type>
	Iterable<Type> presentInstances (
			@NonNull Optional<Type> argument0,
			@NonNull Optional<Type> argument1,
			@NonNull Optional<Type> argument2) {

		return presentInstances (
			ImmutableList.<Optional<Type>>of (
				argument0,
				argument1,
				argument2));

	}

	public static <Type>
	Iterable<Type> presentInstances (
			@NonNull Optional<Type> argument0,
			@NonNull Optional<Type> argument1,
			@NonNull Optional<Type> argument2,
			@NonNull Optional<Type> argument3) {

		return presentInstances (
			ImmutableList.<Optional<Type>>of (
				argument0,
				argument1,
				argument2,
				argument3));

	}

	@SafeVarargs
	public static <Type>
	Iterable<Type> presentInstances (
			@NonNull Optional<Type>... arguments) {

		return Optional.presentInstances (
			Arrays.asList (
				arguments));

	}

	public static <Type>
	Optional<Type> optionalIf (
			@NonNull Boolean present,
			@NonNull Type value) {

		return present
			? Optional.<Type>of (
				value)
			: Optional.<Type>absent ();

	}

	public static <T>
	T optionalOr (
			Optional<T> optional,
			T instead) {

		return optional.or (
			instead);

	}

	@SafeVarargs
	public static <Type>
	Type ifNotPresent (
			@NonNull Optional<Type>... optionalValues) {

		for (
			Optional<Type> optionalValue
				: optionalValues
		) {

			if (
				isPresent (
					optionalValue)
			) {

				return optionalValue.get ();

			}

		}

		throw new IllegalArgumentException ();

	}

	public static <Type>
	Type ifNotPresent (
			@NonNull Optional<? extends Type> optionalValueOne) {

		if (
			isPresent (
				optionalValueOne)
		) {

			return optionalValueOne.get ();

		}

		throw new IllegalArgumentException ();

	}

	public static <Type>
	Type ifNotPresent (
			@NonNull Optional<? extends Type> optionalValueOne,
			@NonNull Optional<? extends Type> optionalValueTwo) {

		if (
			isPresent (
				optionalValueOne)
		) {

			return optionalValueOne.get ();

		}

		if (
			isPresent (
				optionalValueTwo)
		) {

			return optionalValueTwo.get ();

		}

		throw new IllegalArgumentException ();

	}

	public static <Type>
	Type ifNotPresent (
			@NonNull Optional<? extends Type> optionalValueOne,
			@NonNull Optional<? extends Type> optionalValueTwo,
			@NonNull Optional<? extends Type> optionalValueThree) {

		if (
			isPresent (
				optionalValueOne)
		) {

			return optionalValueOne.get ();

		}

		if (
			isPresent (
				optionalValueTwo)
		) {

			return optionalValueTwo.get ();

		}

		if (
			isPresent (
				optionalValueThree)
		) {

			return optionalValueThree.get ();

		}

		throw new IllegalArgumentException ();

	}

	public static <Type>
	Optional <Type> optionalCast (
			@NonNull Class <Type> classToCastTo,
			@NonNull Optional <?> optionalValue) {

		if (
			isPresent (
				optionalValue)
		) {

			if (
				isInstanceOf (
					classToCastTo,
					optionalValue.get ())
			) {

				return Optional.of (
					dynamicCast (
						classToCastTo,
						optionalValue.get ()));

			} else {

				throw new ClassCastException ();

			}

		} else {

			return Optional.absent ();

		}

	}

	public static <From,To>
	Optional<To> optionalMapRequired (
			@NonNull Optional<From> optionalValue,
			@NonNull Function<? super From,To> mappingFunction) {

		if (
			isPresent (
				optionalValue)
		) {

			return Optional.of (
				mappingFunction.apply (
					optionalValue.get ()));

		} else {

			return Optional.absent ();

		}

	}

	public static <From,To>
	Optional<To> optionalMapOptional (
			@NonNull Optional<From> optionalValue,
			@NonNull Function<? super From,Optional<To>> mappingFunction) {

		if (
			isPresent (
				optionalValue)
		) {

			return mappingFunction.apply (
				optionalValue.get ());

		} else {

			return Optional.absent ();

		}

	}

	public static <Type>
	Optional<Type> optionalFromNullable (
			Type value) {

		return Optional.fromNullable (
			value);

	}

}
