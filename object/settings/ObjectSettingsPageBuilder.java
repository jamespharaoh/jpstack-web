package wbs.platform.object.settings;

import static wbs.utils.etc.NullUtils.ifNull;
import static wbs.utils.string.StringUtils.camelToSpaces;
import static wbs.utils.string.StringUtils.capitalise;
import static wbs.utils.string.StringUtils.stringFormat;

import java.util.ArrayList;
import java.util.Collections;
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
import wbs.console.forms.IdFormFieldSpec;
import wbs.console.forms.NameFormFieldSpec;
import wbs.console.helper.ConsoleHelper;
import wbs.console.helper.ConsoleHelperRegistry;
import wbs.console.module.ConsoleManager;
import wbs.console.module.ConsoleMetaManager;
import wbs.console.module.ConsoleModuleBuilder;
import wbs.console.module.ConsoleModuleImplementation;
import wbs.console.part.PagePart;
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
import wbs.framework.component.annotations.WeakSingletonDependency;
import wbs.framework.component.manager.ComponentManager;
import wbs.framework.entity.record.Record;
import wbs.framework.web.Action;
import wbs.framework.web.Responder;

@PrototypeComponent ("objectSettingsPageBuilder")
@ConsoleModuleBuilderHandler
public
class ObjectSettingsPageBuilder <
	ObjectType extends Record <ObjectType>,
	ParentType extends Record <ParentType>
> {

	// singleton dependencies

	@SingletonDependency
	ComponentManager componentManager;

	@WeakSingletonDependency
	ConsoleHelperRegistry consoleHelperRegistry;

	@SingletonDependency
	ConsoleModuleBuilder consoleModuleBuilder;

	@WeakSingletonDependency
	ConsoleManager consoleManager;

	@SingletonDependency
	ConsoleMetaManager consoleMetaManager;

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

	ConsoleHelper<ObjectType> consoleHelper;
	FormFieldSet formFieldSet;
	FieldsProvider<ObjectType,ParentType> fieldsProvider;

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

	Action settingsAction;

	// build

	@BuildMethod
	public
	void build (
			@NonNull Builder builder) {

		setDefaults ();

		buildAction ();
		buildResponder ();

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

	}

	void buildTab (
			ResolvedConsoleContextExtensionPoint resolvedExtensionPoint) {

		consoleModule.addContextTab (
			"end",
			contextTab.get ()

				.name (
					tabName)

				.defaultLabel (
					capitalise (friendlyShortName))

				.localFile (
					fileName)

				.privKeys (
					Collections.<String>singletonList (privKey)),

			resolvedExtensionPoint.contextTypeNames ());

	}

	void buildAction () {

		settingsAction =
			new Action () {

			@Override
			public
			Responder handle () {

				Action settingsAction;

				if (consoleHelper.ephemeral ()) {

					settingsAction =
						objectSettingsActionProvider.get ()

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

						.formFieldSet (
							formFieldSet)

						.formFieldsProvider (
							fieldsProvider)

						.objectRefName (
							consoleHelper.codeExists ()
								? consoleHelper.codeFieldName ()
								: "id")

						.objectType (
							consoleHelper.objectTypeCode ());

				} else {

					settingsAction =
						objectSettingsActionProvider.get ()

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

						.formFieldSet (
							formFieldSet)

						.formFieldsProvider (
								fieldsProvider);

				}

				return settingsAction.handle ();

			}

		};

	}

	void buildFile (
			ResolvedConsoleContextExtensionPoint resolvedExtensionPoint) {

		consoleModule.addContextFile (

			fileName,

			consoleFile.get ()

				.getResponderName (
					responderName)

				.postAction (
					settingsAction)

				.privName (
					privKey),

			resolvedExtensionPoint.contextTypeNames ());

		if (consoleHelper.ephemeral ()) {

			Action removeAction =
				new Action () {

				@Override
				public
				Responder handle () {

					Action removeAction;

					removeAction =
						objectRemoveAction.get ()

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

					return removeAction.handle ();

				}

			};

			consoleModule.addContextFile (

				stringFormat (
					"%s.remove",
					container.structuralName ()),

				consoleFile.get ()

					.postAction (
						removeAction)

					.privName (
						privKey),

				resolvedExtensionPoint.contextTypeNames ());

		}

	}

	void buildResponder () {

		Provider<PagePart> partFactory =
			new Provider<PagePart> () {

			@Override
			public
			PagePart get () {

				return objectSettingsPartProvider.get ()

					.objectLookup (
						consoleHelper)

					.consoleHelper (
						consoleHelper)

					.editPrivKey (
						privKey)

					.localName (
						"/" + fileName)

					.formFieldSet (
						formFieldSet)

					.formFieldsProvider (
						fieldsProvider)

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

	void setDefaults () {

		@SuppressWarnings ("unchecked")
		ConsoleHelper<ObjectType> consoleHelperTemp =
			spec.objectName () != null
				? (ConsoleHelper<ObjectType>)
					consoleHelperRegistry.findByObjectName (
						spec.objectName ())
				: container.consoleHelper ();

		consoleHelper =
			consoleHelperTemp;

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

		formFieldSet =
			spec.fieldsName () != null
				? consoleModule.formFieldSets ().get (
					spec.fieldsName ())
				: defaultFields ();

		// if a provider name is provided

		if (spec.fieldsProviderName () != null) {

			@SuppressWarnings ("unchecked")
			FieldsProvider<ObjectType,ParentType> fieldsProviderTemp =
				(FieldsProvider<ObjectType,ParentType>)
				componentManager.getComponentRequired (
					spec.fieldsProviderName (),
					FieldsProvider.class);

			fieldsProvider =
				fieldsProviderTemp;

		}

		else {

			fieldsProvider =
				null;

		}

	}

	FormFieldSet defaultFields () {

		List<Object> formFieldSpecs =
			new ArrayList<Object> ();

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

		return consoleModuleBuilder.buildFormFieldSet (
			consoleHelper,
			fieldSetName,
			formFieldSpecs);

	}

}
