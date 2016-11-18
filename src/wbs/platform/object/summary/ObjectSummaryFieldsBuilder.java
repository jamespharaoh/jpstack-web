package wbs.platform.object.summary;

import static wbs.utils.etc.TypeUtils.genericCastUnchecked;

import javax.inject.Provider;

import lombok.NonNull;

import wbs.console.annotations.ConsoleModuleBuilderHandler;
import wbs.console.module.ConsoleModuleBuilder;
import wbs.framework.builder.Builder;
import wbs.framework.builder.annotations.BuildMethod;
import wbs.framework.builder.annotations.BuilderParent;
import wbs.framework.builder.annotations.BuilderSource;
import wbs.framework.builder.annotations.BuilderTarget;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.PrototypeDependency;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.entity.record.Record;

@PrototypeComponent ("objectSummaryFieldsBuilder")
@ConsoleModuleBuilderHandler
public
class ObjectSummaryFieldsBuilder <
	ObjectType extends Record <ObjectType>,
	ParentType extends Record <ParentType>
> {

	// singleton dependencies

	@SingletonDependency
	ConsoleModuleBuilder consoleModuleBuilder;

	// prototype dependencies

	@PrototypeDependency
	Provider <ObjectSummaryFieldsPart <ObjectType, ParentType>>
	summaryFieldsPartProvider;

	// builder

	@BuilderParent
	ObjectSummaryPageSpec objectSummaryPageSpec;

	@BuilderSource
	ObjectSummaryFieldsSpec objectSummaryFieldsSpec;

	@BuilderTarget
	ObjectSummaryPageBuilder <ObjectType, ParentType> objectSummaryPageBuilder;

	// build

	@BuildMethod
	public
	void build (
			@NonNull Builder builder) {

		objectSummaryPageBuilder.addFieldsPart (
			genericCastUnchecked (
				objectSummaryPageBuilder.consoleModule.formFieldSetRequired (
					objectSummaryFieldsSpec.fieldsName ())));

	}

}
