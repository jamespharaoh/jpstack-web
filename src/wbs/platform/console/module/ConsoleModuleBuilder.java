package wbs.platform.console.module;

import static wbs.framework.utils.etc.Misc.equal;
import static wbs.framework.utils.etc.Misc.stringFormat;

import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Provider;

import lombok.NonNull;
import wbs.framework.application.annotations.SingletonComponent;
import wbs.framework.builder.Builder;
import wbs.framework.builder.BuilderFactory;
import wbs.platform.console.annotations.ConsoleModuleBuilderHandler;
import wbs.platform.console.forms.FormField;
import wbs.platform.console.forms.FormFieldBuilderContext;
import wbs.platform.console.forms.FormFieldBuilderContextImpl;
import wbs.platform.console.forms.FormFieldSet;
import wbs.platform.console.forms.FormFieldSetSpec;
import wbs.platform.console.helper.ConsoleHelper;
import wbs.platform.console.helper.ConsoleHelperRegistry;
import wbs.platform.console.spec.ConsoleSpec;

@SingletonComponent ("consoleModuleBuilder")
public
class ConsoleModuleBuilder
	implements Builder {

	// dependencies

	@Inject
	ConsoleHelperRegistry consoleHelperRegistry;

	// prototype dependencies

	@Inject
	Provider<BuilderFactory> builderFactoryProvider;

	@Inject
	@ConsoleModuleBuilderHandler
	Map<Class<?>,Provider<Object>> consoleModuleBuilders;

	// state

	Builder builder;

	// init

	@PostConstruct
	public
	void init () {

		BuilderFactory builderFactory =
			builderFactoryProvider.get ();

		for (Map.Entry<Class<?>,Provider<Object>> entry
				: consoleModuleBuilders.entrySet ()) {

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
			new FormFieldBuilderContextImpl ()
				.containerClass (consoleHelper.objectClass ())
				.consoleHelper (consoleHelper);

		FormFieldSet formFieldSet =
			new FormFieldSet ();

		builder.descend (
			formFieldBuilderContext,
			formFieldSpecs,
			formFieldSet);

		for (FormField<?,?,?,?> formField
				: formFieldSet.formFields ()) {

			formField.init (
				fieldSetName);

		}

		return formFieldSet;

	}

	public
	FormFieldSet buildFormFieldSet (
			ConsoleSpec consoleSpec,
			String fieldSetName,
			Class<?> containerClass,
			@NonNull List<Object> formFieldSpecs) {

		FormFieldBuilderContext formFieldBuilderContext =
			new FormFieldBuilderContextImpl ()
				.containerClass (containerClass);

		FormFieldSet formFieldSet =
			new FormFieldSet ();

		builder.descend (
			formFieldBuilderContext,
			formFieldSpecs,
			formFieldSet);

		String fullFieldSetName =
			stringFormat (
				"%s.%s",
				consoleSpec.name (),
				fieldSetName);

		for (FormField<?,?,?,?> formField
				: formFieldSet.formFields ()) {

			formField.init (
				fullFieldSetName);

		}

		return formFieldSet;

	}

	public
	FormFieldSet buildFormFieldSet (
			@NonNull ConsoleSpec consoleSpec,
			@NonNull String fieldSetName) {

		String fullFieldSetName =
			stringFormat (
				"%s.%s",
				consoleSpec.name (),
				fieldSetName);

		for (Object builder
				: consoleSpec.builders ()) {

			if (! (builder instanceof FormFieldSetSpec))
				continue;

			FormFieldSetSpec formFieldSetSpec =
				(FormFieldSetSpec) builder;

			if (! equal (
					formFieldSetSpec.name (),
					fieldSetName))
				continue;

			ConsoleHelper<?> consoleHelper =
				consoleHelperRegistry.findByObjectName (
					formFieldSetSpec.objectName ());

			if (consoleHelper == null) {

				throw new RuntimeException (
					stringFormat (
						"No console helper for %s for fields %s of %s",
						formFieldSetSpec.objectName (),
						fieldSetName,
						consoleSpec.name ()));

			}

			return buildFormFieldSet (
				consoleHelper,
				fullFieldSetName,
				formFieldSetSpec.formFieldSpecs ());

		}

		throw new RuntimeException (
			stringFormat (
				"Form field set %s not found in %s",
				fieldSetName,
				consoleSpec.name ()));

	}

	public
	FormFieldSet buildFormFieldSet (
			@NonNull Class<?> containerClass,
			@NonNull ConsoleSpec consoleSpec,
			@NonNull String name) {

		for (Object builder
				: consoleSpec.builders ()) {

			if (! (builder instanceof FormFieldSetSpec))
				continue;

			FormFieldSetSpec formFieldSetSpec =
				(FormFieldSetSpec) builder;

			if (! equal (
					formFieldSetSpec.name (),
					name))
				continue;

			return buildFormFieldSet (
				consoleSpec,
				formFieldSetSpec.name (),
				containerClass,
				formFieldSetSpec.formFieldSpecs ());

		}

		throw new RuntimeException (
			stringFormat (
				"Form field set %s not found in %s",
				name,
				consoleSpec.name ()));

	}

	// builder

	@Override
	public
	void descend (
			Object parentObject,
			List<?> childObjects,
			Object targetObject) {

		builder.descend (
			parentObject,
			childObjects,
			targetObject);

	}

}
