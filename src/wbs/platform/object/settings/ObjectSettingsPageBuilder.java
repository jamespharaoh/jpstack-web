package wbs.platform.object.settings;

import static wbs.utils.etc.LogicUtils.ifNotNullThenElse;
import static wbs.utils.etc.NullUtils.ifNull;
import static wbs.utils.etc.OptionalUtils.optionalAbsent;
import static wbs.utils.etc.OptionalUtils.optionalOf;
import static wbs.utils.etc.TypeUtils.genericCastUnchecked;
import static wbs.utils.string.StringUtils.camelToSpaces;
import static wbs.utils.string.StringUtils.capitalise;
import static wbs.utils.string.StringUtils.stringFormat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.inject.Provider;

import lombok.NonNull;

import wbs.console.context.ConsoleContextBuilderContainer;
import wbs.console.context.ResolvedConsoleContextExtensionPoint;
import wbs.console.forms.core.ConsoleFormBuilder;
import wbs.console.forms.core.ConsoleFormManager;
import wbs.console.forms.core.ConsoleFormType;
import wbs.console.forms.core.FormFieldSet;
import wbs.console.forms.object.CodeFormFieldSpec;
import wbs.console.forms.object.DescriptionFormFieldSpec;
import wbs.console.forms.object.IdFormFieldSpec;
import wbs.console.forms.object.NameFormFieldSpec;
import wbs.console.forms.types.FormType;
import wbs.console.helper.core.ConsoleHelper;
import wbs.console.helper.manager.ConsoleObjectManager;
import wbs.console.module.ConsoleManager;
import wbs.console.module.ConsoleMetaManager;
import wbs.console.module.ConsoleModuleBuilderComponent;
import wbs.console.module.ConsoleModuleImplementation;
import wbs.console.part.PagePartFactory;
import wbs.console.responder.ConsoleFile;
import wbs.console.tab.ConsoleContextTab;
import wbs.console.tab.TabContextResponder;

import wbs.framework.builder.Builder;
import wbs.framework.builder.annotations.BuildMethod;
import wbs.framework.builder.annotations.BuilderParent;
import wbs.framework.builder.annotations.BuilderSource;
import wbs.framework.builder.annotations.BuilderTarget;
import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.PrototypeDependency;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.component.annotations.WeakSingletonDependency;
import wbs.framework.component.manager.ComponentManager;
import wbs.framework.database.NestedTransaction;
import wbs.framework.entity.record.Record;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.OwnedTaskLogger;
import wbs.framework.logging.TaskLogger;

import wbs.web.action.Action;

@PrototypeComponent ("objectSettingsPageBuilder")
public
class ObjectSettingsPageBuilder <
	ObjectType extends Record <ObjectType>,
	ParentType extends Record <ParentType>
