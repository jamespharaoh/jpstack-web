package wbs.console.forms.scriptref;

import java.util.Collections;

import lombok.NonNull;

import wbs.console.forms.core.ConsoleFormBuilderComponent;
import wbs.console.forms.core.ConsoleFormBuilderContext;
import wbs.console.forms.core.FormFieldSetImplementation;
import wbs.console.html.ScriptRef;
import wbs.console.misc.JqueryScriptRef;

import wbs.framework.builder.Builder;
import wbs.framework.builder.annotations.BuildMethod;
import wbs.framework.builder.annotations.BuilderParent;
import wbs.framework.builder.annotations.BuilderSource;
import wbs.framework.builder.annotations.BuilderTarget;
import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.PrototypeDependency;
import wbs.framework.component.manager.ComponentProvider;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.OwnedTaskLogger;
import wbs.framework.logging.TaskLogger;

@PrototypeComponent ("jqueryScriptRefFormFieldBuilder")
public
class JqueryScriptRefFormFieldBuilder <Container>
	implements ConsoleFormBuilderComponent {

	// singleton depdendencies

	@ClassSingletonDependency
	LogContext logContext;

	// prototype dependencies

	@PrototypeDependency
	ComponentProvider <ScriptRefFormField <Container>>
		scriptRefFormFieldProvider;

	// builder

	@BuilderParent
	ConsoleFormBuilderContext context;

	@BuilderSource
	JqueryScriptRefFormFieldSpec spec;

	@BuilderTarget
	FormFieldSetImplementation <Container> formFieldSet;

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
				scriptRefFormFieldProvider.provide (
					taskLogger)

				.scriptRefs (
					Collections.<ScriptRef> singleton (
						JqueryScriptRef.instance))

			);

		}

	}

}
