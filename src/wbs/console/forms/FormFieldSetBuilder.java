package wbs.console.forms;

import static wbs.utils.etc.TypeUtils.classForNameOrThrow;
import static wbs.utils.string.StringUtils.joinWithFullStop;
import static wbs.utils.string.StringUtils.stringFormat;

import wbs.console.annotations.ConsoleModuleBuilderHandler;
import wbs.console.helper.core.ConsoleHelper;
import wbs.console.helper.manager.ConsoleObjectManager;
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

		ConsoleHelper <?> consoleHelper;
		Class <Container> containerClass;

		if (spec.objectName () != null) {

			consoleHelper =
				objectManager.findConsoleHelper (
					spec.objectName ());

			@SuppressWarnings ("unchecked")
			Class <Container> containerClassTemp =
				(Class <Container>)
				consoleHelper.objectClass ();

			containerClass =
				containerClassTemp;

		} else {

			consoleHelper = null;

			@SuppressWarnings ("unchecked")
			Class <Container> containerClassTemp =
				(Class <Container>)
				classForNameOrThrow (
					spec.className (),
					() -> new RuntimeException (
						stringFormat (
							"Error getting object class %s ",
							spec.className (),
							"for form field set %s ",
							spec.name (),
							"in console module %s",
							spec.consoleModule ().name ())));

			containerClass =
				containerClassTemp;

		}

		FormFieldBuilderContext formFieldBuilderContext =
			new FormFieldBuilderContextImplementation ()

			.containerClass (
				containerClass)

			.consoleHelper (
				consoleHelper);

		FormFieldSet <Container> formFieldSet =
			new FormFieldSet <Container> ()

			.name (
				joinWithFullStop (
					spec.consoleModule ().name (),
					spec.name ()))

			.containerClass (
				containerClass);

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
