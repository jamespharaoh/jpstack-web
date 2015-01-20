package wbs.platform.api.resource;

import javax.inject.Inject;
import javax.inject.Provider;

import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.builder.Builder;
import wbs.framework.builder.annotations.BuildMethod;
import wbs.framework.builder.annotations.BuilderParent;
import wbs.framework.builder.annotations.BuilderSource;
import wbs.framework.builder.annotations.BuilderTarget;
import wbs.platform.api.module.ApiModuleBuilderHandler;
import wbs.platform.api.module.ApiModuleImplementation;
import wbs.platform.api.module.SimpleApiBuilderContainer;
import wbs.platform.api.module.SimpleApiBuilderContainerImplementation;

@PrototypeComponent ("apiResourceBuilder")
@ApiModuleBuilderHandler
public
class ApiResourceBuilder {

	// prototype dependencies

	@Inject
	Provider<SimpleApiBuilderContainerImplementation>
	simpleApiBuilderContainerImplementation;

	// builder

	@BuilderParent
	SimpleApiBuilderContainer container;

	@BuilderSource
	ApiResourceSpec spec;

	@BuilderTarget
	ApiModuleImplementation apiModule;

	// state

	SimpleApiBuilderContainer childContainer;

	// build

	@BuildMethod
	public
	void build (
			Builder builder) {

		setDefaults ();
		initContainers ();

		builder.descend (
			childContainer,
			spec.builders (),
			apiModule);

	}

	// implementation

	void setDefaults () {
	}

	void initContainers () {

		childContainer =
			simpleApiBuilderContainerImplementation.get ()

			.newBeanNamePrefix (
				"newBeanNamePrefix")

			.existingBeanNamePrefix (
				"existingBeanNamePrefix");


	}

}
