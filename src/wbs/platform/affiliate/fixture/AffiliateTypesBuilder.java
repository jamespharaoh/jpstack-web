package wbs.platform.affiliate.fixture;

import lombok.NonNull;

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
import wbs.platform.affiliate.metamodel.AffiliateTypesSpec;

@PrototypeComponent ("affiliateTypesBuilder")
@ModelMetaBuilderHandler
public
class AffiliateTypesBuilder {

	// builder

	@BuilderParent
	ModelMetaSpec parent;

	@BuilderSource
	AffiliateTypesSpec spec;

	@BuilderTarget
	Model <?> model;

	// build

	@BuildMethod
	public
	void build (
			@NonNull Builder builder) {

		builder.descend (
			parent,
			spec.affiliateTypes (),
			model,
			MissingBuilderBehaviour.error);

	}

}
