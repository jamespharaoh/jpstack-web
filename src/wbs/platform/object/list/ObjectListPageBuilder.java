package wbs.platform.object.list;

import static wbs.framework.utils.etc.StringUtils.capitalise;
import static wbs.framework.utils.etc.NullUtils.ifNull;
import static wbs.framework.utils.etc.StringUtils.stringFormat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Provider;

import lombok.NonNull;

import com.google.common.collect.ImmutableMap;

import wbs.console.annotations.ConsoleModuleBuilderHandler;
import wbs.console.context.ConsoleContextBuilderContainer;
import wbs.console.context.ResolvedConsoleContextExtensionPoint;
import wbs.console.forms.CodeFormFieldSpec;
import wbs.console.forms.DescriptionFormFieldSpec;
import wbs.console.forms.FieldsProvider;
import wbs.console.forms.FormFieldSet;
import wbs.console.forms.NameFormFieldSpec;
import wbs.console.forms.StaticFieldsProvider;
import wbs.console.helper.ConsoleHelper;
import wbs.console.module.ConsoleMetaManager;
import wbs.console.module.ConsoleModuleBuilder;
import wbs.console.module.ConsoleModuleImplementation;
import wbs.console.part.PagePart;
import wbs.console.responder.ConsoleFile;
import wbs.console.tab.ConsoleContextTab;
import wbs.console.tab.TabContextResponder;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.application.context.ApplicationContext;
import wbs.framework.builder.Builder;
import wbs.framework.builder.annotations.BuildMethod;
import wbs.framework.builder.annotations.BuilderParent;
import wbs.framework.builder.annotations.BuilderSource;
import wbs.framework.builder.annotations.BuilderTarget;
import wbs.framework.record.Record;
import wbs.platform.object.criteria.WhereDeletedCriteriaSpec;
import wbs.platform.object.criteria.WhereICanManageCriteriaSpec;
import wbs.platform.object.criteria.WhereNotDeletedCriteriaSpec;
import wbs.platform.scaffold.model.SliceRec;

@PrototypeComponent ("objectListPageBuilder")
@ConsoleModuleBuilderHandler
public
class ObjectListPageBuilder<
	ObjectType extends Record<ObjectType>,
	ParentType extends Record<ParentType>
> {

	// dependencies

	@Inject
	ApplicationContext applicationContext;

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
	Provider<ObjectListPart<ObjectType,ParentType>> objectListPart;

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
	ConsoleContextBuilderContainer<ObjectType> container;

	@BuilderSource
	ObjectListPageSpec spec;

	@BuilderTarget
	ConsoleModuleImplementation consoleModule;

	// state

	ConsoleHelper<ObjectType> consoleHelper;

	String typeCode;

	FieldsProvider<ObjectType,ParentType> fieldsProvider;

	FormFieldSet formFieldSet;

	Map<String,ObjectListBrowserSpec> listBrowsersByFieldName;

	Map<String,ObjectListTabSpec> listTabsByName;

	// build

	@BuildMethod
	public
	void build (
			@NonNull Builder builder) {

		setDefaults ();

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

					.formFieldsProvider (
						fieldsProvider)

					.listBrowserSpecs (
						listBrowsersByFieldName)

					.targetContextTypeName (
						ifNull (
							spec.targetContextTypeName (),
							consoleHelper.objectName () + ":combo"));
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

		// if a provider name is provided

		if (spec.fieldsProviderName () != null) {

			@SuppressWarnings ("unchecked")
			FieldsProvider<ObjectType,ParentType> fieldsProviderTemp =
				(FieldsProvider<ObjectType,ParentType>)
				applicationContext.getComponentRequired (
					spec.fieldsProviderName (),
					FieldsProvider.class);

			fieldsProvider =
				fieldsProviderTemp;

		// if a field name is provided

		} else if (spec.fieldsName () != null) {

			fieldsProvider =
				new StaticFieldsProvider<ObjectType,ParentType> ()

				.setFields (
					consoleModule.formFieldSets ().get (
						spec.fieldsName ()));

		// if nothing is provided

		} else {

			fieldsProvider =
				new StaticFieldsProvider<ObjectType,ParentType> ()

				.setFields (
					defaultFields ());

		}


		listBrowsersByFieldName =
			ifNull (
				spec.listBrowsersByFieldName (),
				Collections.<String,ObjectListBrowserSpec>emptyMap ());

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
