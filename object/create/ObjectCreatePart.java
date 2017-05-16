package wbs.platform.object.create;

import static wbs.utils.collection.SetUtils.emptySet;
import static wbs.utils.etc.NullUtils.isNotNull;
import static wbs.utils.etc.OptionalUtils.optionalGetRequired;
import static wbs.utils.etc.OptionalUtils.optionalIsPresent;
import static wbs.utils.etc.TypeUtils.genericCastUnchecked;
import static wbs.utils.string.StringUtils.stringFormat;
import static wbs.web.utils.HtmlBlockUtils.htmlParagraphWriteFormat;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

import wbs.console.forms.core.ConsoleForm;
import wbs.console.forms.core.ConsoleFormType;
import wbs.console.helper.core.ConsoleHelper;
import wbs.console.helper.manager.ConsoleObjectManager;
import wbs.console.html.ScriptRef;
import wbs.console.part.AbstractPagePart;
import wbs.console.priv.UserPrivChecker;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.NestedTransaction;
import wbs.framework.database.Transaction;
import wbs.framework.entity.record.GlobalId;
import wbs.framework.entity.record.Record;
import wbs.framework.logging.LogContext;

import wbs.platform.scaffold.model.RootObjectHelper;

@Accessors (fluent = true)
@PrototypeComponent ("objectCreatePart")
public
class ObjectCreatePart <
	ObjectType extends Record <ObjectType>,
	ParentType extends Record <ParentType>
>
	extends AbstractPagePart {

	// singleton dependencies

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	ConsoleObjectManager objectManager;

	@SingletonDependency
	UserPrivChecker privChecker;

	@SingletonDependency
	RootObjectHelper rootHelper;

	// properties

	@Getter @Setter
	ConsoleHelper <ObjectType> consoleHelper;

	@Getter @Setter
	ConsoleFormType <ObjectType> formType;

	@Getter @Setter
	String parentPrivCode;

	@Getter @Setter
	String localFile;

	// state

	ConsoleHelper <ParentType> parentHelper;
	List <ParentType> parents;
	ParentType parent;

	ObjectType object;

	Map <String, Object> formHints;
	ConsoleForm <ObjectType> form;

	// implementation

	@Override
	public
	Set <ScriptRef> scriptRefs () {

		if (
			isNotNull (
				form)
		) {

			return form.scriptRefs ();

		} else {

			return emptySet ();

		}

	}

	@Override
	public
	void prepare (
			@NonNull Transaction parentTransaction) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"prepare");

		) {

			prepareParents (
				transaction);

			// if a field provider was provided

			/*
			if (formFieldsProvider != null) {

				prepareFieldSet (
					transaction);

			}
			*/

			// get update results

			form =
				formType.buildResponse (
					transaction,
					formHints);

			// create dummy instance

			object =
				consoleHelper.createInstance ();

			// set parent

			if (
				parent != null
				&& consoleHelper.canGetParent ()
			) {

				consoleHelper.setParent (
					object,
					parent);

			}

		}

	}

	void prepareParents (
			@NonNull Transaction parentTransaction) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"prepareParents");

		) {

			ImmutableMap.Builder <String, Object> formHintsBuilder =
				ImmutableMap.builder ();

			ConsoleHelper <ParentType> parentHelperTemp =
				objectManager.findConsoleHelperRequired (
					consoleHelper.parentClass ());

			parentHelper =
				parentHelperTemp;

			if (parentHelper.isRoot ()) {

				ParentType parentTemp =
					genericCastUnchecked (
						rootHelper.findRequired (
							transaction,
							0l));

				parent =
					parentTemp;

				return;

			}

			Optional <Long> parentIdOptional =
				requestContext.stuffInteger (
					parentHelper.idKey ());

			if (
				optionalIsPresent (
					parentIdOptional)
			) {

				// use specific parent

				parent =
					parentHelper.findRequired (
						transaction,
						optionalGetRequired (
							parentIdOptional));

				return;

			}

			ConsoleHelper <?> grandParentHelper =
				objectManager.findConsoleHelperRequired (
					parentHelper.parentClass ());

			Optional <Long> grandParentIdOptional =
				requestContext.stuffInteger (
					grandParentHelper.idKey ());

			if (
				optionalIsPresent (
					grandParentIdOptional)
			) {

				// show parents based on grand parent

				parents =
					parentHelper.findByParent (
						transaction,
						new GlobalId (
							grandParentHelper.objectTypeId (),
							optionalGetRequired (
								grandParentIdOptional)));

				// set grandparent hints

				Record <?> grandparent =
					grandParentHelper.findRequired (
						transaction,
						optionalGetRequired (
							grandParentIdOptional));

				formHintsBuilder.put (
					"grandparent",
					grandparent);

				formHintsBuilder.put (
					stringFormat (
						"%s.parent",
						consoleHelper.parentFieldName ()),
					grandparent);

				formHintsBuilder.put (
					stringFormat (
						"parent.%s",
						parentHelper.parentFieldName ()),
					grandparent);

				formHintsBuilder.put (
					stringFormat (
						"%s.%s",
						consoleHelper.parentFieldName (),
						parentHelper.parentFieldName ()),
					grandparent);

			} else {

				// show all parents

				parents =
					parentHelper.findAll (
						transaction);

			}

			formHints =
				formHintsBuilder.build ();

		}

	}

	/*
	void prepareFieldSet (
			@NonNull TaskLogger parentTaskLogger) {

		formFieldSet =
			parent != null
				? formFieldsProvider.getFieldsForParent (
					parentTaskLogger,
					parent)
				: formFieldsProvider.getStaticFields ();

	}
	*/

	@Override
	public
	void renderHtmlBodyContent (
			@NonNull Transaction parentTransaction) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"renderHtmlBodyContent");

		) {

			htmlParagraphWriteFormat (
				"Please enter the details for the new %h",
				consoleHelper.shortName ());

			form.outputFormTable (
				transaction,
				"post",
				requestContext.resolveLocalUrl (
					"/" + localFile),
				stringFormat (
					"create %h",
					consoleHelper.shortName ()));

		}

	}

}
