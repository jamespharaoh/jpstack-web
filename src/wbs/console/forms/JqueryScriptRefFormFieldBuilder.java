package wbs.console.forms;

import java.util.Collections;

import javax.inject.Provider;

import wbs.console.annotations.ConsoleModuleBuilderHandler;
import wbs.console.html.ScriptRef;
import wbs.console.misc.JqueryScriptRef;
import wbs.framework.builder.Builder;
import wbs.framework.builder.annotations.BuildMethod;
import wbs.framework.builder.annotations.BuilderParent;
import wbs.framework.builder.annotations.BuilderSource;
import wbs.framework.builder.annotations.BuilderTarget;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.PrototypeDependency;

@PrototypeComponent ("jqueryScriptRefFormFieldBuilder")
@ConsoleModuleBuilderHandler
public
class JqueryScriptRefFormFieldBuilder {

	// prototype dependencies

	@PrototypeDependency
	Provider <ScriptRefFormField>
	scriptRefFormFieldProvider;

	// builder

	@BuilderParent
	FormFieldBuilderContext context;

	@BuilderSource
	JqueryScriptRefFormFieldSpec spec;

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
					JqueryScriptRef.instance))

		);

	}

}
