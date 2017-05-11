package wbs.framework.object;

import static wbs.utils.etc.TypeUtils.classForNameRequired;
import static wbs.utils.etc.TypeUtils.classNameFull;
import static wbs.utils.string.StringUtils.capitalise;
import static wbs.utils.string.StringUtils.stringFormat;

import lombok.NonNull;

import wbs.framework.component.registry.ComponentDefinition;
import wbs.framework.component.registry.ComponentRegistryBuilder;
import wbs.framework.component.scaffold.PluginModelSpec;
import wbs.framework.component.scaffold.PluginSpec;
import wbs.framework.component.tools.ComponentPlugin;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.LoggingLogic;
import wbs.framework.logging.OwnedTaskLogger;
import wbs.framework.logging.TaskLogger;

public
class ObjectComponentPlugin
	implements ComponentPlugin {

	// state

	LogContext logContext;

	// constructors

	public
	ObjectComponentPlugin (
			@NonNull LoggingLogic loggingLogic) {

		logContext =
			loggingLogic.findOrCreateLogContext (
				classNameFull (
					getClass ()));

	}

	// public implementation

	@Override
	public
	void registerComponents (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull ComponentRegistryBuilder componentRegistry,
			@NonNull PluginSpec plugin) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"registerComponents");

		) {

			plugin.models ().models ().forEach (
				projectModelSpec -> {

				registerObjectHooks (
					taskLogger,
					componentRegistry,
					projectModelSpec);

				registerObjectHelper (
					taskLogger,
					componentRegistry,
					projectModelSpec);

				registerObjectHelperMethodsImplementation (
					taskLogger,
					componentRegistry,
					projectModelSpec);

			});

		}

	}

	private
	void registerObjectHooks (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull ComponentRegistryBuilder componentRegistry,
			@NonNull PluginModelSpec model) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"registerObjectHooks");

		) {

			String objectHooksComponentName =
				stringFormat (
					"%sHooks",
					model.name ());

			String objectHooksClassName =
				stringFormat (
					"%s.logic.%sHooks",
					model.plugin ().packageName (),
					capitalise (model.name ()));

			Class <?> objectHooksClass;

			try {

				objectHooksClass =
					Class.forName (
						objectHooksClassName);

				componentRegistry.registerDefinition (
					taskLogger,
					new ComponentDefinition ()

					.name (
						objectHooksComponentName)

					.componentClass (
						objectHooksClass)

					.scope (
						"singleton")

				);

			} catch (ClassNotFoundException exception) {

				componentRegistry.registerDefinition (
					taskLogger,
					new ComponentDefinition ()

					.name (
						objectHooksComponentName)

					.componentClass (
						ObjectHooks.DefaultImplementation.class)

					.scope (
						"singleton")

				);

			}

		}

	}

	private
	void registerObjectHelper (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull ComponentRegistryBuilder componentRegistry,
			@NonNull PluginModelSpec model) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"registerObjectHelper");

		) {

			String objectHelperComponentName =
				stringFormat (
					"%sObjectHelper",
					model.name ());

			String objectHelperImplementationClassName =
				stringFormat (
					"%s.logic.%sObjectHelperImplementation",
					model.plugin ().packageName (),
					capitalise (
						model.name ()));

			Class <?> objectHelperImplementationClass =
				classForNameRequired (
					objectHelperImplementationClassName);

			componentRegistry.registerDefinition (
				taskLogger,
				new ComponentDefinition ()

				.name (
					objectHelperComponentName)

				.componentClass (
					objectHelperImplementationClass)

				.scope (
					"singleton")

			);

		}

	}

	private
	void registerObjectHelperMethodsImplementation (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull ComponentRegistryBuilder componentRegistry,
			@NonNull PluginModelSpec model) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"registerObjectHelperMethodsImplementation");

		) {

			String objectHelperMethodsImplementationComponentName =
				stringFormat (
					"%sObjectHelperMethodsImplementation",
					model.name ());

			String objectHelperMethodsImplementationClassName =
				stringFormat (
					"%s.logic.%sObjectHelperMethodsImplementation",
					model.plugin ().packageName (),
					capitalise (
						model.name ()));

			Class <?> objectHelperMethodsImplementationClass;

			try {

				objectHelperMethodsImplementationClass =
					Class.forName (
						objectHelperMethodsImplementationClassName);

			} catch (ClassNotFoundException exception) {

				/*
				log.warn (sf (
					"No object helper implementation for %s.%s.%s",
					model.project ().packageName (),
					model.plugin ().packageName (),
					model.name ()));
				*/

				return;

			}

			componentRegistry.registerDefinition (
				taskLogger,
				new ComponentDefinition ()

				.name (
					objectHelperMethodsImplementationComponentName)

				.componentClass (
					objectHelperMethodsImplementationClass)

				.scope (
					"singleton")

			);

			return;

		}

	}

}
