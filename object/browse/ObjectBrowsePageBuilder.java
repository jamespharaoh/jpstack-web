package wbs.platform.object.browse;

import static wbs.framework.utils.etc.StringUtils.capitalise;
import static wbs.framework.utils.etc.Misc.ifNull;
import static wbs.framework.utils.etc.StringUtils.stringFormat;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Provider;

import wbs.console.annotations.ConsoleModuleBuilderHandler;
import wbs.console.context.ConsoleContextBuilderContainer;
import wbs.console.context.ResolvedConsoleContextExtensionPoint;
import wbs.console.forms.CodeFormFieldSpec;
import wbs.console.forms.DescriptionFormFieldSpec;
import wbs.console.forms.FormFieldSet;
import wbs.console.forms.NameFormFieldSpec;
import wbs.console.helper.ConsoleHelper;
import wbs.console.module.ConsoleMetaManager;
import wbs.console.module.ConsoleModuleBuilder;
import wbs.console.module.ConsoleModuleImplementation;
import wbs.console.part.PagePart;
import wbs.console.responder.ConsoleFile;
import wbs.console.tab.ConsoleContextTab;
import wbs.console.tab.TabContextResponder;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.builder.Builder;
import wbs.framework.builder.annotations.BuildMethod;
import wbs.framework.builder.annotations.BuilderParent;
import wbs.framework.builder.annotations.BuilderSource;
import wbs.framework.builder.annotations.BuilderTarget;
import wbs.framework.record.Record;
import wbs.platform.scaffold.model.SliceRec;

@PrototypeComponent ("objectBrowsePageBuilder")
@ConsoleModuleBuilderHandler
public
class ObjectBrowsePageBuilder<
	ObjectType extends Record<ObjectType>
> {

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
	ConsoleContextBuilderContainer<ObjectType> container;

	@BuilderSource
	ObjectBrowsePageSpec spec;

	@BuilderTarget
	ConsoleModuleImplementation consoleModule;

	// state

	ConsoleHelper<ObjectType> consoleHelper;

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
