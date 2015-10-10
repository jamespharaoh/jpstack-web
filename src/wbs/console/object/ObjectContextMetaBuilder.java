package wbs.console.object;

import static wbs.framework.utils.etc.Misc.ifNull;
import static wbs.framework.utils.etc.Misc.stringFormat;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Provider;

import wbs.console.annotations.ConsoleMetaModuleBuilderHandler;
import wbs.console.context.ConsoleContextMetaBuilderContainer;
import wbs.console.context.ConsoleContextRootExtensionPoint;
import wbs.console.module.ConsoleMetaModuleImpl;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.builder.Builder;
import wbs.framework.builder.annotations.BuildMethod;
import wbs.framework.builder.annotations.BuilderParent;
import wbs.framework.builder.annotations.BuilderSource;
import wbs.framework.builder.annotations.BuilderTarget;

import com.google.common.collect.ImmutableList;

@PrototypeComponent ("objectContextMetaBuilder")
@ConsoleMetaModuleBuilderHandler
public
class ObjectContextMetaBuilder {

	// prototype dependencies

	@Inject
	Provider<ConsoleContextRootExtensionPoint> rootExtensionPointProvider;

	// builder

	@BuilderParent
	ConsoleContextMetaBuilderContainer container;

	@BuilderSource
	ObjectContextSpec spec;

	@BuilderTarget
	ConsoleMetaModuleImpl metaModule;

	// state

	String contextName;
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
				contextName + ":list")

			.contextTypeNames (
				listContextTypeNames)

			.contextLinkNames (
				ImmutableList.<String>of (
					contextName))

			.parentContextNames (
				ImmutableList.<String>of (
					contextName + "s",
					contextName)));

		metaModule.addExtensionPoint (
			rootExtensionPointProvider.get ()

			.name (
				contextName + ":object")

			.contextTypeNames (
				objectContextTypeNames)

			.contextLinkNames (
				ImmutableList.<String>of (
					contextName))

			.parentContextNames (
				ImmutableList.<String>of (
					contextName,
					"link:" + contextName)));

		// descend

		ConsoleContextMetaBuilderContainer listContainer =
			new ConsoleContextMetaBuilderContainer ()

			.structuralName (
				contextName)

			.extensionPointName (
				contextName + ":list");

		builder.descend (
			listContainer,
			spec.listChildren (),
			metaModule);

		ConsoleContextMetaBuilderContainer objectContainer =
			new ConsoleContextMetaBuilderContainer ()

			.structuralName (
				contextName)

			.extensionPointName (
				contextName + ":object");

		builder.descend (
			objectContainer,
			spec.objectChildren (),
			metaModule);

	}

	// defaults

	void setDefaults () {

		contextName =
			spec.name ();

		beanName =
			ifNull (
				spec.beanName (),
				contextName);

		if (beanName.contains ("_")) {

			throw new RuntimeException (
				stringFormat (
					"Object context name %s cannot be used as bean name",
					contextName));

		}

		hasListChildren =
			! spec.listChildren ().isEmpty ();

		hasObjectChildren =
			! spec.objectChildren ().isEmpty ();

		// context type names

		listContextTypeNames =
			ImmutableList.<String>builder ()

			.add (
				contextName + "s")

			.add (
				contextName + "+")

			.build ();

		objectContextTypeNames =
			ImmutableList.<String>builder ()

			.add (
				contextName + "+")

			.add (
				contextName)

			.build ();

	}

}
