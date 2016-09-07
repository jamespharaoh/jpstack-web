package wbs.platform.event.fixture;

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
import wbs.platform.event.metamodel.EventTypesSpec;

@PrototypeComponent ("eventTypesBuilder")
@ModelMetaBuilderHandler
public
class EventTypesBuilder {

	// builder

	@BuilderParent
	ModelMetaSpec parent;

	@BuilderSource
	EventTypesSpec spec;

	@BuilderTarget
	Model model;

	// build

	@BuildMethod
	public
	void build (
			Builder builder) {

		builder.descend (
			parent,
			spec.eventTypes (),
			model,
			MissingBuilderBehaviour.error);

	}

}
