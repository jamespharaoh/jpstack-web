package wbs.console.forms;

import java.util.Collections;

import javax.inject.Inject;
import javax.inject.Provider;

import wbs.console.annotations.ConsoleModuleBuilderHandler;
import wbs.console.context.ConsoleApplicationScriptRef;
import wbs.console.html.ScriptRef;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.builder.Builder;
import wbs.framework.builder.annotations.BuildMethod;
import wbs.framework.builder.annotations.BuilderParent;
import wbs.framework.builder.annotations.BuilderSource;
import wbs.framework.builder.annotations.BuilderTarget;

@PrototypeComponent ("scriptRefFormFieldBuilder")
@ConsoleModuleBuilderHandler
public
class ScriptRefFormFieldBuilder {

	// prototype dependencies

	@Inject
	Provider<ScriptRefFormField>
	scriptRefFormFieldProvider;

	// builder

	@BuilderParent
	FormFieldBuilderContext context;

	@BuilderSource
	ScriptRefFormFieldSpec spec;

	@BuilderTarget
	FormFieldSet formFieldSet;

	// state

	// build

	@BuildMethod
	public
	void build (
			Builder builder) {

		formFieldSet.addFormField (
			scriptRefFormFieldProvider.get ()

			.scriptRefs (
				Collections.<ScriptRef>singleton (
					ConsoleApplicationScriptRef.javascript (
						spec.path ())))

		);

	}

}
