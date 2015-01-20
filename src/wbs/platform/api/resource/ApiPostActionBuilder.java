package wbs.platform.api.resource;

import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.builder.Builder;
import wbs.framework.builder.annotations.BuildMethod;
import wbs.framework.builder.annotations.BuilderParent;
import wbs.framework.builder.annotations.BuilderSource;
import wbs.framework.builder.annotations.BuilderTarget;
import wbs.platform.api.module.ApiModuleBuilderHandler;
import wbs.platform.api.module.ApiModuleImplementation;
import wbs.platform.api.module.SimpleApiBuilderContainer;

@PrototypeComponent ("apiPostActionBuilder")
@ApiModuleBuilderHandler
public
class ApiPostActionBuilder {

	// builder

	@BuilderParent
	SimpleApiBuilderContainer container;

	@BuilderSource
	ApiPostActionSpec spec;

	@BuilderTarget
	ApiModuleImplementation apiModule;

	// build

	@BuildMethod
	public
	void build (
			Builder builder) {

		setDefaults ();

	}

	// implementation

	void setDefaults () {
	}

}
