package wbs.platform.object.link;

import static wbs.utils.collection.MapUtils.emptyMap;
import static wbs.utils.etc.LogicUtils.booleanToTrueFalse;
import static wbs.utils.etc.NumberUtils.integerToDecimalString;
import static wbs.utils.etc.TypeUtils.genericCastUnchecked;
import static wbs.web.utils.HtmlBlockUtils.htmlParagraphClose;
import static wbs.web.utils.HtmlBlockUtils.htmlParagraphOpen;
import static wbs.web.utils.HtmlFormUtils.htmlFormClose;
import static wbs.web.utils.HtmlFormUtils.htmlFormOpenPostAction;
import static wbs.web.utils.HtmlTableUtils.htmlTableClose;
import static wbs.web.utils.HtmlTableUtils.htmlTableHeaderCellWrite;
import static wbs.web.utils.HtmlTableUtils.htmlTableOpenList;
import static wbs.web.utils.HtmlTableUtils.htmlTableRowClose;
import static wbs.web.utils.HtmlTableUtils.htmlTableRowOpen;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.inject.Provider;

import com.google.common.collect.ImmutableSet;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

import wbs.console.context.ConsoleApplicationScriptRef;
import wbs.console.forms.FormFieldLogic;
import wbs.console.forms.FormFieldSet;
import wbs.console.helper.core.ConsoleHelper;
import wbs.console.html.HtmlTableCheckWriter;
import wbs.console.html.ScriptRef;
import wbs.console.part.AbstractPagePart;
import wbs.console.priv.UserPrivChecker;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.PrototypeDependency;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.entity.record.Record;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.TaskLogger;

import wbs.platform.user.model.UserObjectHelper;

import wbs.utils.etc.PropertyUtils;

@Accessors (fluent = true)
@PrototypeComponent ("objectLinksPart")
public
class ObjectLinksPart <
	ObjectType extends Record <ObjectType>,
	TargetType extends Record <TargetType>
>
	extends AbstractPagePart {

	// singleton dependencies

	@SingletonDependency
	FormFieldLogic formFieldLogic;

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	UserPrivChecker privChecker;

	@SingletonDependency
	UserObjectHelper userHelper;

	// prototype dependencies

	@PrototypeDependency
	Provider <HtmlTableCheckWriter> htmlTableCheckWriterProvider;

	// properties

	@Getter @Setter
	ConsoleHelper <ObjectType> consoleHelper;

	@Getter @Setter
	String contextLinksField;

	@Getter @Setter
	ConsoleHelper <TargetType> targetHelper;

	@Getter @Setter
	FormFieldSet <TargetType> targetFields;

	@Getter @Setter
	String localFile;

	// state

	Record <?> contextObject;
	Set <?> contextLinks;

	List <TargetType> targetObjects;

	@Override
	public
	Set <ScriptRef> scriptRefs () {
		return scriptRefs;
	}

	@Override
	public
	void prepare (
			@NonNull TaskLogger parentTaskLogger) {

		contextObject =
			consoleHelper.lookupObject (
				requestContext.consoleContextStuffRequired ());

		contextLinks =
			genericCastUnchecked (
				PropertyUtils.propertyGetAuto (
					contextObject,
					contextLinksField));

		targetObjects =
			targetHelper.findAll ();

		Collections.sort (
			targetObjects);

	}

	@Override
	public
	void renderHtmlBodyContent (
			@NonNull TaskLogger parentTaskLogger) {

		TaskLogger taskLogger =
			logContext.nestTaskLogger (
				parentTaskLogger,
				"renderHtmlBodyContent");

		// form open

		htmlFormOpenPostAction (
			requestContext.resolveLocalUrl (
				"/" + localFile));

		// form controls

		htmlParagraphOpen ();

		formatWriter.writeLineFormat (
			"<input",
			" type=\"submit\"",
			" value=\"save changes\"",
			">");

		htmlParagraphClose ();

		// table open

		htmlTableOpenList ();

		// table header

		htmlTableRowOpen ();

		formFieldLogic.outputTableHeadings (
			formatWriter,
			targetFields);

		htmlTableHeaderCellWrite (
			"Member");

		htmlTableRowClose ();

		// table content

		for (
			TargetType targetObject
				: targetObjects
		) {

			if (
				! privChecker.canRecursive (
					taskLogger,
					targetObject,
					"manage")
			) {
				continue;
			}

			htmlTableRowOpen ();

			formatWriter.increaseIndent ();

			formFieldLogic.outputTableCellsList (
				taskLogger,
				formatWriter,
				targetFields,
				targetObject,
				emptyMap (),
				true);

			htmlTableCheckWriterProvider.get ()

				.name (
					"link_" + targetObject.getId ())

				.label (
					"member")

				.value (
					contextLinks.contains (
						targetObject))

				.write (
					formatWriter);

			formatWriter.decreaseIndent ();

			htmlTableRowClose ();

		}

		// table close

		htmlTableClose ();

		// form controls

		htmlParagraphOpen ();

		formatWriter.writeLineFormat (
			"<input",
			" type=\"submit\"",
			" value=\"save changes\"",
			">");

		htmlParagraphClose ();

		// form hidden fields

		for (
			Record<?> targetObject
				: targetObjects
		) {

			if (
				! privChecker.canRecursive (
					taskLogger,
					targetObject,
					"manage")
			) {
				continue;
			}

			formatWriter.writeLineFormat (
				"<input",
				" type=\"hidden\"",
				" name=\"old_link\"",
				" value=\"%h,%h\"",
				integerToDecimalString (
					targetObject.getId ()),
				booleanToTrueFalse (
					contextLinks.contains (
						targetObject)),
				">");

		}

		// form close

		htmlFormClose ();

		// flush scripts

		requestContext.flushScripts ();

	}

	// data

	static
	Set <ScriptRef> scriptRefs =
		ImmutableSet.<ScriptRef> of (

		ConsoleApplicationScriptRef.javascript (
			"/js/DOM.js"),

		ConsoleApplicationScriptRef.javascript (
			"/js/wbs.js")

	);

}
