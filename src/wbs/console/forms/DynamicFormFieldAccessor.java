package wbs.console.forms;

import static wbs.utils.etc.TypeUtils.objectClassNameSimple;
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
import wbs.framework.entity.record.Record;

@Accessors (fluent = true)
@PrototypeComponent ("dynamicFormFieldAccessor")
public
class DynamicFormFieldAccessor <
	Container extends Record <Container>,
	Native
>
	implements FormFieldAccessor <Container, Native> {

	// singleton dependencies

	@SingletonDependency
	ConsoleObjectManager consoleObjectManager;

	// properties

	@Getter @Setter
	String name;

	@Getter @Setter
	Class <? extends Native> nativeClass;

	// implementation

	@Override
	public
	Optional <Native> read (
			@NonNull Container container) {

		// get native object

		ConsoleHelper <?> consoleHelper =
			consoleObjectManager.findConsoleHelper (
				(Container)
				container);

		Object nativeObject =
			consoleHelper.getDynamicGeneric (
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
			@NonNull Optional<Native> nativeValueOptional) {

		// sanity check native type

		if (

			nativeValueOptional.isPresent ()

			&& ! nativeClass.isInstance (
				nativeValueOptional.get ())

		) {

			throw new RuntimeException (
				stringFormat (
					"Field %s is %s, not %s",
					name,
					nativeClass.getSimpleName (),
					objectClassNameSimple (
						nativeValueOptional.get ())));

		}

		// set property

		ConsoleHelper <?> consoleHelper =
			consoleObjectManager.findConsoleHelper(
				container);

		consoleHelper.setDynamicGeneric (
			container,
			name,
			nativeValueOptional);

	}

}
