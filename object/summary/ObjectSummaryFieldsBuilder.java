package wbs.platform.object.summary;

import javax.inject.Inject;
import javax.inject.Provider;

import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.builder.Builder;
import wbs.framework.builder.annotations.BuildMethod;
import wbs.framework.builder.annotations.BuilderParent;
import wbs.framework.builder.annotations.BuilderSource;
import wbs.framework.builder.annotations.BuilderTarget;
import wbs.platform.console.annotations.ConsoleModuleBuilderHandler;
import wbs.platform.console.module.ConsoleModuleBuilder;

@PrototypeComponent ("objectSummaryFieldsBuilder")
@ConsoleModuleBuilderHandler
public
class ObjectSummaryFieldsBuilder {

	// dependencies

	@Inject
	ConsoleModuleBuilder consoleModuleBuilder;

	// prototype dependencies

	@Inject
	Provider<ObjectSummaryFieldsPart> summaryFieldsPart;

	// builder

	@BuilderParent
	ObjectSummaryPageSpec objectSummaryPageSpec;

	@BuilderSource
	ObjectSummaryFieldsSpec objectSummaryFieldsSpec;

	@BuilderTarget
	ObjectSummaryPageBuilder objectSummaryPageBuilder;

	// build

	@BuildMethod
	public
	void build (
			Builder builder) {

		objectSummaryPageBuilder.addFieldsPart (
			objectSummaryPageBuilder.consoleModule.formFieldSets ().get (
				objectSummaryFieldsSpec.fieldsName ()));

	}

}
