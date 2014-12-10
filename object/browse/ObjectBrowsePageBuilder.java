package wbs.platform.object.browse;

import static wbs.framework.utils.etc.Misc.capitalise;
import static wbs.framework.utils.etc.Misc.ifNull;
import static wbs.framework.utils.etc.Misc.stringFormat;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Provider;

import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.builder.Builder;
import wbs.framework.builder.annotations.BuildMethod;
import wbs.framework.builder.annotations.BuilderParent;
import wbs.framework.builder.annotations.BuilderSource;
import wbs.framework.builder.annotations.BuilderTarget;
import wbs.platform.console.annotations.ConsoleModuleBuilderHandler;
import wbs.platform.console.context.ConsoleContextBuilderContainer;
import wbs.platform.console.context.ResolvedConsoleContextExtensionPoint;
import wbs.platform.console.forms.CodeFormFieldSpec;
import wbs.platform.console.forms.DescriptionFormFieldSpec;
import wbs.platform.console.forms.FormFieldSet;
import wbs.platform.console.forms.NameFormFieldSpec;
import wbs.platform.console.helper.ConsoleHelper;
import wbs.platform.console.metamodule.ConsoleMetaManager;
import wbs.platform.console.module.ConsoleModuleBuilder;
import wbs.platform.console.module.ConsoleModuleImpl;
import wbs.platform.console.part.PagePart;
import wbs.platform.console.responder.ConsoleFile;
import wbs.platform.console.tab.ConsoleContextTab;
import wbs.platform.console.tab.TabContextResponder;
import wbs.platform.scaffold.model.SliceRec;

@PrototypeComponent ("objectBrowsePageBuilder")
@ConsoleModuleBuilderHandler
public
class ObjectBrowsePageBuilder {

	// dependencies

	@Inject
	ConsoleModuleBuilder consoleModuleBuilder;

	@Inject
	ConsoleMetaManager consoleMetaManager;

	// prototype dependencies

	@Inject
	Provider<ConsoleFile> consoleFile;

	@Inject
	Provider<ConsoleContextTab> contextTab;

	@Inject
	Provider<ObjectBrowsePart> objectBrowsePart;

	@Inject
	Provider<TabContextResponder> tabContextResponder;

	// builder

	@BuilderParent
	ConsoleContextBuilderContainer container;

	@BuilderSource
	ObjectBrowsePageSpec spec;

	@BuilderTarget
	ConsoleModuleImpl consoleModule;

	// state

	ConsoleHelper<?> consoleHelper;

	String typeCode;

	FormFieldSet formFieldSet;

	// build

	@BuildMethod
	public
	void build (
			Builder builder) {

		setDefaults ();

		for (ResolvedConsoleContextExtensionPoint resolvedExtensionPoint
				: consoleMetaManager.resolveExtensionPoint (
					container.extensionPointName ())) {

			buildContextTab (
				resolvedExtensionPoint);

			buildContextFile (
				resolvedExtensionPoint);

		}

		buildResponder ();

	}

	void buildContextTab (
			ResolvedConsoleContextExtensionPoint resolvedExtensionPoint) {

		consoleModule.addContextTab (

			container.tabLocation (),

			contextTab.get ()

				.name (
					container.pathPrefix () + ".browse")

				.defaultLabel (
					"Browse")

				.localFile (
					container.pathPrefix () + ".browse"),

			resolvedExtensionPoint.contextTypeNames ());

	}

	void buildContextFile (
			ResolvedConsoleContextExtensionPoint resolvedExtensionPoint) {

		consoleModule.addContextFile (

			container.pathPrefix () + ".browse",

			consoleFile.get ()

				.getResponderName (
					stringFormat (
						"%sBrowseResponder",
						container.newBeanNamePrefix ())),

			resolvedExtensionPoint.contextTypeNames ());

	}

	void buildResponder () {

		Provider<PagePart> partFactory =
			new Provider<PagePart> () {

			@Override
			public
			PagePart get () {

				return objectBrowsePart.get ()

					.consoleHelper (
						consoleHelper)

					.typeCode (
						typeCode)

					.localName (
						container.pathPrefix () + ".browse")

					.formFieldSet (
						formFieldSet)

					.targetContextTypeName (
						ifNull (
							spec.targetContextTypeName (),
							consoleHelper.objectName () + "+"));

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

	void setDefaults () {

		consoleHelper =
			container.consoleHelper ();

		typeCode =
			spec.typeCode ();

		formFieldSet =
			spec.fieldsName () != null
				? consoleModule.formFieldSets ().get (
					spec.fieldsName ())
				: defaultFields ();

	}

	FormFieldSet defaultFields () {

		// create spec

		List<Object> formFieldSpecs =
			new ArrayList<Object> ();

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
			consoleHelper,
			fieldSetName,
			formFieldSpecs);

	}

}
