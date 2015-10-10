package wbs.platform.object.summary;

import javax.inject.Inject;
import javax.inject.Provider;

import wbs.console.annotations.ConsoleModuleBuilderHandler;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.builder.Builder;
import wbs.framework.builder.annotations.BuildMethod;
import wbs.framework.builder.annotations.BuilderParent;
import wbs.framework.builder.annotations.BuilderSource;
import wbs.framework.builder.annotations.BuilderTarget;

@PrototypeComponent ("objectSummaryBeanPartBuilder")
@ConsoleModuleBuilderHandler
public
class ObjectSummaryBeanPartBuilder {

	// prototype dependencies

	@Inject
	Provider<ObjectSummaryFieldsPart> summaryFieldsPart;

	// builder

	@BuilderParent
	ObjectSummaryPageSpec objectSummaryPageSpec;

	@BuilderSource
	ObjectSummaryBeanPartSpec objectSummaryBeanPartSpec;

	@BuilderTarget
	ObjectSummaryPageBuilder objectSummaryPageBuilder;

	// build

	@BuildMethod
	public
	void build (
			Builder builder) {

		objectSummaryPageBuilder.addPart (
			objectSummaryBeanPartSpec.beanName ());

	}

}
