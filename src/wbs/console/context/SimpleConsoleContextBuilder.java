package wbs.console.context;

import static wbs.framework.utils.etc.StringUtils.capitalise;
import static wbs.framework.utils.etc.NullUtils.ifNull;
import static wbs.framework.utils.etc.StringUtils.stringFormat;
import static wbs.framework.utils.etc.StringUtils.camelToSpaces;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Provider;

import lombok.NonNull;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;

import wbs.console.annotations.ConsoleModuleBuilderHandler;
import wbs.console.helper.ConsoleObjectManager;
import wbs.console.helper.PrivKeySpec;
import wbs.console.module.ConsoleMetaManager;
import wbs.console.module.ConsoleModuleImplementation;
import wbs.console.module.ResolvedConsoleContextLink;
import wbs.console.module.SimpleConsoleBuilderContainer;
import wbs.console.object.ObjectContext;
import wbs.console.tab.ConsoleContextTab;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.application.context.ApplicationContext;
import wbs.framework.builder.Builder;
import wbs.framework.builder.Builder.MissingBuilderBehaviour;
import wbs.framework.builder.annotations.BuildMethod;
import wbs.framework.builder.annotations.BuilderParent;
import wbs.framework.builder.annotations.BuilderSource;
import wbs.framework.builder.annotations.BuilderTarget;
import wbs.framework.entity.record.Record;

@PrototypeComponent ("simpleConsoleContextBuilder")
@ConsoleModuleBuilderHandler
public
class SimpleConsoleContextBuilder<
	ObjectType extends Record<ObjectType>
> {

	// dependencies

	@Inject
	ApplicationContext applicationContext;

	@Inject
	ConsoleMetaManager consoleMetaManager;

	@Inject
	ConsoleObjectManager objectManager;

	// prototype dependencies

	@Inject
	Provider<ConsoleContextTab> contextTab;

	@Inject
	Provider<ConsoleContextType> contextType;

	@Inject
	Provider<ObjectContext> objectContext;

	@Inject
	Provider<SimpleConsoleContext> simpleContext;

	// builder

	@BuilderParent
	SimpleConsoleBuilderContainer simpleContainerSpec;

	@BuilderSource
	SimpleConsoleContextSpec simpleContextSpec;

	@BuilderTarget
	ConsoleModuleImplementation consoleModule;

	// state

	String name;
	String structuralName;
	String contextName;
	String contextTypeName;
	String typeName;
	String title;

	List<PrivKeySpec> privKeySpecs;

	// build

	@BuildMethod
	public
	void build (
			@NonNull Builder builder) {

		setDefaults ();

		buildContextType ();
		buildSimpleContext ();

		List<ResolvedConsoleContextLink> resolvedContextLinks =
			consoleMetaManager.resolveContextLink (
				name);

		for (
			ResolvedConsoleContextLink resolvedContextLink
				: resolvedContextLinks
		) {

			buildResolvedContexts (
				resolvedContextLink);

			buildResolvedTabs (
				resolvedContextLink);

		}

		ConsoleContextBuilderContainer<ObjectType> nextBuilderContainer =
			new ConsoleContextBuilderContainerImplementation<ObjectType> ()

			.consoleHelper (
				null)

			.structuralName (
				structuralName)

			.extensionPointName (
				structuralName)

			.pathPrefix (
				structuralName)

			.newBeanNamePrefix (
				structuralName)

			.existingBeanNamePrefix (
				structuralName)

			.tabLocation (
				"end")

			.friendlyName (
				camelToSpaces (
					structuralName));

		builder.descend (
			nextBuilderContainer,
			simpleContextSpec.children (),
			consoleModule,
			MissingBuilderBehaviour.error);


	}

	void buildContextType () {

		consoleModule.addContextType (
			contextType.get ()

			.name (
				contextTypeName));

	}

	void buildSimpleContext () {

		consoleModule.addContext (
			simpleContext.get ()

			.name (
				contextName)

			.typeName (
				contextTypeName)

			.pathPrefix (
				"/" + contextName)

			.global (
				true)

			.title (
				title)

			.privKeySpecs (
				privKeySpecs)

		);

	}

	void buildResolvedContexts (
			@NonNull ResolvedConsoleContextLink resolvedContextLink) {

		for (
			String parentContextName
				: resolvedContextLink.parentContextNames ()
		) {

			String resolvedContextName =
				stringFormat (
					"%s.%s",
					parentContextName,
					resolvedContextLink.localName ());

			consoleModule.addContext (
				simpleContext.get ()

				.name (
					resolvedContextName)

				.typeName (
					contextTypeName)

				.pathPrefix (
					"/" + resolvedContextName)

				.global (
					true)

				.title (
					title)

				.privKeySpecs (
					privKeySpecs)

				.parentContextName (
					parentContextName)

				.parentContextTabName (
					resolvedContextLink.tabName ()));

		}

	}

	void buildResolvedTabs (
			ResolvedConsoleContextLink resolvedConsoleContextLink) {

		consoleModule.addContextTab (
			resolvedConsoleContextLink.tabLocation (),

			contextTab.get ()

				.name (
					resolvedConsoleContextLink.tabName ())

				.defaultLabel (
					resolvedConsoleContextLink.tabLabel ())

				.privKeys (
					resolvedConsoleContextLink.tabPrivKey ())

				.localFile (
					"type:" + name),

			resolvedConsoleContextLink.tabContextTypeNames ());

	}

	void setDefaults () {

		name =
			simpleContextSpec.name ();

		structuralName =
			name;

		contextName =
			name;

		contextTypeName =
			name;

		typeName =
			ifNull (
				simpleContextSpec.typeName (),
				structuralName);

		title =
			ifNull (
				simpleContextSpec.title (),
				capitalise (
					camelToSpaces (
						structuralName)));

		// TODO fix this

		privKeySpecs =
			ImmutableList.<PrivKeySpec>copyOf (
				Iterables.filter (
					simpleContextSpec.children (),
					PrivKeySpec.class));

	}

}
