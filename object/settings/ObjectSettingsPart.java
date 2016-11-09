package wbs.platform.object.settings;

import static wbs.utils.etc.Misc.isNotNull;
import static wbs.utils.etc.OptionalUtils.optionalCast;
import static wbs.utils.web.HtmlBlockUtils.htmlHeadingTwoWrite;
import static wbs.utils.web.HtmlBlockUtils.htmlParagraphClose;
import static wbs.utils.web.HtmlBlockUtils.htmlParagraphOpen;
import static wbs.utils.web.HtmlFormUtils.htmlFormClose;
import static wbs.utils.web.HtmlFormUtils.htmlFormOpenPostAction;
import static wbs.utils.web.HtmlFormUtils.htmlFormOpenPostActionEncoding;
import static wbs.utils.web.HtmlTableUtils.htmlTableClose;
import static wbs.utils.web.HtmlTableUtils.htmlTableOpenDetails;

import java.util.LinkedHashSet;
import java.util.Set;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import wbs.console.forms.FieldsProvider;
import wbs.console.forms.FormFieldLogic;
import wbs.console.forms.FormFieldLogic.UpdateResultSet;
import wbs.console.forms.FormFieldSet;
import wbs.console.forms.FormType;
import wbs.console.helper.core.ConsoleHelper;
import wbs.console.helper.manager.ConsoleObjectManager;
import wbs.console.html.ScriptRef;
import wbs.console.lookup.ObjectLookup;
import wbs.console.part.AbstractPagePart;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.entity.record.Record;
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

	@SingletonDependency
	ConsoleObjectManager objectManager;

	@SingletonDependency
	FormFieldLogic formFieldLogic;

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
	FormFieldSet <ObjectType> formFieldSet;

	@Getter @Setter
	String removeLocalName;

	@Getter @Setter
	FieldsProvider <ObjectType, ParentType> formFieldsProvider;

	// state

	Optional <UpdateResultSet> updateResultSet;
	ObjectType object;
	ParentType parent;
	boolean canEdit;

	// implementation

	@Override
	public
	Set<ScriptRef> scriptRefs () {

		Set<ScriptRef> scriptRefs =
			new LinkedHashSet<ScriptRef> ();

		scriptRefs.addAll (
			formFieldSet.scriptRefs ());

		return scriptRefs;

	}

	@Override
	public
	void prepare () {

		updateResultSet =
			optionalCast (
				UpdateResultSet.class,
				requestContext.request (
					"objectSettingsUpdateResultSet"));

		object =
			objectLookup.lookupObject (
				requestContext.contextStuff ());

		canEdit = (

			editPrivKey != null

			&& requestContext.canContext (
				editPrivKey)

		);

		if (formFieldsProvider != null) {

			prepareParent ();
			prepareFieldSet ();

		}

	}

	void prepareParent () {

		ConsoleHelper <ParentType> parentHelper =
			objectManager.findConsoleHelperRequired (
				consoleHelper.parentClass ());

		if (parentHelper.isRoot ()) {

			parent =
				parentHelper.findRequired (
					0l);

			return;

		}

		Long parentId =
			requestContext.stuffInteger (
				parentHelper.idKey ());

		if (
			isNotNull (
				parentId)
		) {

			// use specific parent

			parent =
				parentHelper.findRequired (
					parentId);

			return;

		}

	}

	void prepareFieldSet () {

		formFieldSet =
			formFieldsProvider.getFieldsForObject (
				object);

	}

	@Override
	public
	void renderHtmlHeadContent () {

	}

	@Override
	public
	void renderHtmlBodyContent () {

		if (canEdit) {

			String enctype =
				"application/x-www-form-urlencoded";

			try {

				if (formFieldSet.fileUpload ()) {

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

		formFieldLogic.outputFormRows (
			requestContext,
			formatWriter,
			formFieldSet,
			updateResultSet,
			object,
			ImmutableMap.of (),
			FormType.update,
			"settings");

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
