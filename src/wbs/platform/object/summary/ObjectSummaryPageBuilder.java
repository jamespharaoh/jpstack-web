package wbs.platform.object.summary;

import static wbs.utils.etc.LogicUtils.ifNotNullThenElse;
import static wbs.utils.etc.OptionalUtils.optionalAbsent;
import static wbs.utils.etc.OptionalUtils.optionalOf;
import static wbs.utils.string.StringUtils.capitalise;
import static wbs.utils.string.StringUtils.stringFormat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.inject.Provider;

import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.Accessors;

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
import wbs.console.forms.object.ParentFormFieldSpec;
import wbs.console.forms.types.FormType;
import wbs.console.helper.core.ConsoleHelper;
import wbs.console.module.ConsoleMetaManager;
import wbs.console.module.ConsoleMetaModuleImplementation;
import wbs.console.module.ConsoleModuleBuilderComponent;
import wbs.console.module.ConsoleModuleImplementation;
import wbs.console.part.PagePart;
import wbs.console.part.PagePartFactory;
import wbs.console.part.TextPart;
import wbs.console.responder.ConsoleFile;
import wbs.console.tab.ConsoleContextTab;
import wbs.console.tab.TabContextResponder;

import wbs.framework.builder.Builder;
import wbs.framework.builder.Builder.MissingBuilderBehaviour;
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

@Accessors (fluent = true)
@PrototypeComponent ("objectSummaryPageBuilder")
public
class ObjectSummaryPageBuilder <
	ObjectType extends Record <ObjectType>,
	ParentType extends Record <ParentType>
