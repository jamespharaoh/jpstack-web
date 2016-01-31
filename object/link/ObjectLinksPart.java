package wbs.platform.object.link;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

import wbs.console.context.ConsoleApplicationScriptRef;
import wbs.console.forms.FormFieldLogic;
import wbs.console.forms.FormFieldSet;
import wbs.console.helper.ConsoleHelper;
import wbs.console.html.ScriptRef;
import wbs.console.part.AbstractPagePart;
import wbs.console.priv.PrivChecker;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.record.Record;
import wbs.framework.utils.etc.BeanLogic;
import wbs.platform.user.model.UserObjectHelper;

@Accessors (fluent = true)
@PrototypeComponent ("objectLinksPart")
public
class ObjectLinksPart
	extends AbstractPagePart {

	// dependencies

	@Inject
	FormFieldLogic formFieldLogic;

	@Inject
	PrivChecker privChecker;

	@Inject
	UserObjectHelper userHelper;

	// properties

	@Getter @Setter
	ConsoleHelper<?> consoleHelper;

	@Getter @Setter
	String contextLinksField;

	@Getter @Setter
	ConsoleHelper<?> targetHelper;

	@Getter @Setter
	FormFieldSet formFieldSet;

	@Getter @Setter
	String localFile;

	// state

	Record<?> contextObject;
	Set<?> contextLinks;

	List<? extends Record<?>> targetObjects;

	@Override
	public
	Set<ScriptRef> scriptRefs () {
		return scriptRefs;
	}

	@Override
	public
	void prepare () {

		contextObject =
			consoleHelper.lookupObject (
				requestContext.contextStuff ());

		contextLinks =
			(Set<?>)
			BeanLogic.getProperty (
				contextObject,
				contextLinksField);

		targetObjects =
			targetHelper.findAll ();

		Collections.sort (
			targetObjects);

	}

	@Override
	public
	void renderHtmlBodyContent () {

		printFormat (
			"<form",
			" action=\"%h\"",
			requestContext.resolveLocalUrl (
				"/" + localFile),
			" method=\"post\"",
			">\n");

		printFormat (
			"<p><input",
			" type=\"submit\"",
			" value=\"save changes\"",
			"></p>\n");

		printFormat (
			"<table class=\"list\">\n");

		printFormat (
			"<tr>\n");

		formFieldLogic.outputTableHeadings (
			formatWriter,
			formFieldSet);

		printFormat (
			"<th>Member</th>\n",
			"</tr>\n");

		for (
			Record<?> targetObject
				: targetObjects
		) {

			if (! privChecker.can (
					targetObject,
					"manage"))
				continue;

			printFormat (
				"<tr>\n");

			formFieldLogic.outputTableCellsList (
				formatWriter,
				formFieldSet,
				targetObject,
				ImmutableMap.of (),
				true);

			printFormat (
				"%s\n",
				requestContext.magicTdCheck (
					"link_" + targetObject.getId (),
					"member",
					contextLinks.contains (targetObject)),

				"</tr>\n");

		}

		printFormat (
			"</table>\n");

		printFormat (
			"<p><input",
			" type=\"submit\"",
			" value=\"save changes\"",
			"></p>\n");

		for (Record<?> targetObject
				: targetObjects) {

			if (! privChecker.can (
					targetObject,
					"manage"))
				continue;

			printFormat (
				"<input",
				" type=\"hidden\"",
				" name=\"old_link\"",
				" value=\"%h,%h\"",
				targetObject.getId (),
				contextLinks.contains (targetObject)
					? "true"
					: "false",
				">\n");

		}

		printFormat (
			"</form>\n");

		requestContext.flushScripts ();

	}

	// data

	static
	Set<ScriptRef> scriptRefs =
		ImmutableSet.<ScriptRef> of (

		ConsoleApplicationScriptRef.javascript (
			"/js/DOM.js"),

		ConsoleApplicationScriptRef.javascript (
			"/js/wbs.js")

	);

}
