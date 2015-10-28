package wbs.console.forms;

import static wbs.framework.utils.etc.Misc.stringFormat;

import javax.inject.Inject;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

import com.google.common.base.Optional;

import wbs.console.helper.ConsoleHelper;
import wbs.console.helper.ConsoleObjectManager;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.record.Record;
import wbs.framework.utils.etc.BeanLogic;

@Accessors (fluent = true)
@PrototypeComponent ("simpleFormFieldAccessor")
public
class SimpleFormFieldAccessor<Container,Native>
	implements FormFieldAccessor<Container,Native> {

	// properties

	@Getter @Setter
	String name;

	@Getter @Setter
	Boolean dynamic;

	@Getter @Setter
	Class<? extends Native> nativeClass;

	@Inject
	ConsoleObjectManager consoleObjectManager;

	// implementation

	@Override
	public
	Optional<Native> read (
		Container container) {

		// get native object

		Object nativeObject;

		if (dynamic != null && dynamic) {

			ConsoleHelper<?> consoleHelper =
				consoleObjectManager.getConsoleObjectHelper(
					(Record<?>) container);

			nativeObject =
				consoleHelper.getDynamic (
					(Record<?>) container,
					name);

		} else {

			 nativeObject =
				BeanLogic.getProperty (
					container,
					name);

		}

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
					"Field %s is %s, not %s",
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
			@NonNull Optional<Native> nativeValue) {

		// sanity check native type

		if (

			nativeValue.isPresent ()

			&& ! nativeClass.isInstance (
				nativeValue.get ())

		) {

			throw new RuntimeException (
				stringFormat (
					"Field %s is %s, not %s",
					name,
					nativeClass.getSimpleName (),
					nativeValue.getClass ().getSimpleName ()));

		}

		// set property

		if (dynamic != null && dynamic) {

			ConsoleHelper<?> consoleHelper =
					consoleObjectManager.getConsoleObjectHelper(
						(Record<?>) container);

			consoleHelper.setDynamic (
				(Record<?>) container,
				name,
				nativeValue);

		}

		else {

			BeanLogic.setProperty (
				container,
				name,
				nativeValue);

		}

	}

}
