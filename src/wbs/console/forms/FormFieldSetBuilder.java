package wbs.console.forms;

import static wbs.utils.string.StringUtils.joinWithFullStop;
import static wbs.utils.string.StringUtils.stringFormat;

import wbs.console.annotations.ConsoleModuleBuilderHandler;
import wbs.console.helper.ConsoleHelper;
import wbs.console.helper.ConsoleHelperRegistry;
import wbs.console.helper.ConsoleObjectManager;
import wbs.console.module.ConsoleModuleImplementation;
import wbs.console.module.SimpleConsoleBuilderContainer;
import wbs.framework.builder.Builder;
import wbs.framework.builder.Builder.MissingBuilderBehaviour;
import wbs.framework.builder.annotations.BuildMethod;
import wbs.framework.builder.annotations.BuilderParent;
import wbs.framework.builder.annotations.BuilderSource;
import wbs.framework.builder.annotations.BuilderTarget;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;

@PrototypeComponent ("formFieldSetBuilder")
@ConsoleModuleBuilderHandler
public
class FormFieldSetBuilder <Container> {

	// singleton dependencies

	@SingletonDependency
	ConsoleHelperRegistry consoleHelperRegistry;

	@SingletonDependency
	ConsoleObjectManager objectManager;

	// builder

	@BuilderParent
	SimpleConsoleBuilderContainer container;

	@BuilderSource
	FormFieldSetSpec spec;

	@BuilderTarget
	ConsoleModuleImplementation consoleModule;

	// build

	@BuildMethod
	public
	void build (
			Builder builder) {

		if (
			spec.objectName () == null
			&& spec.className () == null
		) {

			throw new RuntimeException (
				stringFormat (
					"Form field set %s ",
					spec.name (),
					"in console module %s ",
					spec.consoleModule ().name (),
					"has neither object name nor class"));

		}

		if (
			spec.objectName () != null
			&& spec.className () != null
		) {

			throw new RuntimeException (
				stringFormat (
					"Form field set %s ",
					spec.name (),
					"in console module %s ",
					spec.consoleModule ().name (),
					"has both object name and class"));

		}

		ConsoleHelper<?> consoleHelper;
		Class<?> objectClass;

		if (spec.objectName () != null) {

			consoleHelper =
				consoleHelperRegistry.findByObjectName (
					spec.objectName ());

			objectClass =
				consoleHelper.objectClass ();

		} else {

			consoleHelper = null;

			try {

				objectClass =
					Class.forName (
						spec.className ());

			} catch (ClassNotFoundException exception) {

				throw new RuntimeException (
					stringFormat (
						"Error getting object class %s ",
						spec.className (),
						"for form field set %s ",
						spec.name (),
						"in console module %s",
						spec.consoleModule ().name ()));

			}

		}

		FormFieldBuilderContext formFieldBuilderContext =
			new FormFieldBuilderContextImplementation ()

			.containerClass (
				objectClass)

			.consoleHelper (
				consoleHelper);

		FormFieldSet <Container> formFieldSet =
			new FormFieldSet <Container> ()

			.name (
				joinWithFullStop (
					spec.consoleModule ().name (),
					spec.name ()));

		builder.descend (
			formFieldBuilderContext,
			spec.formFieldSpecs (),
			formFieldSet,
			MissingBuilderBehaviour.error);

		String fullName =
			joinWithFullStop (
				spec.consoleModule ().name (),
				spec.name ());

		for (
			FormItem <?> formItem
				: formFieldSet.formItems ()
		) {

			formItem.init (
				fullName);

		}

		for (
			FormField <?, ?, ?, ?> formField
				: formFieldSet.formFields ()
		) {

			if (formField.fileUpload ()) {
				formFieldSet.fileUpload (true);
			}

		}

		if (formFieldSet.fileUpload () == null)
			formFieldSet.fileUpload (false);

		consoleModule.addFormFieldSet (
			spec.name (),
			formFieldSet);

	}

}
