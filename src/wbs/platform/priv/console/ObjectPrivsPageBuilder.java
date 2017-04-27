package wbs.platform.priv.console;

import static wbs.utils.etc.NullUtils.ifNull;
import static wbs.utils.string.StringUtils.capitalise;
import static wbs.utils.string.StringUtils.stringFormat;

import javax.inject.Provider;

import com.google.common.collect.ImmutableList;

import lombok.NonNull;

import wbs.console.annotations.ConsoleModuleBuilderHandler;
import wbs.console.context.ConsoleContextBuilderContainer;
import wbs.console.context.ResolvedConsoleContextExtensionPoint;
import wbs.console.helper.core.ConsoleHelper;
import wbs.console.module.ConsoleMetaManager;
import wbs.console.module.ConsoleModuleImplementation;
import wbs.console.part.PagePartFactory;
import wbs.console.responder.ConsoleFile;
import wbs.console.tab.ConsoleContextTab;
import wbs.console.tab.TabContextResponder;

import wbs.framework.builder.Builder;
import wbs.framework.builder.BuilderComponent;
import wbs.framework.builder.annotations.BuildMethod;
import wbs.framework.builder.annotations.BuilderParent;
import wbs.framework.builder.annotations.BuilderSource;
import wbs.framework.builder.annotations.BuilderTarget;
import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.PrototypeDependency;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.entity.record.Record;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.TaskLogger;

@PrototypeComponent ("objectPrivsPageBuilder")
@ConsoleModuleBuilderHandler
public
class ObjectPrivsPageBuilder <
	ObjectType extends Record <ObjectType>
> implements BuilderComponent {

	// singleton dependencies

	@SingletonDependency
	ConsoleMetaManager consoleMetaManager;

	// prototype dependencies

	@PrototypeDependency
	Provider <ConsoleFile> consoleFile;

	@PrototypeDependency
	Provider <ConsoleContextTab> contextTab;

	@ClassSingletonDependency
	LogContext logContext;

	@PrototypeDependency
	Provider <TabContextResponder> tabContextResponder;

	@PrototypeDependency
	Provider <ObjectPrivsPart <ObjectType>> objectPrivsPartProvider;

	// builder

	@BuilderParent
	ConsoleContextBuilderContainer <ObjectType> container;

	@BuilderSource
	ObjectPrivsPageSpec spec;

	@BuilderTarget
	ConsoleModuleImplementation consoleModule;

	// state

	ConsoleHelper <ObjectType> consoleHelper;

	String name;
	String tabName;
	String tabLabel;
	String fileName;
	String responderName;

	// build

	@BuildMethod
	@Override
	public
	void build (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull Builder builder) {

		try (

			TaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"build");

		) {

			setDefaults (
				taskLogger);

			buildResponder (
				taskLogger);

			for (
				ResolvedConsoleContextExtensionPoint resolvedExtensionPoint
					: consoleMetaManager.resolveExtensionPoint (
						container.extensionPointName ())
			) {

				buildContextTab (
					taskLogger,
					resolvedExtensionPoint);

				buildContextFile (
					taskLogger,
					resolvedExtensionPoint);

			}

		}

	}

	void buildContextTab (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull ResolvedConsoleContextExtensionPoint extensionPoint) {

		consoleModule.addContextTab (
			container.taskLogger (),
			"end",

			contextTab.get ()

				.name (
					tabName)

				.defaultLabel (
					tabLabel)

				.localFile (
					fileName)

				.privKeys (
					ImmutableList.of (
						stringFormat (
							"%s.manage",
							consoleHelper.objectName ()))),

			extensionPoint.contextTypeNames ());

	}

	void buildContextFile (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull ResolvedConsoleContextExtensionPoint
				resolvedExtensionPoint) {

		consoleModule.addContextFile (

			fileName,

			consoleFile.get ()

				.getResponderName (
					responderName)

				.privKeys (
					ImmutableList.of (
						stringFormat (
							"%s.manage",
							consoleHelper.objectName ()))),

			resolvedExtensionPoint.contextTypeNames ());

	}

	void buildResponder (
			@NonNull TaskLogger parentTaskLogger) {

		PagePartFactory privsPartFactory =
			nestedTaskLogger ->
				objectPrivsPartProvider.get ()

			.consoleHelper (
				consoleHelper)

		;

		consoleModule.addResponder (
			responderName,
			tabContextResponder.get ()

			.tab (
				tabName)

			.title (
				capitalise (
					stringFormat (
						"%s privs",
						consoleHelper.friendlyName ())))

			.pagePartFactory (
				privsPartFactory)

		);

	}

	void setDefaults (
			@NonNull TaskLogger parentTaskLogger) {

		consoleHelper =
			container.consoleHelper ();

		name =
			ifNull (
				spec.name (),
				"privs");

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
				"Privs");

		fileName =
			ifNull (
				spec.fileName (),
				stringFormat (
					"%s.%s",
					container.pathPrefix (),
					name));

		responderName =
			stringFormat (
				"%s%sResponder",
				container.newBeanNamePrefix (),
				capitalise (
					name));


	}

}
