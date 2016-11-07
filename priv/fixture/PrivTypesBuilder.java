package wbs.platform.priv.fixture;

import wbs.framework.builder.Builder;
import wbs.framework.builder.Builder.MissingBuilderBehaviour;
import wbs.framework.builder.annotations.BuildMethod;
import wbs.framework.builder.annotations.BuilderParent;
import wbs.framework.builder.annotations.BuilderSource;
import wbs.framework.builder.annotations.BuilderTarget;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.entity.fixtures.ModelMetaBuilderHandler;
import wbs.framework.entity.meta.model.ModelMetaSpec;
import wbs.framework.entity.model.Model;
import wbs.platform.priv.metamodel.PrivTypesSpec;

@PrototypeComponent ("privTypesBuilder")
@ModelMetaBuilderHandler
public
class PrivTypesBuilder {

	// builder

	@BuilderParent
	ModelMetaSpec parent;

	@BuilderSource
	PrivTypesSpec spec;

	@BuilderTarget
	Model <?> model;

	// build

	@BuildMethod
	public
	void build (
			Builder builder) {

		builder.descend (
			parent,
			spec.privTypes (),
			model,
			MissingBuilderBehaviour.error);

	}

}
