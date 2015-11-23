package wbs.platform.object.summary;

import javax.inject.Inject;
import javax.inject.Provider;

import lombok.NonNull;

import wbs.console.annotations.ConsoleModuleBuilderHandler;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.builder.Builder;
import wbs.framework.builder.annotations.BuildMethod;
import wbs.framework.builder.annotations.BuilderParent;
import wbs.framework.builder.annotations.BuilderSource;
import wbs.framework.builder.annotations.BuilderTarget;
import wbs.framework.record.Record;

@PrototypeComponent ("objectSummaryBeanPartBuilder")
@ConsoleModuleBuilderHandler
public
class ObjectSummaryBeanPartBuilder<
	ObjectType extends Record<ObjectType>,
	ParentType extends Record<ParentType>
> {

	// prototype dependencies

	@Inject
	Provider<ObjectSummaryFieldsPart<ObjectType,ParentType>> summaryFieldsPart;

	// builder

	@BuilderParent
	ObjectSummaryPageSpec objectSummaryPageSpec;

	@BuilderSource
	ObjectSummaryBeanPartSpec objectSummaryBeanPartSpec;

	@BuilderTarget
	ObjectSummaryPageBuilder<ObjectType,ParentType> objectSummaryPageBuilder;

	// build

	@BuildMethod
	public
	void build (
			@NonNull Builder builder) {

		objectSummaryPageBuilder.addPart (
			objectSummaryBeanPartSpec.beanName ());

	}

}
