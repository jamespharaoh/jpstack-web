package wbs.platform.object.list;

import static wbs.framework.utils.etc.Misc.capitalise;
import static wbs.framework.utils.etc.Misc.ifNull;
import static wbs.framework.utils.etc.Misc.stringFormat;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
import wbs.platform.console.forms.DelegateFormFieldSpec;
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
import wbs.platform.object.criteria.WhereDeletedCriteriaSpec;
import wbs.platform.object.criteria.WhereICanManageCriteriaSpec;
import wbs.platform.object.criteria.WhereNotDeletedCriteriaSpec;
import wbs.platform.scaffold.model.SliceRec;

import com.google.common.collect.ImmutableMap;

@PrototypeComponent ("objectListPageBuilder")
@ConsoleModuleBuilderHandler
public
class ObjectListPageBuilder {

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
	Provider<ObjectListTabSpec> listTabSpec;

	@Inject
	Provider<ObjectListPart> objectListPart;

	@Inject
	Provider<TabContextResponder> tabContextResponder;

	@Inject
	Provider<WhereDeletedCriteriaSpec> whereDeletedCriteriaSpec;

	@Inject
	Provider<WhereICanManageCriteriaSpec> whereICanManageCriteriaSpec;

	@Inject
	Provider<WhereNotDeletedCriteriaSpec> whereNotDeletedCriteriaSpec;

	// builder

	@BuilderParent
	ConsoleContextBuilderContainer container;

	@BuilderSource
	ObjectListPageSpec spec;

	@BuilderTarget
	ConsoleModuleImpl consoleModule;

	// state

	ConsoleHelper<?> consoleHelper;

	String typeCode;

	FormFieldSet formFieldSet;
	Map<String,ObjectListTabSpec> listTabsByName;

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
					container.pathPrefix () + ".list")

				.defaultLabel (
					"List")

				.localFile (
					container.pathPrefix () + ".list"),

			resolvedExtensionPoint.contextTypeNames ());

	}

	void buildContextFile (
			ResolvedConsoleContextExtensionPoint resolvedExtensionPoint) {

		consoleModule.addContextFile (

			container.pathPrefix () + ".list",

			consoleFile.get ()

				.getResponderName (
					stringFormat (
						"%sListResponder",
						container.newBeanNamePrefix ())),

			resolvedExtensionPoint.contextTypeNames ());

	}

	void buildResponder () {

		Provider<PagePart> partFactory =
			new Provider<PagePart> () {

			@Override
			public
			PagePart get () {

				return objectListPart.get ()

					.consoleHelper (
						consoleHelper)

					.typeCode (
						typeCode)

					.localName (
						container.pathPrefix () + ".list")

					.listTabSpecs (
						listTabsByName)

					.formFieldSet (
						formFieldSet)

					.targetContextTypeName (
						ifNull (
							spec.targetContextTypeName (),
							consoleHelper.objectName () + "+"));

			}

		};

		consoleModule.addResponder (
			container.newBeanNamePrefix () + "ListResponder",
			tabContextResponder.get ()

				.tab (
					container.pathPrefix () + ".list")

				.title (
					capitalise (consoleHelper.friendlyName () + " list"))

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
				? consoleModuleBuilder.buildFormFieldSet (
					spec.consoleSpec (),
					spec.fieldsName ())
				: defaultFields ();

		listTabsByName =
			spec.listTabsByName () != null
			&& ! spec.listTabsByName ().isEmpty ()
				? spec.listTabsByName ()
				: defaultListTabSpecs ();

	}

	FormFieldSet defaultFields () {

		// create spec

		List<Object> formFieldSpecs =
			new ArrayList<Object> ();

		if (consoleHelper.parentTypeIsFixed ()
				&& consoleHelper.parentClass () == SliceRec.class) {

			formFieldSpecs.add (
				new DelegateFormFieldSpec ()
					.delegate ("slice")
					.name ("description")
					.label ("Slice"));

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
				"%s.list",
				consoleHelper.objectName ());

		return consoleModuleBuilder.buildFormFieldSet (
			consoleHelper,
			fieldSetName,
			formFieldSpecs);

	}

	Map<String,ObjectListTabSpec> defaultListTabSpecs () {

		if (consoleHelper.deletedExists ()) {

			return ImmutableMap.<String,ObjectListTabSpec>builder ()

				.put (
					"all",
					listTabSpec.get ()

						.name (
							"all")

						.label (
							stringFormat (
								"All %s",
								consoleHelper.shortNamePlural ()))

						.addCriteria (
							whereNotDeletedCriteriaSpec.get ()))

				.put (
					"deleted",
					listTabSpec.get ()

						.name (
							"deleted")

						.label (
							"Deleted")

						.addCriteria (
							whereDeletedCriteriaSpec.get ())

						.addCriteria (
							whereICanManageCriteriaSpec.get ()))

				.build ();

		} else {

			return ImmutableMap.<String,ObjectListTabSpec>builder ()

				.put (
					"all",
					listTabSpec.get ()

						.name (
							"all")

						.label (
							stringFormat (
								"All %s",
								consoleHelper.shortNamePlural ())))

				.build ();

		}

	}

}
