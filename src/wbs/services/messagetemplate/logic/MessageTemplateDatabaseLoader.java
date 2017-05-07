package wbs.services.messagetemplate.logic;

import static wbs.utils.etc.TypeUtils.genericCastUnchecked;

import javax.inject.Provider;

import lombok.NonNull;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.NormalLifecycleSetup;
import wbs.framework.component.annotations.PrototypeDependency;
import wbs.framework.component.annotations.SingletonComponent;
import wbs.framework.data.tools.DataFromXml;
import wbs.framework.data.tools.DataFromXmlBuilder;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.OwnedTaskLogger;
import wbs.framework.logging.TaskLogger;

import wbs.services.messagetemplate.fixture.MessageTemplateDatabaseSpec;
import wbs.services.messagetemplate.fixture.MessageTemplateEntryTypeSpec;
import wbs.services.messagetemplate.fixture.MessageTemplateFieldTypeSpec;
import wbs.services.messagetemplate.fixture.MessageTemplateParameterSpec;

@SingletonComponent ("messageTemplateDatabaseLoader")
public
class MessageTemplateDatabaseLoader {

	// singleton dependencies

	@ClassSingletonDependency
	LogContext logContext;

	// prototpe depenencies

	@PrototypeDependency
	Provider <DataFromXmlBuilder> dataFromXmlBuilderProvider;

	// state

	DataFromXml dataFromXml;

	// life cycle

	@NormalLifecycleSetup
	public
	void setup (
			@NonNull TaskLogger parentTaskLogger) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"setup");

		) {

			dataFromXml =
				dataFromXmlBuilderProvider.get ()

				.registerBuilderClasses (
					MessageTemplateDatabaseSpec.class,
					MessageTemplateEntryTypeSpec.class,
					MessageTemplateFieldTypeSpec.class,
					MessageTemplateParameterSpec.class)

				.build ();

		}

	}

	// public implementation

	public
	MessageTemplateDatabaseSpec loadFromClasspath (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull String resourceName) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"enclosing_method");

		) {

			return genericCastUnchecked (
				dataFromXml.readClasspath (
					taskLogger,
					resourceName));

		}

	}

}
