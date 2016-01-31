package wbs.console.forms;

import static wbs.framework.utils.etc.Misc.joinWithSeparator;
import static wbs.framework.utils.etc.Misc.stringFormat;

import javax.inject.Inject;

import wbs.console.annotations.ConsoleModuleBuilderHandler;
import wbs.console.helper.ConsoleHelper;
import wbs.console.helper.ConsoleHelperRegistry;
import wbs.console.helper.ConsoleObjectManager;
import wbs.console.module.ConsoleModuleImplementation;
import wbs.console.module.SimpleConsoleBuilderContainer;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.builder.Builder;
import wbs.framework.builder.Builder.MissingBuilderBehaviour;
import wbs.framework.builder.annotations.BuildMethod;
import wbs.framework.builder.annotations.BuilderParent;
import wbs.framework.builder.annotations.BuilderSource;
import wbs.framework.builder.annotations.BuilderTarget;

@PrototypeComponent ("formFieldSetBuilder")
@ConsoleModuleBuilderHandler
public
class FormFieldSetBuilder {

	// dependencies

	@Inject
	ConsoleHelperRegistry consoleHelperRegistry;

	@Inject
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

		FormFieldSet formFieldSet =
			new FormFieldSet ()

			.name (
				joinWithSeparator (
					".",
					spec.consoleModule ().name (),
					spec.name ()));

		builder.descend (
			formFieldBuilderContext,
			spec.formFieldSpecs (),
			formFieldSet,
			MissingBuilderBehaviour.error);

		String fullName =
			joinWithSeparator (
				".",
				spec.consoleModule ().name (),
				spec.name ());

		for (
			FormField<?,?,?,?> formField
				: formFieldSet.formFields ()
		) {

			formField.init (
				fullName);

			if (formField.fileUpload ())
				formFieldSet.fileUpload (true);

		}

		if (formFieldSet.fileUpload () == null)
			formFieldSet.fileUpload (false);

		consoleModule.addFormFieldSet (
			spec.name (),
			formFieldSet);

	}

}
