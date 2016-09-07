package wbs.console.context;

import static wbs.framework.utils.etc.NullUtils.ifNull;
import static wbs.framework.utils.etc.StringUtils.camelToSpaces;
import static wbs.framework.utils.etc.StringUtils.capitalise;
import static wbs.framework.utils.etc.StringUtils.joinWithoutSeparator;
import static wbs.framework.utils.etc.StringUtils.stringFormat;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Provider;

import com.google.common.collect.Iterables;

import lombok.NonNull;

import wbs.console.annotations.ConsoleModuleBuilderHandler;
import wbs.console.helper.ConsoleHelper;
import wbs.console.module.ConsoleMetaManager;
import wbs.console.module.ConsoleModuleImplementation;
import wbs.console.tab.ConsoleContextTab;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.application.annotations.PrototypeDependency;
import wbs.framework.application.annotations.SingletonDependency;
import wbs.framework.builder.Builder;
import wbs.framework.builder.Builder.MissingBuilderBehaviour;
import wbs.framework.builder.annotations.BuildMethod;
import wbs.framework.builder.annotations.BuilderParent;
import wbs.framework.builder.annotations.BuilderSource;
import wbs.framework.builder.annotations.BuilderTarget;
import wbs.framework.entity.record.Record;

@PrototypeComponent ("consoleContextSectionBuilder")
@ConsoleModuleBuilderHandler
public
class ConsoleContextSectionBuilder <
	ObjectType extends Record <ObjectType>
> {

	// singleton dependencies

	@SingletonDependency
	ConsoleMetaManager consoleMetaManager;

	// prototype dependencies

	@PrototypeDependency
	Provider <ConsoleContextTab> contextTabProvider;

	@PrototypeDependency
	Provider <ConsoleContextType> contextTypeProvider;

	@PrototypeDependency
	Provider <SimpleConsoleContext> simpleConsoleContextProvider;

	// builder

	@BuilderParent
	ConsoleContextBuilderContainer <ObjectType> container;

	@BuilderSource
	ConsoleContextSectionSpec spec;

	@BuilderTarget
	ConsoleModuleImplementation consoleModule;

	// state

	ConsoleHelper<ObjectType> consoleHelper;

	String name;
	String structuralName;
	String aliasOf;
	String label;
	String contextTypeName;
	String tabName;
	String tabTarget;

	// build

	@BuildMethod
	public
	void build (
			Builder builder) {

		setDefaults ();

		buildContextTypes ();

		for (
			ResolvedConsoleContextExtensionPoint resolvedExtensionPoint
				: consoleMetaManager.resolveExtensionPoint (
					container.extensionPointName ())
		) {

			buildResolvedContexts (
				resolvedExtensionPoint);

			buildContextTabs (
				resolvedExtensionPoint);

		}

		ConsoleContextBuilderContainer<ObjectType> nextContextBuilderContainer =
			new ConsoleContextBuilderContainerImplementation<ObjectType> ()

			.consoleHelper (
				consoleHelper)

			.structuralName (
				structuralName)

			.extensionPointName (
				"section:" + contextTypeName)

			.pathPrefix (
				contextTypeName)

			.existingBeanNamePrefix (
				stringFormat (
					"%s%s",
					container.existingBeanNamePrefix (),
					capitalise (
						aliasOf)))

			.newBeanNamePrefix (
				stringFormat (
					"%s%s",
					container.newBeanNamePrefix (),
					capitalise (
						name)))

			.tabLocation (
				"end")

			.friendlyName (
				container.friendlyName ());

		builder.descend (
			nextContextBuilderContainer,
			spec.children (),
			consoleModule,
			MissingBuilderBehaviour.error);

	}

	void buildContextTypes () {

		consoleModule.addContextType (
			contextTypeProvider.get ()

			.name (
				contextTypeName)

		);

	}

	void buildResolvedContexts (
			@NonNull ResolvedConsoleContextExtensionPoint resolvedExtensionPoint) {

		Map<String,Object> stuffMap =
			new HashMap<String,Object> ();

		for (
			ConsoleContextStuffSpec contextStuffSpec
				: Iterables.filter (
					spec.children (),
					ConsoleContextStuffSpec.class)
		) {

			stuffMap.put (
				contextStuffSpec.name (),
				contextStuffSpec.value ());

		}

		for (
			String parentContextName
				: resolvedExtensionPoint.parentContextNames ()
		) {

				String resolvedContextName =
					stringFormat (
						"%s.%s",
						parentContextName,
						spec.name ());

				boolean link =
					resolvedContextName.startsWith ("link:");

				String resolvedPathPrefix =
					joinWithoutSeparator (
						"/",
						link
							? resolvedContextName.substring (5)
							: resolvedContextName);

			consoleModule.addContext (
				simpleConsoleContextProvider.get ()

				.name (
					stringFormat (
						"%s.%s",
						parentContextName,
						spec.name ()))

				.typeName (
					contextTypeName)

				.pathPrefix (
					resolvedPathPrefix)

				.global (
					! link)

				.title (
					label)

				.parentContextName (
					parentContextName)

				.parentContextTabName (
					tabName)

				.stuff (
					stuffMap));

		}

	}

	void buildContextTabs (
			ResolvedConsoleContextExtensionPoint resolvedExtensionPoint) {

		consoleModule.addContextTab (

			container.tabLocation (),

			contextTabProvider.get ()

				.name (
					tabName)

				.defaultLabel (
					label)

				.localFile (
					tabTarget),

			resolvedExtensionPoint.contextTypeNames ());

	}

	void setDefaults () {

		consoleHelper =
			container.consoleHelper ();

		name =
			spec.name ();

		structuralName =
			stringFormat (
				"%s.%s",
				container.structuralName (),
				name);

		aliasOf =
			ifNull (
				spec.aliasOf (),
				name);

		label =
			ifNull (
				spec.label (),
				camelToSpaces (
					name));

		contextTypeName =
			structuralName;

		tabName =
			structuralName;

		tabTarget =
			stringFormat (
				"type:%s",
				contextTypeName);

	}

}
