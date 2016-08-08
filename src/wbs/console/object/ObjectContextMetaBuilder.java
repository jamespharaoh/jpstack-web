package wbs.console.object;

import static wbs.framework.utils.etc.Misc.ifNull;
import static wbs.framework.utils.etc.StringUtils.stringFormat;
import static wbs.framework.utils.etc.StringUtils.naivePluralise;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Provider;

import com.google.common.collect.ImmutableList;

import wbs.console.annotations.ConsoleMetaModuleBuilderHandler;
import wbs.console.context.ConsoleContextHint;
import wbs.console.context.ConsoleContextMetaBuilderContainer;
import wbs.console.context.ConsoleContextRootExtensionPoint;
import wbs.console.module.ConsoleMetaModuleImplementation;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.builder.Builder;
import wbs.framework.builder.Builder.MissingBuilderBehaviour;
import wbs.framework.builder.annotations.BuildMethod;
import wbs.framework.builder.annotations.BuilderParent;
import wbs.framework.builder.annotations.BuilderSource;
import wbs.framework.builder.annotations.BuilderTarget;

@PrototypeComponent ("objectContextMetaBuilder")
@ConsoleMetaModuleBuilderHandler
public
class ObjectContextMetaBuilder {

	// builder

	@BuilderParent
	ConsoleContextMetaBuilderContainer container;

	@BuilderSource
	ObjectContextSpec spec;

	@BuilderTarget
	ConsoleMetaModuleImplementation metaModule;

	// prototype dependencies

	@Inject
	Provider<ConsoleContextRootExtensionPoint> rootExtensionPointProvider;

	// state

	String contextTypeName;
	String beanName;

	Boolean hasListChildren;
	Boolean hasObjectChildren;

	List<String> listContextTypeNames;
	List<String> objectContextTypeNames;

	// build

	@BuildMethod
	public
	void build (
			Builder builder) {

		setDefaults ();

		// extension points

		metaModule.addExtensionPoint (
			rootExtensionPointProvider.get ()

			.name (
				contextTypeName + ":list")

			.contextTypeNames (
				listContextTypeNames)

			.contextLinkNames (
				ImmutableList.<String>of (
					contextTypeName))

			.parentContextNames (
				ImmutableList.<String>of (
					naivePluralise (
						contextTypeName),
					contextTypeName))

		);

		metaModule.addExtensionPoint (
			rootExtensionPointProvider.get ()

			.name (
				contextTypeName + ":object")

			.contextTypeNames (
				objectContextTypeNames)

			.contextLinkNames (
				ImmutableList.<String>of (
					contextTypeName))

			.parentContextNames (
				ImmutableList.<String>of (
					contextTypeName,
					"link:" + contextTypeName))

		);

		// context hints

		metaModule.addContextHint (
			new ConsoleContextHint ()

			.linkName (
				contextTypeName)

			.singular (
				true)

			.plural (
				true)

		);

		// descend

		ConsoleContextMetaBuilderContainer listContainer =
			new ConsoleContextMetaBuilderContainer ()

			.structuralName (
				contextTypeName)

			.extensionPointName (
				contextTypeName + ":list");

		builder.descend (
			listContainer,
			spec.listChildren (),
			metaModule,
			MissingBuilderBehaviour.ignore);

		ConsoleContextMetaBuilderContainer objectContainer =
			new ConsoleContextMetaBuilderContainer ()

			.structuralName (
				contextTypeName)

			.extensionPointName (
				contextTypeName + ":object");

		builder.descend (
			objectContainer,
			spec.objectChildren (),
			metaModule,
			MissingBuilderBehaviour.ignore);

	}

	// defaults

	void setDefaults () {

		contextTypeName =
			spec.name ();

		beanName =
			ifNull (
				spec.beanName (),
				contextTypeName);

		if (beanName.contains ("_")) {

			throw new RuntimeException (
				stringFormat (
					"Object context type name %s cannot be used as bean name",
					contextTypeName));

		}

		hasListChildren =
			! spec.listChildren ().isEmpty ();

		hasObjectChildren =
			! spec.objectChildren ().isEmpty ();

		// context type names

		listContextTypeNames =
			ImmutableList.<String>builder ()

			.add (
				contextTypeName + ":list")

			.add (
				contextTypeName + ":combo")

			.build ();

		objectContextTypeNames =
			ImmutableList.<String>builder ()

			.add (
				contextTypeName + ":combo")

			.add (
				contextTypeName + ":object")

			.build ();

	}

}
