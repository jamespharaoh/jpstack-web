package wbs.console.forms;

import java.util.Collections;

import javax.inject.Provider;

import lombok.NonNull;

import wbs.console.annotations.ConsoleModuleBuilderHandler;
import wbs.console.html.ScriptRef;
import wbs.console.misc.JqueryScriptRef;

import wbs.framework.builder.Builder;
import wbs.framework.builder.BuilderComponent;
import wbs.framework.builder.annotations.BuildMethod;
import wbs.framework.builder.annotations.BuilderParent;
import wbs.framework.builder.annotations.BuilderSource;
import wbs.framework.builder.annotations.BuilderTarget;
import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.PrototypeDependency;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.OwnedTaskLogger;
import wbs.framework.logging.TaskLogger;

@PrototypeComponent ("jqueryScriptRefFormFieldBuilder")
@ConsoleModuleBuilderHandler
public
class JqueryScriptRefFormFieldBuilder <Container>
	implements BuilderComponent {

	// singleton depdendencies

	@ClassSingletonDependency
	LogContext logContext;

	// prototype dependencies

	@PrototypeDependency
	Provider <ScriptRefFormField <Container>> scriptRefFormFieldProvider;

	// builder

	@BuilderParent
	FormFieldBuilderContext context;

	@BuilderSource
	JqueryScriptRefFormFieldSpec spec;

	@BuilderTarget
	FormFieldSet <Container> formFieldSet;

	// build

	@Override
	@BuildMethod
	public
	void build (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull Builder <TaskLogger> builder) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"build");

		) {

			formFieldSet.addFormItem (
				scriptRefFormFieldProvider.get ()

				.scriptRefs (
					Collections.<ScriptRef> singleton (
						JqueryScriptRef.instance))

			);

		}

	}

}
