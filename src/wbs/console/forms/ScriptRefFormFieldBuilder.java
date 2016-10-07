package wbs.console.forms;

import java.util.Collections;

import javax.inject.Provider;

import wbs.console.annotations.ConsoleModuleBuilderHandler;
import wbs.console.context.ConsoleApplicationScriptRef;
import wbs.console.html.ScriptRef;
import wbs.framework.builder.Builder;
import wbs.framework.builder.annotations.BuildMethod;
import wbs.framework.builder.annotations.BuilderParent;
import wbs.framework.builder.annotations.BuilderSource;
import wbs.framework.builder.annotations.BuilderTarget;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.PrototypeDependency;

@PrototypeComponent ("scriptRefFormFieldBuilder")
@ConsoleModuleBuilderHandler
public
class ScriptRefFormFieldBuilder {

	// prototype dependencies

	@PrototypeDependency
	Provider <ScriptRefFormField>
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

		formFieldSet.addFormItem (
			scriptRefFormFieldProvider.get ()

			.scriptRefs (
				Collections.<ScriptRef>singleton (
					ConsoleApplicationScriptRef.javascript (
						spec.path ())))

		);

	}

}
