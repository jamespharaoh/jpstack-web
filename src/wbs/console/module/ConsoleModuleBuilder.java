package wbs.console.module;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.inject.Provider;

import lombok.NonNull;

import wbs.console.annotations.ConsoleModuleBuilderHandler;
import wbs.console.forms.FormField;
import wbs.console.forms.FormFieldBuilderContext;
import wbs.console.forms.FormFieldBuilderContextImplementation;
import wbs.console.forms.FormFieldSet;
import wbs.console.forms.FormFieldSetSpec;
import wbs.console.helper.ConsoleHelper;
import wbs.console.helper.ConsoleHelperRegistry;
import wbs.framework.application.annotations.PrototypeDependency;
import wbs.framework.application.annotations.SingletonComponent;
import wbs.framework.application.annotations.SingletonDependency;
import wbs.framework.builder.Builder;
import wbs.framework.builder.BuilderFactory;

@SingletonComponent ("consoleModuleBuilder")
public
class ConsoleModuleBuilder
	implements Builder {

	// singleton dependencies

	@SingletonDependency
	ConsoleHelperRegistry consoleHelperRegistry;

	// prototype dependencies

	@PrototypeDependency
	Provider <BuilderFactory> builderFactoryProvider;

	@PrototypeDependency
	@ConsoleModuleBuilderHandler
	Map <Class <?>, Provider <Object>> consoleModuleBuilders;

	// state

	Builder builder;

	// init

	@PostConstruct
	public
	void init () {

		BuilderFactory builderFactory =
			builderFactoryProvider.get ();

		for (
			Map.Entry<Class<?>,Provider<Object>> entry
				: consoleModuleBuilders.entrySet ()
		) {

			builderFactory.addBuilder (
				entry.getKey (),
				entry.getValue ());

		}

		builder =
			builderFactory.create ();

	}

	// implementation

	public
	FormFieldSet buildFormFieldSet (
			@NonNull ConsoleHelper<?> consoleHelper,
			@NonNull String fieldSetName,
			@NonNull List<Object> formFieldSpecs) {

		FormFieldBuilderContext formFieldBuilderContext =
			new FormFieldBuilderContextImplementation ()

			.containerClass (
				consoleHelper.objectClass ())

			.consoleHelper (
				consoleHelper);

		FormFieldSet formFieldSet =
			new FormFieldSet ();

		builder.descend (
			formFieldBuilderContext,
			formFieldSpecs,
			formFieldSet,
			MissingBuilderBehaviour.error);

		for (
			FormField<?,?,?,?> formField
				: formFieldSet.formFields ()
		) {

			formField.init (
				fieldSetName);

			if (formField.fileUpload ())
				formFieldSet.fileUpload (true);

		}

		if (formFieldSet.fileUpload () == null)
			formFieldSet.fileUpload (false);

		return formFieldSet;

	}

	// builder

	@Override
	public
	void descend (
			@NonNull Object parentObject,
			@NonNull List<?> childObjects,
			@NonNull Object targetObject,
			@NonNull MissingBuilderBehaviour missingBuilderBehaviour) {

		List<Object> firstPass =
			new ArrayList<Object> ();

		List<Object> secondPass =
			new ArrayList<Object> ();

		for (
			Object childObject
				: childObjects
		) {

			if (childObject instanceof FormFieldSetSpec) {

				firstPass.add (
					childObject);

			} else {

				secondPass.add (
					childObject);

			}

		}

		builder.descend (
			parentObject,
			firstPass,
			targetObject,
			missingBuilderBehaviour);

		builder.descend (
			parentObject,
			secondPass,
			targetObject,
			missingBuilderBehaviour);

	}

}
