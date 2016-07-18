package wbs.console.forms;

import static wbs.framework.utils.etc.Misc.equal;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

import com.google.common.base.Optional;

import wbs.console.helper.ConsoleHelper;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.record.Record;

@Accessors (fluent = true)
@PrototypeComponent ("objectIdFormFieldNativeMapping")
public
class ObjectIdFormFieldNativeMapping<Container,Type extends Record<Type>,Native>
	implements FormFieldNativeMapping<Container,Type,Native> {

	// properties

	@Getter @Setter
	ConsoleHelper<Type> consoleHelper;

	@Getter @Setter
	Class<Native> propertyClass;

	// implementation

	@Override
	public
	Optional<Type> nativeToGeneric (
			@NonNull Container container,
			@NonNull Optional<Native> nativeValue) {

		if (! nativeValue.isPresent ()) {
			return Optional.<Type>absent ();
		}

		Integer objectId;

		if (
			equal (
				propertyClass,
				Integer.class)
		) {

			objectId =
				(Integer)
				nativeValue.get ();

		} else if (
			equal (
				propertyClass,
				Long.class)
		) {

			objectId =
				(int) (long) (Long)
				nativeValue.get ();

		} else {

			throw new RuntimeException ();

		}

		return Optional.of (
			consoleHelper.findRequired (
				objectId));

	}

	@Override
	public
	Optional<Native> genericToNative (
			@NonNull Container container,
			@NonNull Optional<Type> genericValue) {

		if (! genericValue.isPresent ()) {
			return Optional.<Native>absent ();
		}

		if (
			equal (
				propertyClass,
				Integer.class)
		) {

			return Optional.of (
				propertyClass.cast (
					genericValue.get ().getId ()));

		} else if (
			equal (
				propertyClass,
				Long.class)
		) {

			return Optional.of (
				propertyClass.cast (
					(long) (int)
					genericValue.get ().getId ()));

		} else {

			throw new RuntimeException ();

		}

	}

}
