package wbs.platform.object.browse;

import static wbs.utils.etc.NullUtils.ifNull;
import static wbs.utils.string.StringUtils.capitalise;
import static wbs.utils.string.StringUtils.stringFormat;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Provider;

import lombok.NonNull;

import wbs.console.annotations.ConsoleModuleBuilderHandler;
import wbs.console.context.ConsoleContextBuilderContainer;
import wbs.console.context.ResolvedConsoleContextExtensionPoint;
import wbs.console.forms.context.FormContextBuilder;
import wbs.console.forms.context.FormContextManager;
import wbs.console.forms.core.FormFieldSet;
import wbs.console.forms.object.CodeFormFieldSpec;
import wbs.console.forms.object.DescriptionFormFieldSpec;
import wbs.console.forms.object.NameFormFieldSpec;
import wbs.console.helper.core.ConsoleHelper;
import wbs.console.module.ConsoleMetaManager;
import wbs.console.module.ConsoleModuleBuilder;
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
import wbs.framework.database.NestedTransaction;
import wbs.framework.entity.record.Record;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.OwnedTaskLogger;
import wbs.framework.logging.TaskLogger;

import wbs.platform.scaffold.model.SliceRec;

@PrototypeComponent ("objectBrowsePageBuilder")
@ConsoleModuleBuilderHandler
public
class ObjectBrowsePageBuilder <
	ObjectType extends Record <ObjectType>
>
	implements BuilderComponent {

	// singleton dependencies

	@SingletonDependency
	ConsoleModuleBuilder consoleModuleBuilder;

	@SingletonDependency
	ConsoleMetaManager consoleMetaManager;

	@SingletonDependency
	FormContextManager formContextManager;

	@ClassSingletonDependency
	LogContext logContext;

	// prototype dependencies

	@PrototypeDependency
	Provider <ConsoleFile> consoleFile;

	@PrototypeDependency
	Provider <ConsoleContextTab> contextTab;

	@PrototypeDependency
	Provider <ObjectBrowsePart <ObjectType>> objectBrowsePart;

	@PrototypeDependency
	Provider <TabContextResponder> tabContextResponder;

	// builder

	@BuilderParent
	ConsoleContextBuilderContainer <ObjectType> container;

	@BuilderSource
	ObjectBrowsePageSpec spec;

	@BuilderTarget
	ConsoleModuleImplementation consoleModule;

	// state

	ConsoleHelper <ObjectType> consoleHelper;

	String typeCode;

	FormContextBuilder <ObjectType> formContextBuilder;

	// build

	@BuildMethod
	@Override
	public
	void build (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull Builder <TaskLogger> builder) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"build");

		) {

			setDefaults (
				taskLogger);

			for (
				ResolvedConsoleContextExtensionPoint resolvedExtensionPoint
					: consoleMetaManager.resolveExtensionPoint (
						container.extensionPointName ())
			) {

				buildContextTab (
					resolvedExtensionPoint);

				buildContextFile (
					resolvedExtensionPoint);

			}

			buildResponder ();

		}

	}

	void buildContextTab (
			@NonNull ResolvedConsoleContextExtensionPoint extensionPoint) {

		consoleModule.addContextTab (
			container.taskLogger (),
			container.tabLocation (),

			contextTab.get ()

				.name (
					container.pathPrefix () + ".browse")

				.defaultLabel (
					"Browse")

				.localFile (
					container.pathPrefix () + ".browse"),

			extensionPoint.contextTypeNames ());

	}

	void buildContextFile (
			@NonNull ResolvedConsoleContextExtensionPoint extensionPoint) {

		consoleModule.addContextFile (

			container.pathPrefix () + ".browse",

			consoleFile.get ()

				.getResponderName (
					stringFormat (
						"%sBrowseResponder",
						container.newBeanNamePrefix ())),

			extensionPoint.contextTypeNames ());

	}

	void buildResponder () {

		PagePartFactory partFactory =
			parentTransaction -> {

			try (

				NestedTransaction transaction =
					parentTransaction.nestTransaction (
						logContext,
						"buildPagePart");

			) {

				return objectBrowsePart.get ()

					.consoleHelper (
						consoleHelper)

					.typeCode (
						typeCode)

					.localName (
						container.pathPrefix () + ".browse")

					.formContextBuilder (
						formContextBuilder)

					.targetContextTypeName (
						ifNull (
							spec.targetContextTypeName (),
							consoleHelper.objectName () + ":combo"));

			}

		};

		consoleModule.addResponder (
			container.newBeanNamePrefix () + "BrowseResponder",
			tabContextResponder.get ()

				.tab (
					container.pathPrefix () + ".browse")

				.title (
					capitalise (
						consoleHelper.friendlyName () + " browse"))

				.pagePartFactory (
					partFactory));

	}

	// defaults

	void setDefaults (
			@NonNull TaskLogger parentTaskLogger) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"setDefaults");

		) {

			consoleHelper =
				container.consoleHelper ();

			typeCode =
				spec.typeCode ();

			formContextBuilder =
				formContextManager.formContextBuilderRequired (
					consoleModule.name (),
					spec.formContextName (),
					consoleHelper.objectClass ());

		}

	}

	FormFieldSet <ObjectType> defaultFields (
			@NonNull TaskLogger parentTaskLogger) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"defaultFields");

		) {

			// create spec

			List <Object> formFieldSpecs =
				new ArrayList<> ();

			if (
				consoleHelper.parentTypeIsFixed ()
				&& consoleHelper.parentClass () == SliceRec.class
			) {

				formFieldSpecs.add (
					new DescriptionFormFieldSpec ()

					.delegate (
						"slice")

					.label (
						"Slice")

				);

			}

			if (consoleHelper.nameIsCode ()) {

				formFieldSpecs.add (
					new CodeFormFieldSpec ());

			} else if (consoleHelper.nameExists ()) {

				formFieldSpecs.add (
					new NameFormFieldSpec ());

			}

			if (consoleHelper.descriptionExists ()) {

				formFieldSpecs.add (
					new DescriptionFormFieldSpec ());

			}

			// build

			String fieldSetName =
				stringFormat (
					"%s.browse",
					consoleHelper.objectName ());

			return consoleModuleBuilder.buildFormFieldSet (
				taskLogger,
				consoleHelper,
				fieldSetName,
				formFieldSpecs);

		}

	}

}
