package wbs.platform.object.create;

import static wbs.utils.etc.NullUtils.ifNull;
import static wbs.utils.etc.OptionalUtils.optionalAbsent;
import static wbs.utils.etc.OptionalUtils.optionalOf;
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
import wbs.console.forms.object.ParentFormFieldSpec;
import wbs.console.forms.types.FormType;
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
import wbs.framework.component.manager.ComponentManager;
import wbs.framework.database.NestedTransaction;
import wbs.framework.entity.record.Record;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.OwnedTaskLogger;
import wbs.framework.logging.TaskLogger;

import wbs.web.action.Action;

@PrototypeComponent ("objectCreatePageBuilder")
@ConsoleModuleBuilderHandler
public
class ObjectCreatePageBuilder <
	ObjectType extends Record <ObjectType>,
	ParentType extends Record <ParentType>
>
	implements BuilderComponent {

	// singleton dependences

	@SingletonDependency
	ComponentManager componentManager;

	@SingletonDependency
	ConsoleMetaManager consoleMetaManager;

	@SingletonDependency
	ConsoleModuleBuilder consoleModuleBuilder;

	@SingletonDependency
	FormContextManager formContextManager;

	@ClassSingletonDependency
	LogContext logContext;

	// prototype dependencies

	@PrototypeDependency
	Provider <ConsoleFile> consoleFileProvider;

	@PrototypeDependency
	Provider <ConsoleContextTab> contextTabProvider;

	@PrototypeDependency
	Provider <ObjectCreateAction <ObjectType, ParentType>>
	objectCreateActionProvider;

	@PrototypeDependency
	Provider <ObjectCreatePart <ObjectType, ParentType>>
	objectCreatePartProvider;

	@PrototypeDependency
	Provider <TabContextResponder> tabContextResponderProvider;

	// builder

	@BuilderParent
	ConsoleContextBuilderContainer<ObjectType> container;

	@BuilderSource
	ObjectCreatePageSpec spec;

	@BuilderTarget
	ConsoleModuleImplementation consoleModule;

	// state

	ConsoleHelper <ObjectType> consoleHelper;

	String name;
	String typeCode;
	String tabName;
	String tabLabel;
	String localFile;
	String responderName;
	String targetContextTypeName;
	String targetResponderName;
	//FieldsProvider <ObjectType, ParentType> fieldsProvider;
	FormContextBuilder <ObjectType> formContextBuilder;
	String createTimeFieldName;
	String createUserFieldName;
	String createPrivDelegate;
	String createPrivCode;
	String privKey;

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

				buildTab (
					resolvedExtensionPoint);

				buildFile (
					resolvedExtensionPoint);

			}

			buildResponder ();

		}

	}

	void buildTab (
			@NonNull ResolvedConsoleContextExtensionPoint extensionPoint) {

		consoleModule.addContextTab (
			container.taskLogger (),
			"end",

			contextTabProvider.get ()

				.name (
					tabName)

				.defaultLabel (
					tabLabel)

				.localFile (
					localFile)

				/*.privKeys (
				 * 	Collections.singletonList (privKey))*/,

			extensionPoint.contextTypeNames ());

	}

	void buildFile (
			ResolvedConsoleContextExtensionPoint resolvedExtensionPoint) {

		Provider <Action> createActionProvider =
			() -> objectCreateActionProvider.get ()

			.consoleHelper (
				consoleHelper)

			.typeCode (
				typeCode)

			.responderName (
				responderName)

			.targetContextTypeName (
				targetContextTypeName)

			.targetResponderName (
				targetResponderName)

			.createPrivDelegate (
				createPrivDelegate)

			.createPrivCode (
				createPrivCode)

			.formContextBuilder (
				formContextBuilder)

			.createTimeFieldName (
				createTimeFieldName)

			.createUserFieldName (
				createUserFieldName);

		consoleModule.addContextFile (

			localFile,

			consoleFileProvider.get ()

				.getResponderName (
					responderName)

				.postActionProvider (
					createActionProvider)

				/*.privKeys (
					Collections.singletonList (privKey)*/,

			resolvedExtensionPoint.contextTypeNames ());

	}

	void buildResponder () {

		PagePartFactory partFactory =
			parentTransaction -> {

			try (

				NestedTransaction transaction =
					parentTransaction.nestTransaction (
						logContext,
						"buildResponder");

			) {

				return objectCreatePartProvider.get ()

					.consoleHelper (
						consoleHelper)

					.formContextBuilder (
						formContextBuilder)

					.parentPrivCode (
						createPrivCode)

					.localFile (
						localFile);

			}

		};

		consoleModule.addResponder (

			responderName,

			tabContextResponderProvider.get ()

				.tab (
					tabName)

				.title (
					capitalise (
						consoleHelper.friendlyName () + " create"))

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
				container.consoleHelper ();

			name =
				ifNull (
					spec.name (),
					"create");

			typeCode =
				spec.typeCode ();

			tabName =
				ifNull (
					spec.tabName (),
					stringFormat (
						"%s.%s",
						container.pathPrefix (),
						name));

			tabLabel =
				capitalise (
					name);

			localFile =
				ifNull (
					spec.localFile (),
					stringFormat (
						"%s.%s",
						container.pathPrefix (),
						name));

			responderName =
				ifNull (
					spec.responderName (),
					stringFormat (
						"%s%sResponder",
						container.newBeanNamePrefix (),
						capitalise (
							name)));

			targetContextTypeName =
				ifNull (
					spec.targetContextTypeName (),
					consoleHelper.objectName () + ":combo");

			targetResponderName =
				ifNull (
					spec.targetResponderName (),
					stringFormat (
						"%sSettingsResponder",
						consoleHelper.objectName ()));

			createPrivDelegate =
				spec.createPrivDelegate ();

			createPrivCode =
				ifNull (
					spec.createPrivCode (),
					stringFormat (
						"%s_create",
						consoleHelper.objectTypeCode ()));

			formContextBuilder =
				formContextManager.createFormContextBuilder (
					consoleModule,
					name,
					consoleHelper.objectClass (),
					FormType.create,
					optionalOf (
						spec.formFieldsName ()),
					optionalAbsent ());

			// if a provider name is provided

			/*
			if (spec.fieldsProviderName () != null) {

				fieldsProvider =
					genericCastUnchecked (
						componentManager.getComponentRequired (
							taskLogger,
							spec.fieldsProviderName (),
							FieldsProvider.class));

			}
			*/

			createTimeFieldName =
				spec.createTimeFieldName ();

			createUserFieldName =
				spec.createUserFieldName ();

			privKey =
				spec.privKey ();

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

			// parent

			List <Object> formFieldSpecs =
				new ArrayList<> ();

			if (consoleHelper.canGetParent ()) {

				formFieldSpecs.add (
					new ParentFormFieldSpec ()

					.createPrivDelegate (
						createPrivDelegate)

					.createPrivCode (
						createPrivCode));

			}

			if (consoleHelper.codeExists ()
					&& ! consoleHelper.nameExists ()) {

				formFieldSpecs.add (
					new CodeFormFieldSpec ());

			}

			if (consoleHelper.nameExists ()) {

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
					"%s.create",
					consoleHelper.objectName ());

			return consoleModuleBuilder.buildFormFieldSet (
				taskLogger,
				consoleHelper,
				fieldSetName,
				formFieldSpecs);

		}

	}

}
