package wbs.console.combo;

import static wbs.utils.etc.NullUtils.ifNull;
import static wbs.utils.etc.OptionalUtils.presentInstances;
import static wbs.utils.string.StringUtils.camelToSpaces;
import static wbs.utils.string.StringUtils.capitalise;
import static wbs.utils.string.StringUtils.stringFormat;

import java.util.Collections;
import java.util.List;

import javax.inject.Provider;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;

import lombok.NonNull;

import wbs.console.annotations.ConsoleModuleBuilderHandler;
import wbs.console.context.ConsoleContextBuilderContainer;
import wbs.console.context.ResolvedConsoleContextExtensionPoint;
import wbs.console.module.ConsoleMetaManager;
import wbs.console.module.ConsoleModuleImplementation;
import wbs.console.responder.ConsoleFile;
import wbs.console.tab.ConsoleContextTab;
import wbs.console.tab.TabContextResponder;
import wbs.framework.builder.Builder;
import wbs.framework.builder.annotations.BuildMethod;
import wbs.framework.builder.annotations.BuilderParent;
import wbs.framework.builder.annotations.BuilderSource;
import wbs.framework.builder.annotations.BuilderTarget;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.PrototypeDependency;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.entity.record.Record;

@PrototypeComponent ("contextTabActionPageBuider")
@ConsoleModuleBuilderHandler
public
class ContextTabActionPageBuilder <
	ObjectType extends Record <ObjectType>
> {

	// singleton dependencies

	@SingletonDependency
	ConsoleMetaManager consoleMetaManager;

	// prototype dependencies

	@PrototypeDependency
	Provider <ConsoleFile> consoleFile;

	@PrototypeDependency
	Provider <ConsoleContextTab> contextTab;

	@PrototypeDependency
	Provider <TabContextResponder> tabContextResponder;

	// builder

	@BuilderParent
	ConsoleContextBuilderContainer <ObjectType> container;

	@BuilderSource
	ContextTabActionPageSpec spec;

	@BuilderTarget
	ConsoleModuleImplementation consoleModule;

	// state

	String name;
	String tabName;
	String tabLabel;
	String localFile;
	Boolean hideTab;
	String responderName;
	String actionName;
	String title;
	String pagePartName;
	List <String> privKeys;

	// build

	@BuildMethod
	public
	void build (
			@NonNull Builder builder) {

		setDefaults ();

		for (
			ResolvedConsoleContextExtensionPoint resolvedExtensionPoint
				: consoleMetaManager.resolveExtensionPoint (
					container.extensionPointName ())
		) {

			buildTab (
				resolvedExtensionPoint);

			buildFile (
				resolvedExtensionPoint);

		}

		buildResponder ();

	}

	void buildTab (
			@NonNull ResolvedConsoleContextExtensionPoint
				resolvedExtensionPoint) {

		consoleModule.addContextTab (
			container.tabLocation (),

			contextTab.get ()

				.name (
					tabName)

				.defaultLabel (
					tabLabel)

				.localFile (
					localFile)

				.privKeys (
					privKeys),

			hideTab
				? Collections.emptyList ()
				: resolvedExtensionPoint.contextTypeNames ());

	}

	void buildFile (
			@NonNull ResolvedConsoleContextExtensionPoint
				resolvedExtensionPoint) {

		consoleModule.addContextFile (
			localFile,
			consoleFile.get ()

				.getResponderName (
					responderName)

				.postActionName (
					actionName)

				.privKeys (
					privKeys),

			resolvedExtensionPoint.contextTypeNames ());

	}

	void buildResponder () {

		consoleModule.addResponder (
			responderName,
			tabContextResponder.get ()

			.tab (
				tabName)

			.title (
				title)

			.pagePartName (
				pagePartName)

		);

	}

	// defaults

	void setDefaults () {

		name =
			spec.name ();

		tabName =
			ifNull (
				spec.tabName (),
				stringFormat (
					"%s.%s",
					container.pathPrefix (),
					name));

		tabLabel =
			ifNull (
				spec.tabLabel (),
				capitalise (
					camelToSpaces (
						name)));

		localFile =
			ifNull (
				spec.localFile (),
				stringFormat (
					"%s.%s",
					container.pathPrefix (),
					name));

		hideTab =
			spec.hideTab ();

		responderName =
			ifNull (
				spec.responderName (),
				stringFormat (
					"%s%sResponder",
					container.newBeanNamePrefix (),
					capitalise (
						name)));

		actionName =
			ifNull (
				spec.actionName (),
				stringFormat (
					"%s%sAction",
					container.existingBeanNamePrefix (),
					capitalise (
						name)));

		title =
			ifNull (
				spec.title (),
				capitalise (
					stringFormat (
						"%s %s",
						container.friendlyName (),
						camelToSpaces (name))));

		pagePartName =
			ifNull (
				spec.pagePartName (),
				stringFormat (
					"%s%sPart",
					container.existingBeanNamePrefix (),
					capitalise (name)));

		privKeys =
			ImmutableList.copyOf (
				presentInstances (
					Optional.fromNullable (
						spec.privKey ())));

	}

}
