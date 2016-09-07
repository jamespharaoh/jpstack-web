package wbs.platform.service.fixture;

import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.builder.Builder;
import wbs.framework.builder.Builder.MissingBuilderBehaviour;
import wbs.framework.builder.annotations.BuildMethod;
import wbs.framework.builder.annotations.BuilderParent;
import wbs.framework.builder.annotations.BuilderSource;
import wbs.framework.builder.annotations.BuilderTarget;
import wbs.framework.entity.fixtures.ModelMetaBuilderHandler;
import wbs.framework.entity.meta.model.ModelMetaSpec;
import wbs.framework.entity.model.Model;
import wbs.platform.service.metamodel.ServiceTypesSpec;

@PrototypeComponent ("serviceTypesBuilder")
@ModelMetaBuilderHandler
public
class ServiceTypesBuilder {

	// builder

	@BuilderParent
	ModelMetaSpec parent;

	@BuilderSource
	ServiceTypesSpec spec;

	@BuilderTarget
	Model model;

	// build

	@BuildMethod
	public
	void build (
			Builder builder) {

		builder.descend (
			parent,
			spec.serviceTypes (),
			model,
			MissingBuilderBehaviour.error);

	}

}
