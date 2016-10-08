package wbs.console.forms;

import javax.inject.Provider;

import com.google.common.collect.ImmutableSet;

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
class ScriptRefFormFieldBuilder <Container> {

	// prototype dependencies

	@PrototypeDependency
	Provider <ScriptRefFormField <Container>>
	scriptRefFormFieldProvider;

	// builder

	@BuilderParent
	FormFieldBuilderContext context;

	@BuilderSource
	ScriptRefFormFieldSpec spec;

	@BuilderTarget
	FormFieldSet <Container> formFieldSet;

	// state

	// build

	@BuildMethod
	public
	void build (
			Builder builder) {

		formFieldSet.addFormItem (
			scriptRefFormFieldProvider.get ()

			.scriptRefs (
				ImmutableSet.<ScriptRef> of (
					ConsoleApplicationScriptRef.javascript (
						spec.path ())))

		);

	}

}
