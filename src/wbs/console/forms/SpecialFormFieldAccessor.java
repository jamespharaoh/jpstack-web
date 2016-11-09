package wbs.console.forms;

import static wbs.utils.string.StringUtils.stringFormat;

import com.google.common.base.Optional;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

import wbs.console.helper.core.ConsoleHelper;
import wbs.console.helper.manager.ConsoleObjectManager;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.entity.model.ModelField;
import wbs.framework.entity.record.Record;
import wbs.utils.etc.PropertyUtils;

@Accessors (fluent = true)
@PrototypeComponent ("specialFormFieldAccessor")
public
class SpecialFormFieldAccessor <Container extends Record <?>, Native>
	implements FormFieldAccessor <Container, Native> {

	// singleton dependencies

	@SingletonDependency
	ConsoleObjectManager objectManager;

	// properties

	@Getter @Setter
	String specialName;

	@Getter @Setter
	Class<? extends Native> nativeClass;

	// implementation

	@Override
	public
	Optional <Native> read (
			@NonNull Container container) {

		// get field name

		ConsoleHelper <?> consoleHelper =
			objectManager.findConsoleHelperRequired (
				container);

		ModelField modelField =
			(ModelField)
			PropertyUtils.get (
				consoleHelper,
				stringFormat (
					"%sField",
					specialName));

		String name =
			modelField.name ();

		// get native object

		Object nativeObject =
			PropertyUtils.getProperty (
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

		ConsoleHelper <?> consoleHelper =
			objectManager.findConsoleHelperRequired (
				container);

		ModelField modelField =
			(ModelField)
			PropertyUtils.get (
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

		PropertyUtils.setProperty (
			container,
			name,
			nativeValue.orNull ());

	}

}