>
	implements ConsoleModuleBuilderComponent {

	// singleton dependencies

	@SingletonDependency
	ComponentManager componentManager;

	@SingletonDependency
	ConsoleMetaManager consoleMetaManager;

	@SingletonDependency
	ConsoleFormBuilder consoleFormBuilder;

	@SingletonDependency
	ConsoleFormManager consoleFormManager;

	@SingletonDependency
	ConsoleFormManager formContextManager;

	@ClassSingletonDependency
	LogContext logContext;

	// prototype dependencies

	@PrototypeDependency
	Provider <ConsoleFile> consoleFileProvider;

	@PrototypeDependency
	Provider <ConsoleContextTab> contextTabProvider;

	@PrototypeDependency
	Provider <ObjectSummaryPart> objectSummaryPartProvider;

	@PrototypeDependency
	Provider <ParentFormFieldSpec> parentFieldProvider;

	@PrototypeDependency
	Provider <ObjectSummaryFieldsPart <ObjectType, ParentType>>
	summaryFieldsPartProvider;

	@PrototypeDependency
	Provider <TabContextResponder> tabContextResponder;

	@PrototypeDependency
	Provider <TextPart> textPart;

	// builder

	@BuilderParent
	ConsoleContextBuilderContainer <ObjectType> container;

	@BuilderSource
	ObjectSummaryPageSpec spec;

	@BuilderTarget
	ConsoleModuleImplementation consoleModule;

	// state

	@Getter
	ConsoleHelper <ObjectType> consoleHelper;

	ConsoleFormType <ObjectType> formType;

	String privKey;

	List <PagePartFactory> pagePartFactories =
		new ArrayList<> ();

	// build meta

	public
	void buildMeta (
			@NonNull ConsoleMetaModuleImplementation consoleMetaModule) {

	}

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

			buildResponder ();

			for (
				ResolvedConsoleContextExtensionPoint resolvedExtensionPoint
					: consoleMetaManager.resolveExtensionPoint (
						container.extensionPointName ())
			) {

				buildContextTabs (
					taskLogger,
					resolvedExtensionPoint);

				buildContextFile (
					resolvedExtensionPoint);

			}

			builder.descend (
				taskLogger,
				spec,
				spec.builders (),
				this,
				MissingBuilderBehaviour.error);

		}

	}

	void buildContextTabs (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull ResolvedConsoleContextExtensionPoint extensionPoint) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"buildContextTabs");

		) {

			consoleModule.addContextTab (
				taskLogger,
				"end",

				contextTabProvider.get ()

					.name (
						stringFormat (
							"%s.summary",
							container.pathPrefix ()))

					.defaultLabel (
						"Summary")

					.localFile (
						stringFormat (
							"%s.summary",
							container.pathPrefix ()))

					.privKeys (
						privKey),

				extensionPoint.contextTypeNames ());

		}

	}

	void buildContextFile (
			@NonNull ResolvedConsoleContextExtensionPoint extensionPoint) {

		consoleModule.addContextFile (

			stringFormat (
				"%s.summary",
				container.pathPrefix ()),

			consoleFileProvider.get ()

				.getResponderName (
					stringFormat (
						"%sSummaryResponder",
						container.newBeanNamePrefix ()))

				.privKeys (
					privKey != null
						? Collections.singletonList (privKey)
						: Collections.<String>emptyList ()),

			extensionPoint.contextTypeNames ());

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

				return objectSummaryPartProvider.get ().partFactories (
					pagePartFactories);

			}

		};

		consoleModule.addResponder (

			stringFormat (
				"%sSummaryResponder",
				container.newBeanNamePrefix ()),

			tabContextResponder.get ()

				.tab (
					stringFormat (
						"%s.summary",
						container.pathPrefix ()))

				.title (
					capitalise (
						stringFormat (
							"%s summary",
							consoleHelper.friendlyName ())))

				.pagePartFactory (
					partFactory));

	}

	public
	ObjectSummaryPageBuilder <ObjectType, ParentType> addFieldsPart (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull String fieldsName) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"addFieldsPart");
		) {

			return addFieldsPart (
				taskLogger,
				consoleFormManager.createFormType (
					taskLogger,
					consoleModule,
					fieldsName,
					consoleHelper.objectClass (),
					FormType.readOnly,
					optionalOf (
						fieldsName),
					optionalAbsent ()));

		}

	}

	public
	ObjectSummaryPageBuilder <ObjectType, ParentType> addFieldsPart (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull ConsoleFormType <ObjectType> formType) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"addFieldsPart");

		) {

			PagePartFactory partFactory =
				parentTransaction -> {

				try (

					NestedTransaction nestedTansaction =
						parentTransaction.nestTransaction (
							logContext,
							"addFieldsPart.PagePartFactory");

				) {

					return summaryFieldsPartProvider.get ()

						.consoleHelper (
							consoleHelper)

						.formContextBuilder (
							formType)

					;

				}

			};

			pagePartFactories.add (
				partFactory);

			return this;

		}

	}

	public
	ObjectSummaryPageBuilder <ObjectType, ParentType> addHeading (
			@NonNull String heading) {

		String html =
			stringFormat (
				"<h2>%h</h2>\n",
				heading);

		PagePartFactory pagePartFactory =
			parentTransaction -> {

			try (

				NestedTransaction transaction =
					parentTransaction.nestTransaction (
						logContext,
						"addHeading");

			) {

				return textPart.get ()

					.text (
						html);

			}

		};

		pagePartFactories.add (
			pagePartFactory);

		return this;

	}

	public
	ObjectSummaryPageBuilder<ObjectType,ParentType> addPart (
			@NonNull String beanName) {

		PagePartFactory partFactory =
			parentTransaction -> {

			try (

				NestedTransaction transaction =
					parentTransaction.nestTransaction (
						logContext,
						"addPart");

			) {

				Object object =
					componentManager.getComponentRequired (
						transaction,
						beanName,
						Object.class);

				if (object instanceof PagePart)
					return (PagePart) object;

				if (object instanceof Provider) {

					Provider<?> provider =
						(Provider<?>) object;

					return (PagePart)
						provider.get ();

				}

				throw new ClassCastException (
					object.getClass ().getName ());

			}

		};

		pagePartFactories.add (
			partFactory);

		return this;

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

			formType =
				ifNotNullThenElse (
					spec.formFieldsName,
					() -> formContextManager.createFormType (
						taskLogger,
						consoleModule,
						"summary",
						consoleHelper.objectClass (),
						FormType.readOnly,
						optionalOf (
							spec.formFieldsName ()),
						optionalAbsent ()),
					() -> formContextManager.createFormType (
						taskLogger,
						"summary",
						consoleHelper.objectClass (),
						FormType.readOnly,
						optionalOf (
							defaultFields (
								taskLogger)),
						optionalAbsent ()));

			privKey =
				spec.privKey ();

			if (spec.builders ().isEmpty ()) {

				addFieldsPart (
					taskLogger,
					formType);

			}

			/*
			// if a provider name is provided

			if (spec.fieldsProviderName () != null) {

				fieldsProvider =
					genericCastUnchecked (
						componentManager.getComponentRequired (
							taskLogger,
							spec.fieldsProviderName (),
							FieldsProvider.class));

			} else {

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

			if (consoleHelper.parentTypeIsFixed ()) {

				// TODO this should not be disabled!

				/*
				ConsoleObjectHelper parentHelper =
					objectManager.getConsoleObjectHelper (
						maintSchedConsoleHelper.parentClass ());

				if (! parentHelper.isRoot ())
					fields.add (parentField.get ());
				*/

			} else {

				formFieldSpecs.add (
					new ParentFormFieldSpec ());

			}

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
					"%s.summary",
					consoleHelper.objectName ());

			return consoleFormBuilder.buildFormFieldSet (
				taskLogger,
				consoleHelper,
				fieldSetName,
				formFieldSpecs);

		}

	}

}
