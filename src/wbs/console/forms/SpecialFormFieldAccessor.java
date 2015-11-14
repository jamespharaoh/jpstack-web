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
import wbs.framework.entity.model.ModelField;
import wbs.framework.record.Record;
import wbs.framework.utils.etc.BeanLogic;

@Accessors (fluent = true)
@PrototypeComponent ("specialFormFieldAccessor")
public
class SpecialFormFieldAccessor<Container extends Record<?>,Native>
	implements FormFieldAccessor<Container,Native> {

	// dependencies

	@Inject
	ConsoleObjectManager objectManager;

	// properties

	@Getter @Setter
	String specialName;

	@Getter @Setter
	Class<? extends Native> nativeClass;

	// implementation

	@Override
	public
	Optional<Native> read (
			@NonNull Container container) {

		// get field name

		ConsoleHelper<?> consoleHelper =
			objectManager.getConsoleObjectHelper (
				container);

		ModelField modelField =
			(ModelField)
			BeanLogic.get (
				consoleHelper,
				stringFormat (
					"%sField",
					specialName));

		String name =
			modelField.name ();

		// get native object

		Object nativeObject =
			BeanLogic.getProperty (
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

		// get field name

		ConsoleHelper<?> consoleHelper =
			objectManager.getConsoleObjectHelper (
				container);

		ModelField modelField =
			(ModelField)
			BeanLogic.get (
				consoleHelper,
				stringFormat (
					"%sField",
					specialName));

		String name =
			modelField.name ();

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
					nativeValue.get ().getClass ().getSimpleName ()));

		}

		// set property

		BeanLogic.setProperty (
			container,
			name,
			nativeValue.orNull ());

	}

}
