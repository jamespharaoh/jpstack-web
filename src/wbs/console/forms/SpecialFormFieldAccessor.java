package wbs.console.forms;

import static wbs.framework.utils.etc.Misc.stringFormat;

import javax.inject.Inject;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
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
	Native read (
			Container container) {

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

		if (nativeObject == null)
			return null;

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

		return nativeClass.cast (
			nativeObject);

	}

	@Override
	public
	void write (
			Container container,
			Native nativeValue) {

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

			nativeValue != null

			&& ! nativeClass.isInstance (
				nativeValue)

		) {

			throw new RuntimeException (
				stringFormat (
					"Field %s is %s, not %s",
					name,
					nativeClass.getSimpleName (),
					nativeValue.getClass ().getSimpleName ()));

		}

		// set property

		BeanLogic.setProperty (
			container,
			name,
			nativeValue);

	}

}
