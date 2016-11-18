package wbs.platform.object.create;

import static wbs.utils.etc.LogicUtils.ifNotNullThenElse;
import static wbs.utils.etc.NullUtils.ifNull;
import static wbs.utils.etc.TypeUtils.genericCastUnchecked;
import static wbs.utils.string.StringUtils.capitalise;
import static wbs.utils.string.StringUtils.stringFormat;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Provider;

import lombok.NonNull;

import wbs.console.annotations.ConsoleModuleBuilderHandler;
import wbs.console.context.ConsoleContextBuilderContainer;
import wbs.console.context.ResolvedConsoleContextExtensionPoint;
import wbs.console.forms.CodeFormFieldSpec;
import wbs.console.forms.DescriptionFormFieldSpec;
import wbs.console.forms.FieldsProvider;
import wbs.console.forms.FormFieldSet;
import wbs.console.forms.NameFormFieldSpec;
import wbs.console.forms.ParentFormFieldSpec;
import wbs.console.helper.core.ConsoleHelper;
import wbs.console.module.ConsoleMetaManager;
import wbs.console.module.ConsoleModuleBuilder;
import wbs.console.module.ConsoleModuleImplementation;
import wbs.console.part.PagePart;
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
import wbs.framework.entity.record.Record;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.TaskLogger;

import wbs.web.action.Action;
import wbs.web.responder.Responder;

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
	FieldsProvider <ObjectType, ParentType> fieldsProvider;
	FormFieldSet <ObjectType> formFieldSet;
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
			@NonNull Builder builder) {

		TaskLogger taskLogger =
			logContext.nestTaskLogger (
				parentTaskLogger,
				"build");

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

		Action createAction =
			new Action () {

			@Override
			public
			Responder handle (
					@NonNull TaskLogger taskLogger) {

				return objectCreateActionProvider.get ()

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

					.formFieldSet (
						formFieldSet)

					.formFieldsProvider (
						fieldsProvider)

					.createTimeFieldName (
						createTimeFieldName)

					.createUserFieldName (
						createUserFieldName)

					.handle (
						taskLogger);

			}

		};

		consoleModule.addContextFile (

			localFile,

			consoleFileProvider.get ()

				.getResponderName (
					responderName)

				.postAction (
					createAction)

				/*.privKeys (
					Collections.singletonList (privKey)*/,

			resolvedExtensionPoint.contextTypeNames ());

	}

	void buildResponder () {

		PagePartFactory partFactory =
			new PagePartFactory () {

			@Override
			public
			PagePart buildPagePart (
					@NonNull TaskLogger parentTaskLogger) {

				return objectCreatePartProvider.get ()

					.consoleHelper (
						consoleHelper)

					.formFieldSet (
						formFieldSet)

					.formFieldsProvider (
						fieldsProvider)

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
			@NonNull TaskLogger taskLogger) {

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

		formFieldSet =
			ifNotNullThenElse (
				spec.fieldsName (),
				() -> consoleModule.formFieldSet (
					spec.fieldsName (),
					consoleHelper.objectClass ()),
				() -> defaultFields (
					taskLogger));

		// if a provider name is provided

		if (spec.fieldsProviderName () != null) {

			fieldsProvider =
				genericCastUnchecked (
					componentManager.getComponentRequired (
						taskLogger,
						spec.fieldsProviderName (),
						FieldsProvider.class));

		}

		else {

			fieldsProvider =
				null;

		}

		createTimeFieldName =
			spec.createTimeFieldName ();

		createUserFieldName =
			spec.createUserFieldName ();

		privKey =
			spec.privKey ();

	}

	FormFieldSet <ObjectType> defaultFields (
			@NonNull TaskLogger parentTaskLogger) {

		TaskLogger taskLogger =
			logContext.nestTaskLogger (
				parentTaskLogger,
				"defaultFields");

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
