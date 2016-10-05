package wbs.console.object;

import lombok.NonNull;

import wbs.console.annotations.ConsoleMetaModuleBuilderHandler;
import wbs.console.context.ConsoleContextMetaBuilderContainer;
import wbs.console.module.ConsoleMetaModuleImplementation;
import wbs.framework.builder.Builder;
import wbs.framework.builder.Builder.MissingBuilderBehaviour;
import wbs.framework.builder.annotations.BuildMethod;
import wbs.framework.builder.annotations.BuilderParent;
import wbs.framework.builder.annotations.BuilderSource;
import wbs.framework.builder.annotations.BuilderTarget;
import wbs.framework.component.annotations.PrototypeComponent;

@PrototypeComponent ("extendContextMetaBuilder")
@ConsoleMetaModuleBuilderHandler
public
class ExtendContextMetaBuilder {

	// builder

	@BuilderParent
	ConsoleContextMetaBuilderContainer container;

	@BuilderSource
	ExtendContextSpec spec;

	@BuilderTarget
	ConsoleMetaModuleImplementation metaModule;

	// state

	String name;
	String baseName;
	String extensionPointName;

	// build

	@BuildMethod
	public
	void build (
			@NonNull Builder builder) {

		setDefaults ();

		ConsoleContextMetaBuilderContainer nextContainer =
			new ConsoleContextMetaBuilderContainer ()

			.structuralName (
				baseName)

			.extensionPointName (
				extensionPointName);

		builder.descend (
			nextContainer,
			spec.children (),
			metaModule,
			MissingBuilderBehaviour.ignore);

	}

	// defaults

	void setDefaults () {

		name =
			spec.name ();

		baseName =
			spec.baseName ();

		extensionPointName =
			spec.extensionPointName ();

	}

}