> implements ConsoleModuleBuilderComponent {

	// singleton dependencies

	@SingletonDependency
	ComponentManager componentManager;

	@SingletonDependency
	ConsoleFormBuilder consoleFormBuilder;

	@WeakSingletonDependency
	ConsoleManager consoleManager;

	@SingletonDependency
	ConsoleMetaManager consoleMetaManager;

	@SingletonDependency
	ConsoleFormManager formContextManager;

	@ClassSingletonDependency
	LogContext logContext;

	@WeakSingletonDependency
	ConsoleObjectManager objectManager;

	// prototype dependencies

	@PrototypeDependency
	Provider <ConsoleFile> consoleFile;

	@PrototypeDependency
	Provider <ConsoleContextTab> contextTab;

	@PrototypeDependency
	Provider <ObjectRemoveAction> objectRemoveAction;

	@PrototypeDependency
	Provider <ObjectSettingsAction <ObjectType, ParentType>>
	objectSettingsActionProvider;

	@PrototypeDependency
	Provider <ObjectSettingsPart <ObjectType, ParentType>>
	objectSettingsPartProvider;

	@PrototypeDependency
	Provider <TabContextResponder> tabContextResponder;

	// builder

	@BuilderParent
	ConsoleContextBuilderContainer <ObjectType> container;

	@BuilderSource
	ObjectSettingsPageSpec spec;

	@BuilderTarget
	ConsoleModuleImplementation consoleModule;

	// state

	ConsoleHelper <ObjectType> consoleHelper;

	ConsoleFormType <ObjectType> formContextBuilder;

	//FormFieldSet <ObjectType> formFieldSet;
	//FieldsProvider <ObjectType, ParentType> fieldsProvider;

	String privKey;
	String name;
	String shortName;
	String longName;
	String friendlyLongName;
	String friendlyShortName;
	String responderName;
	String fileName;
	String tabName;
	String tabLocation;

	Provider <Action> settingsActionProvider;

	// build

	@Override
	@BuildMethod
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

			buildAction ();
			buildResponder ();

			for (
				ResolvedConsoleContextExtensionPoint resolvedExtensionPoint
					: consoleMetaManager.resolveExtensionPoint (
						container.extensionPointName ())
			) {

				buildTab (
					taskLogger,
					resolvedExtensionPoint);

				buildFile (
					resolvedExtensionPoint);

			}

		}

	}

	void buildTab (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull ResolvedConsoleContextExtensionPoint extensionPoint) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"buildTab");

		) {

			consoleModule.addContextTab (
				taskLogger,
				"end",
				contextTab.get ()

					.name (
						tabName)

					.defaultLabel (
						capitalise (friendlyShortName))

					.localFile (
						fileName)

					.privKeys (
						Collections.singletonList (
							privKey)),

				extensionPoint.contextTypeNames ());

		}

	}

	void buildAction () {

		if (consoleHelper.ephemeral ()) {

			settingsActionProvider =
				() -> objectSettingsActionProvider.get ()

				.detailsResponder (
					consoleManager.responder (
						responderName,
						true))

				.accessDeniedResponder (
					consoleManager.responder (
						responderName,
						true))

				.editPrivKey (
					privKey)

				.objectLookup (
					consoleHelper)

				.consoleHelper (
					consoleHelper)

				.formContextBuilder (
					formContextBuilder)

				.objectRefName (
					consoleHelper.codeExists ()
						? consoleHelper.codeFieldName ()
						: "id")

				.objectType (
					consoleHelper.objectTypeCode ());

		} else {

			settingsActionProvider =
				() -> objectSettingsActionProvider.get ()

				.detailsResponder (
					consoleManager.responder (
						responderName,
						true))

				.accessDeniedResponder (
					consoleManager.responder (
						responderName,
						true))

				.editPrivKey (
					privKey)

				.objectLookup (
					consoleHelper)

				.consoleHelper (
						consoleHelper)

				.formContextBuilder (
					formContextBuilder)

			;

		}

	}

	void buildFile (
			ResolvedConsoleContextExtensionPoint resolvedExtensionPoint) {

		consoleModule.addContextFile (

			fileName,

			consoleFile.get ()

				.getResponderName (
					responderName)

				.postActionProvider (
					settingsActionProvider)

				.privName (
					privKey),

			resolvedExtensionPoint.contextTypeNames ());

		if (consoleHelper.ephemeral ()) {

			Provider <Action> removeActionProvider =
				() -> objectRemoveAction.get ()

				.objectHelper (
					consoleHelper)

				.settingsResponder (
					consoleManager.responder (
						responderName,
						true))

				.listResponder (
					consoleManager.responder (
						stringFormat (
							"%sListResponder",
							container.newBeanNamePrefix ()),
						true))

				.nextContextTypeName (
					ifNull (
						spec.listContextTypeName (),
					consoleHelper.objectName () + ":list"))

				.editPrivKey (
					privKey);

			consoleModule.addContextFile (

				stringFormat (
					"%s.remove",
					container.structuralName ()),

				consoleFile.get ()

					.postActionProvider (
						removeActionProvider)

					.privName (
						privKey),

				resolvedExtensionPoint.contextTypeNames ());

		}

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

				return objectSettingsPartProvider.get ()

					.objectLookup (
						consoleHelper)

					.consoleHelper (
						consoleHelper)

					.editPrivKey (
						privKey)

					.localName (
						"/" + fileName)

					.formContextBuilder (
						formContextBuilder)

					.removeLocalName (
						consoleHelper.ephemeral ()
							? stringFormat (
								"/%s.remove",
								container.structuralName ())
							: null);

			}

		};

		consoleModule.addResponder (

			responderName,

			tabContextResponder.get ()

				.tab (
					tabName)

				.title (
					capitalise (
						friendlyLongName))

				.pagePartFactory (
					partFactory));

	}

	void setDefaults (
			@NonNull TaskLogger parentTaskLogger) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"setDefaults");

		) {

			consoleHelper =
				genericCastUnchecked (
					ifNotNullThenElse (
						spec.objectName (),
						() -> objectManager.findConsoleHelperRequired (
							spec.objectName ()),
						() -> container.consoleHelper ()));

			name =
				spec.name ();

			shortName =
				ifNull (
					spec.shortName (),
					"settings");

			longName =
				ifNull (
					spec.longName (),
					"settings");

			friendlyShortName =
				ifNull (
					spec.friendlyShortName (),
					camelToSpaces (
						shortName));

			friendlyLongName =
				ifNull (
					spec.friendlyLongName (),
					stringFormat (
						"%s %s",
						consoleHelper.friendlyName (),
						camelToSpaces (
							longName)));

			responderName =
				ifNull (
					spec.responderName (),
					stringFormat (
						"%s%s%s",
						container.newBeanNamePrefix (),
						capitalise (shortName),
						"Responder"));

			fileName =
				ifNull (
					spec.fileName (),
					stringFormat (
						"%s.%s",
						container.pathPrefix (),
						shortName));

			tabName =
				ifNull (
					spec.tabName (),
					stringFormat (
						"%s.%s",
						container.pathPrefix (),
						shortName));

			privKey =
				ifNull (
					spec.privKey (),
					stringFormat (
						"%s.manage",
						consoleHelper.objectName ()));

			formContextBuilder =
				ifNotNullThenElse (
					spec.formFieldsName (),
					() -> formContextManager.createFormType (
						taskLogger,
						consoleModule,
						shortName,
						consoleHelper.objectClass (),
						FormType.update,
						optionalOf (
							spec.formFieldsName ()),
						optionalAbsent ()),
					() -> formContextManager.createFormType (
						taskLogger,
						shortName,
						consoleHelper.objectClass (),
						FormType.update,
						optionalOf (
							defaultFields (
								taskLogger)),
						optionalAbsent ()));

			// if a provider name is provided

			/*
			if (spec.fieldsProviderName () != null) {

				fieldsProvider =
					genericCastUnchecked (
						componentManager.getComponentRequired (
							parentTaskLogger,
							spec.fieldsProviderName (),
							FieldsProvider.class));

			}

			else {

				fieldsProvider =
					null;

			}
			*/

		}

	}

	private
	FormFieldSet <ObjectType> defaultFields (
			@NonNull TaskLogger parentTaskLogger) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"defaultFields");

		) {

			List <Object> formFieldSpecs =
				new ArrayList<> ();

			formFieldSpecs.add (
				new IdFormFieldSpec ());

			if (consoleHelper.codeExists ()) {

				formFieldSpecs.add (
					new CodeFormFieldSpec ());

			}

			if (
				consoleHelper.nameExists ()
				&& ! consoleHelper.nameIsCode ()
			) {

				formFieldSpecs.add (
					new NameFormFieldSpec ());

			}

			if (consoleHelper.descriptionExists ()) {

				formFieldSpecs.add (
					new DescriptionFormFieldSpec ());

			}

			String fieldSetName =
				stringFormat (
					"%s.settings",
					consoleHelper.objectName ());

			return consoleFormBuilder.buildFormFieldSet (
				taskLogger,
				consoleHelper,
				fieldSetName,
				formFieldSpecs);

		}

	}

}
