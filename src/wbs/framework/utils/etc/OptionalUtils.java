package wbs.framework.utils.etc;

import static wbs.framework.utils.etc.StringUtils.stringFormat;
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
	Optional <Type> optionalFromJava (
			@NonNull java.util.Optional <Type> javaOptional) {

		if (javaOptional.isPresent ()) {

			return Optional.of (
				javaOptional.get ());

		} else {

			return Optional.absent ();

		}

	}

	public static
	boolean optionalEqualAndPresentSafe (
			@NonNull Optional <?> optional0,
			@NonNull Optional <?> optional1) {

		if (
			! optional0.isPresent ()
			|| ! optional1.isPresent ()
		) {
			return false;
		}

		if (optional0.getClass () != optional1.getClass ()) {
			throw new IllegalArgumentException ();
		}

		return optional0.get ().equals (
			optional1.get ());

	}

	public static
	boolean optionalNotEqualAndPresentSafe (
			@NonNull Optional <?> optional0,
			@NonNull Optional <?> optional1) {

		if (
			! optional0.isPresent ()
			|| ! optional1.isPresent ()
		) {
			return true;
		}

		if (optional0.getClass () != optional1.getClass ()) {

			throw new ClassCastException (
				stringFormat (
					"Tried to compare a %s to a %s",
					optional0.get ().getClass ().getSimpleName (),
					optional1.get ().getClass ().getSimpleName ()));

		}

		return ! optional0.get ().equals (
			optional1.get ());

	}

	public static <Type>
	boolean optionalEqualAndPresentWithClass (
			@NonNull Class <Type> valueClass,
			@NonNull Optional <Type> optional0,
			@NonNull Optional <Type> optional1) {

		if (
			optional0.isPresent ()
			&& ! valueClass.isInstance (
				optional0.get ())
		) {

			throw new ClassCastException (
				stringFormat (
					"Generic type violation passing %s as %s",
					optional0.get ().getClass ().getSimpleName (),
					valueClass.getSimpleName ()));

		}

		if (
			optional1.isPresent ()
			&& ! valueClass.isInstance (
				optional1.get ())
		) {

			throw new ClassCastException (
				stringFormat (
					"Generic type violation passing %s as %s",
					optional1.get ().getClass ().getSimpleName (),
					valueClass.getSimpleName ()));

		}

		if (
			! optional0.isPresent ()
			|| ! optional1.isPresent ()
		) {
			return false;
		}

		return optional0.get ().equals (
			optional1.get ());

	}

	public static <Type>
	boolean optionalNotEqualAndPresentWithClass (
			@NonNull Class <Type> valueClass,
			@NonNull Optional <Type> optional0,
			@NonNull Optional <Type> optional1) {

		if (
			optional0.isPresent ()
			&& ! valueClass.isInstance (
				optional0.get ())
		) {

			throw new ClassCastException (
				stringFormat (
					"Generic type violation passing %s as %s",
					optional0.get ().getClass ().getSimpleName (),
					valueClass.getSimpleName ()));

		}

		if (
			optional1.isPresent ()
			&& ! valueClass.isInstance (
				optional1.get ())
		) {

			throw new ClassCastException (
				stringFormat (
					"Generic type violation passing %s as %s",
					optional1.get ().getClass ().getSimpleName (),
					valueClass.getSimpleName ()));

		}

		if (
			! optional0.isPresent ()
			|| ! optional1.isPresent ()
		) {
			return true;
		}

		return ! optional0.get ().equals (
			optional1.get ());

	}

	public static
	boolean optionalEqualOrNotPresentSafe (
			@NonNull Optional <?> optional0,
			@NonNull Optional <?> optional1) {

		if (
			! optional0.isPresent ()
			&& ! optional1.isPresent ()
		) {
			return true;
		}

		if (
			! optional0.isPresent ()
			|| ! optional1.isPresent ()
		) {
			return false;
		}

		if (optional0.get ().getClass () != optional1.get ().getClass ()) {

			throw new ClassCastException (
				stringFormat (
					"Tried to compare a %s to a %s",
					optional0.get ().getClass ().getSimpleName (),
					optional1.get ().getClass ().getSimpleName ()));

		}

		return optional0.get ().equals (
			optional1.get ());

	}

	public static
	boolean optionalNotEqualOrNotPresentSafe (
			@NonNull Optional <?> optional0,
			@NonNull Optional <?> optional1) {

		if (
			! optional0.isPresent ()
			&& ! optional1.isPresent ()
		) {
			return false;
		}

		if (
			! optional0.isPresent ()
			|| ! optional1.isPresent ()
		) {
			return true;
		}

		if (optional0.get ().getClass () != optional1.get ().getClass ()) {

			throw new ClassCastException (
				stringFormat (
					"Tried to compare a %s to a %s",
					optional0.get ().getClass ().getSimpleName (),
					optional1.get ().getClass ().getSimpleName ()));

		}

		return ! optional0.get ().equals (
			optional1.get ());

	}

	public static <Type>
	boolean optionalEqualOrNotPresentWithClass (
			@NonNull Class <Type> valueClass,
			@NonNull Optional <Type> optional0,
			@NonNull Optional <Type> optional1) {

		// verify class instances

		if (
			optional0.isPresent ()
			&& ! valueClass.isInstance (
				optional0.get ())
		) {

			throw new ClassCastException (
				stringFormat (
					"Generic type violation passing %s as %s",
					optional0.get ().getClass ().getSimpleName (),
					valueClass.getSimpleName ()));

		}

		if (
			optional1.isPresent ()
			&& ! valueClass.isInstance (
				optional1.get ())
		) {

			throw new ClassCastException (
				stringFormat (
					"Generic type violation passing %s as %s",
					optional1.get ().getClass ().getSimpleName (),
					valueClass.getSimpleName ()));

		}

		// handle not present

		if (
			! optional0.isPresent ()
			&& ! optional1.isPresent ()
		) {
			return true;
		}

		if (
			! optional0.isPresent ()
			|| ! optional1.isPresent ()
		) {
			return false;
		}

		// regular equals

		return optional0.get ().equals (
			optional1.get ());

	}

	public static <Type>
	boolean optionalNotEqualOrNotPresentWithClass (
			@NonNull Class <Type> valueClass,
			@NonNull Optional <Type> optional0,
			@NonNull Optional <Type> optional1) {

		// verify class instances

		if (
			optional0.isPresent ()
			&& ! valueClass.isInstance (
				optional0.get ())
		) {

			throw new ClassCastException (
				stringFormat (
					"Generic type violation passing %s as %s",
					optional0.get ().getClass ().getSimpleName (),
					valueClass.getSimpleName ()));

		}

		if (
			optional1.isPresent ()
			&& ! valueClass.isInstance (
				optional1.get ())
		) {

			throw new ClassCastException (
				stringFormat (
					"Generic type violation passing %s as %s",
					optional1.get ().getClass ().getSimpleName (),
					valueClass.getSimpleName ()));

		}

		// handle not present

		if (
			! optional0.isPresent ()
			&& ! optional1.isPresent ()
		) {
			return false;
		}

		if (
			! optional0.isPresent ()
			|| ! optional1.isPresent ()
		) {
			return true;
		}

		// regular equals

		return ! optional0.get ().equals (
			optional1.get ());

	}

	public static <Type>
	boolean optionalValueEqualSafe (
			@NonNull Optional <Type> optional,
			@NonNull Type value) {

		if (! optional.isPresent ()) {
			return false;
		}

		if (optional.get ().getClass () != value.getClass ()) {

			throw new ClassCastException (
				stringFormat (
					"Tried to compare a %s to a %s",
					optional.get ().getClass ().getSimpleName (),
					value.getClass ().getSimpleName ()));

		}

		return optional.get ().equals (
			value);

	}

	public static <Type>
	boolean optionalValueNotEqualSafe (
			@NonNull Optional <Type> optional,
			@NonNull Type value) {

		if (! optional.isPresent ()) {
			return true;
		}

		if (optional.get ().getClass () != value.getClass ()) {

			throw new ClassCastException (
				stringFormat (
					"Tried to compare a %s to a %s",
					optional.get ().getClass ().getSimpleName (),
					value.getClass ().getSimpleName ()));

		}

		return ! optional.get ().equals (
			value);

	}

	public static <Type>
	boolean optionalValueEqualWithClass (
			@NonNull Class <Type> valueClass,
			@NonNull Optional <Type> optional,
			@NonNull Type value) {

		// verify class instances

		if (
			optional.isPresent ()
			&& ! valueClass.isInstance (
				optional.get ())
		) {
			throw new ClassCastException ();
		}

		if (
			! valueClass.isInstance (
				value)
		) {

			throw new ClassCastException (
				stringFormat (
					"Tried to compare a %s to a %s",
					optional.get ().getClass ().getSimpleName (),
					value.getClass ().getSimpleName ()));

		}

		// handle not present

		if (! optional.isPresent ()) {
			return false;
		}

		// regular equals

		return optional.get ().equals (
			value);

	}

	public static <Type>
	boolean optionalValueNotEqualWithClass (
			@NonNull Class <Type> valueClass,
			@NonNull Optional <Type> optional,
			@NonNull Type value) {

		// verify class instances

		if (
			optional.isPresent ()
			&& ! valueClass.isInstance (
				optional.get ())
		) {
			throw new ClassCastException ();
		}

		if (
			! valueClass.isInstance (
				value)
		) {

			throw new ClassCastException (
				stringFormat (
					"Tried to compare a %s to a %s",
					optional.get ().getClass ().getSimpleName (),
					value.getClass ().getSimpleName ()));

		}

		// handle not present

		if (! optional.isPresent ()) {
			return true;
		}

		// regular equals

		return ! optional.get ().equals (
			value);

	}

	public static
	boolean optionalIsPresent (
			@NonNull Optional <?> optional) {

		return optional.isPresent ();

	}

	public static
	boolean optionalIsNotPresent (
			@NonNull Optional <?> optional) {

		return ! optional.isPresent ();

	}

	public static <Type>
	Type optionalGetRequired (
			@NonNull Optional <Type> optional) {

		return optional.get ();

	}

	public static <Type>
	Type optionalOrNull (
			@NonNull Optional <Type> optional) {

		return optional.orNull ();

	}

	public static <Type>
	Type optionalOrElse (
			@NonNull Optional <Type> optional,
			@NonNull Supplier <Type> orElse) {

		if (optional.isPresent ()) {

			return optional.get ();

		} else {

			return orElse.get ();

		}

	}

	public static <Type>
	Optional <Type> requiredOptional (
			@NonNull Optional <Type> optional) {

		if (! optional.isPresent ()) {
			throw new RuntimeException ();
		}

		return optional;

	}

	public static <Type>
	Iterable <Type> presentInstances (
			@NonNull Iterable <Optional <Type>> collection) {

		return Optional.presentInstances (
			collection);

	}

	public static <Type>
	Iterable <Type> presentInstances () {

		return ImmutableList.of ();

	}

	public static <Type>
	Iterable <Type> presentInstances (
			@NonNull Optional <Type> argument) {

		return presentInstances (
			ImmutableList.of (
				argument));

	}

	public static <Type>
	Iterable <Type> presentInstances (
			@NonNull Optional <Type> argument0,
			@NonNull Optional <Type> argument1) {

		return presentInstances (
			ImmutableList.of (
				argument0,
				argument1));

	}

	public static <Type>
	Iterable<Type> presentInstances (
			@NonNull Optional <Type> argument0,
			@NonNull Optional <Type> argument1,
			@NonNull Optional <Type> argument2) {

		return presentInstances (
			ImmutableList.of (
				argument0,
				argument1,
				argument2));

	}

	public static <Type>
	Iterable<Type> presentInstances (
			@NonNull Optional <Type> argument0,
			@NonNull Optional <Type> argument1,
			@NonNull Optional <Type> argument2,
			@NonNull Optional <Type> argument3) {

		return presentInstances (
			ImmutableList.of (
				argument0,
				argument1,
				argument2,
				argument3));

	}

	@SafeVarargs
	public static <Type>
	Iterable <Type> presentInstances (
			@NonNull Optional <Type>... arguments) {

		return Optional.presentInstances (
			Arrays.asList (
				arguments));

	}

	public static <Type>
	Optional <Type> optionalIf (
			@NonNull Boolean present,
			@NonNull Supplier <Type> valueSupplier) {

		return present
			? Optional.of (
				valueSupplier.get ())
			: Optional.absent ();

	}

	public static <T>
	T optionalOr (
			Optional <T> optional,
			T instead) {

		return optional.or (
			instead);

	}

	@SafeVarargs
	public static <Type>
	Type ifNotPresent (
			@NonNull Optional <Type>... optionalValues) {

		for (
			Optional <Type> optionalValue
				: optionalValues
		) {

			if (
				optionalIsPresent (
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
			optionalIsPresent (
				optionalValueOne)
		) {

			return optionalValueOne.get ();

		}

		throw new IllegalArgumentException ();

	}

	public static <Type>
	Type ifNotPresent (
			@NonNull Optional <? extends Type> optionalValueOne,
			@NonNull Optional <? extends Type> optionalValueTwo) {

		if (
			optionalIsPresent (
				optionalValueOne)
		) {

			return optionalValueOne.get ();

		}

		if (
			optionalIsPresent (
				optionalValueTwo)
		) {

			return optionalValueTwo.get ();

		}

		throw new IllegalArgumentException ();

	}

	public static <Type>
	Type ifNotPresent (
			@NonNull Optional <? extends Type> optionalValueOne,
			@NonNull Optional <? extends Type> optionalValueTwo,
			@NonNull Optional <? extends Type> optionalValueThree) {

		if (
			optionalIsPresent (
				optionalValueOne)
		) {

			return optionalValueOne.get ();

		}

		if (
			optionalIsPresent (
				optionalValueTwo)
		) {

			return optionalValueTwo.get ();

		}

		if (
			optionalIsPresent (
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
			optionalIsPresent (
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
			optionalIsPresent (
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
			optionalIsPresent (
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
