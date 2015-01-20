package wbs.platform.object.settings;

import static wbs.framework.utils.etc.Misc.camelToSpaces;
import static wbs.framework.utils.etc.Misc.capitalise;
import static wbs.framework.utils.etc.Misc.ifNull;
import static wbs.framework.utils.etc.Misc.stringFormat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.servlet.ServletException;

import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.builder.Builder;
import wbs.framework.builder.annotations.BuildMethod;
import wbs.framework.builder.annotations.BuilderParent;
import wbs.framework.builder.annotations.BuilderSource;
import wbs.framework.builder.annotations.BuilderTarget;
import wbs.framework.web.Action;
import wbs.framework.web.Responder;
import wbs.platform.console.annotations.ConsoleModuleBuilderHandler;
import wbs.platform.console.context.ConsoleContextBuilderContainer;
import wbs.platform.console.context.ResolvedConsoleContextExtensionPoint;
import wbs.platform.console.forms.CodeFormFieldSpec;
import wbs.platform.console.forms.DescriptionFormFieldSpec;
import wbs.platform.console.forms.FormFieldSet;
import wbs.platform.console.forms.IdFormFieldSpec;
import wbs.platform.console.forms.NameFormFieldSpec;
import wbs.platform.console.helper.ConsoleHelper;
import wbs.platform.console.helper.ConsoleHelperRegistry;
import wbs.platform.console.module.ConsoleManager;
import wbs.platform.console.module.ConsoleMetaManager;
import wbs.platform.console.module.ConsoleModuleBuilder;
import wbs.platform.console.module.ConsoleModuleImpl;
import wbs.platform.console.part.PagePart;
import wbs.platform.console.responder.ConsoleFile;
import wbs.platform.console.tab.ConsoleContextTab;
import wbs.platform.console.tab.TabContextResponder;

@PrototypeComponent ("objectSettingsPageBuilder")
@ConsoleModuleBuilderHandler
public
class ObjectSettingsPageBuilder {

	// dependencies

	@Inject
	ConsoleModuleBuilder consoleModuleBuilder;

	@Inject
	ConsoleMetaManager consoleMetaManager;

	// indirect dependencies

	@Inject
	Provider<ConsoleHelperRegistry> consoleHelperRegistry;

	@Inject
	Provider<ConsoleManager> consoleManager;

	// prototype dependencies

	@Inject
	Provider<ConsoleFile> consoleFile;

	@Inject
	Provider<ConsoleContextTab> contextTab;

	@Inject
	Provider<ObjectRemoveAction> objectRemoveAction;

	@Inject
	Provider<ObjectSettingsAction> objectSettingsAction;

	@Inject
	Provider<ObjectSettingsPart> objectSettingsPart;

	@Inject
	Provider<TabContextResponder> tabContextResponder;

	// builder

	@BuilderParent
	ConsoleContextBuilderContainer container;

	@BuilderSource
	ObjectSettingsPageSpec spec;

	@BuilderTarget
	ConsoleModuleImpl consoleModule;

	// state

	ConsoleHelper<?> consoleHelper;
	FormFieldSet formFieldSet;
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
			Builder builder) {

		setDefaults ();

		buildAction ();
		buildResponder ();

		for (ResolvedConsoleContextExtensionPoint resolvedExtensionPoint
				: consoleMetaManager.resolveExtensionPoint (
					container.extensionPointName ())) {

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
			Responder go ()
				throws ServletException {

				Action settingsAction;

				if (consoleHelper.ephemeral ()) {

					settingsAction =
						objectSettingsAction.get ()

						.detailsResponder (
							consoleManager.get ().responder (
								responderName,
								true))

						.accessDeniedResponder (
							consoleManager.get ().responder (
								responderName,
								true))

						.editPrivKey (
							privKey)

						.objectLookup (
							consoleHelper)

						.formFieldSet (
							formFieldSet)

						.objectRefName (
							consoleHelper.codeExists ()
								? consoleHelper.codeFieldName ()
								: "id")

						.objectType (
							consoleHelper.objectTypeCode ());

				} else {

					settingsAction =
						objectSettingsAction.get ()

						.detailsResponder (
							consoleManager.get ().responder (
								responderName,
								true))

						.accessDeniedResponder (
							consoleManager.get ().responder (
								responderName,
								true))

						.editPrivKey (
							privKey)

						.objectLookup (
							consoleHelper)

						.formFieldSet (
							formFieldSet);

				}

				return settingsAction.go ();

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
				Responder go ()
					throws ServletException {

					Action removeAction;

					removeAction =
						objectRemoveAction.get ()

						.objectHelper (
							consoleHelper)

						.settingsResponder (
							consoleManager.get ().responder (
								responderName,
								true))

						.listResponder (
							consoleManager.get ().responder (
								stringFormat (
									"%sListResponder",
									container.newBeanNamePrefix ()),
								true))

						.nextContextTypeName (
							ifNull (
								spec.listContextTypeName (),
								consoleHelper.objectName () + "s"))

						.editPrivKey (
							privKey);

					return removeAction.go ();

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

				return objectSettingsPart.get ()

					.objectLookup (
						consoleHelper)

					.editPrivKey (
						privKey)

					.localName (
						"/" + fileName)

					.formFieldSet (
						formFieldSet)

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

		consoleHelper =
			spec.objectName () != null
				? consoleHelperRegistry.get ()
					.findByObjectName (spec.objectName ())
				: container.consoleHelper ();

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
				camelToSpaces (shortName));

		friendlyLongName =
			ifNull (
				spec.friendlyLongName (),
				stringFormat (
					"%s %s",
					consoleHelper.friendlyName (),
					camelToSpaces (longName)));

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

		if (consoleHelper.nameExists ()
				&& ! consoleHelper.nameIsCode ()) {

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
