package wbs.platform.object.settings;

import static wbs.utils.collection.MapUtils.emptyMap;
import static wbs.utils.etc.OptionalUtils.optionalGetRequired;
import static wbs.utils.etc.OptionalUtils.optionalIsPresent;
import static wbs.web.utils.HtmlBlockUtils.htmlHeadingTwoWrite;
import static wbs.web.utils.HtmlBlockUtils.htmlParagraphClose;
import static wbs.web.utils.HtmlBlockUtils.htmlParagraphOpen;
import static wbs.web.utils.HtmlFormUtils.htmlFormClose;
import static wbs.web.utils.HtmlFormUtils.htmlFormOpenPostAction;
import static wbs.web.utils.HtmlFormUtils.htmlFormOpenPostActionEncoding;
import static wbs.web.utils.HtmlTableUtils.htmlTableClose;
import static wbs.web.utils.HtmlTableUtils.htmlTableOpenDetails;

import java.util.Set;

import com.google.common.base.Optional;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

import wbs.console.forms.core.ConsoleForm;
import wbs.console.forms.core.ConsoleFormType;
import wbs.console.helper.core.ConsoleHelper;
import wbs.console.helper.manager.ConsoleObjectManager;
import wbs.console.html.ScriptRef;
import wbs.console.lookup.ObjectLookup;
import wbs.console.part.AbstractPagePart;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.NestedTransaction;
import wbs.framework.database.Transaction;
import wbs.framework.entity.record.Record;
import wbs.framework.logging.LogContext;

import wbs.platform.scaffold.model.RootObjectHelper;

@Accessors (fluent = true)
@PrototypeComponent ("objectSettingsPart")
public
class ObjectSettingsPart <
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
	RootObjectHelper rootHelper;

	// properties

	@Getter @Setter
	ObjectLookup <ObjectType> objectLookup;

	@Getter @Setter
	ConsoleHelper <ObjectType> consoleHelper;

	@Getter @Setter
	String editPrivKey;

	@Getter @Setter
	String localName;

	@Getter @Setter
	ConsoleFormType <ObjectType> formContextBuilder;

	@Getter @Setter
	String removeLocalName;

	// state

	ObjectType object;
	ParentType parent;

	boolean canEdit;

	ConsoleForm <ObjectType> form;

	// implementation

	@Override
	public
	Set <ScriptRef> scriptRefs () {

		return form.allFields ().scriptRefs ();

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

			object =
				objectLookup.lookupObject (
					transaction,
					requestContext.consoleContextStuffRequired ());

			canEdit = (

				editPrivKey != null

				&& requestContext.canContext (
					editPrivKey)

			);

			/*
			if (formFieldsProvider != null) {

				prepareParent (
					transaction);

				prepareFieldSet (
					transaction);

			}
			*/

			form =
				formContextBuilder.buildResponse (
					transaction,
					emptyMap ());

		}

	}

	void prepareParent (
			@NonNull Transaction parentTransaction) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"prepareParent");

		) {

			ConsoleHelper <ParentType> parentHelper =
				objectManager.findConsoleHelperRequired (
					consoleHelper.parentClass ());

			if (parentHelper.isRoot ()) {

				parent =
					parentHelper.findRequired (
						transaction,
						0l);

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

		}

	}

	/*
	void prepareFieldSet (
			@NonNull TaskLogger parentTaskLogger) {

		formFieldSet =
			formFieldsProvider.getFieldsForObject (
				parentTaskLogger,
				object);

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

			if (canEdit) {

				String enctype =
					"application/x-www-form-urlencoded";

				try {

					if (form.fileUpload ()) {

						enctype =
							"multipart/form-data";

					}

				} catch (Exception exception) {

					enctype =
						"application/x-www-form-urlencoded";

				}

				htmlFormOpenPostActionEncoding (
					requestContext.resolveLocalUrl (
						localName),
					enctype);

			}

			htmlTableOpenDetails ();

			form.outputFormRows (
				transaction,
				object);

			htmlTableClose ();

			if (canEdit) {

				htmlParagraphOpen ();

				formatWriter.writeLineFormat (
					"<input",
					" type=\"submit\"",
					" value=\"save changes\"",
					">");

				htmlParagraphClose ();

				htmlFormClose ();

				if (removeLocalName != null) {

					htmlHeadingTwoWrite (
						"Remove");

					htmlFormOpenPostAction (
						requestContext.resolveLocalUrl (
							removeLocalName));

					formatWriter.writeLineFormat (
						"<input",
						" type=\"submit\"",
						" value=\"remove\"",
						">");

					htmlFormClose ();

				}

			}

		}

	}

}
