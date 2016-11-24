package wbs.console.forms;

import static wbs.utils.string.StringUtils.stringFormat;

import com.google.common.base.Optional;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

import wbs.console.helper.manager.ConsoleObjectManager;

import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;

import wbs.utils.etc.PropertyUtils;

@Accessors (fluent = true)
@PrototypeComponent ("simpleFormFieldAccessor")
public
class SimpleFormFieldAccessor <Container, Native>
	implements FormFieldAccessor <Container, Native> {

	// singleton dependencies

	@SingletonDependency
	ConsoleObjectManager consoleObjectManager;

	// properties

	@Getter @Setter
	String name;

	@Getter @Setter
	Class<? extends Native> nativeClass;

	// implementation

	@Override
	public
	Optional <Native> read (
			@NonNull Container container) {

		// get native object

		Object nativeObject;

		nativeObject =
			PropertyUtils.propertyGetAuto (
				container,
				name);

		// special case for null

		if (nativeObject == null) {
			return Optional.<Native>absent ();
		}

		// sanity check native type

		if (
			! nativeClass.isInstance (
				nativeObject)
		) {

			throw new RuntimeException (
				stringFormat (
					"Field '%s' is %s, expected %s",
					name,
					nativeObject.getClass ().getSimpleName (),
					nativeClass.getSimpleName ()));

		}

		// cast and return

		return Optional.<Native>of (
			nativeClass.cast (
				nativeObject));

	}

	@Override
	public
	void write (
			@NonNull Container container,
			@NonNull Optional <Native> nativeValue) {

		// sanity check native type

		if (

			nativeValue.isPresent ()

			&& ! nativeClass.isInstance (
				nativeValue.get ())

		) {

			throw new RuntimeException (
				stringFormat (
					"Field %s.%s ",
					container.getClass ().getSimpleName (),
					name,
					"is %s, ",
					nativeClass.getSimpleName (),
					"attempted to write %s",
					nativeValue.get ().getClass ().getSimpleName ()));

		}

		// set property

		PropertyUtils.propertySetAuto (
			container,
			name,
			nativeValue.orNull ());

	}

}
