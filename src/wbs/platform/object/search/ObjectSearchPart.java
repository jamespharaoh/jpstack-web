package wbs.platform.object.search;

import static wbs.framework.utils.etc.Misc.isNull;

import java.io.Serializable;
import java.util.Set;

import javax.inject.Inject;

import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.experimental.Accessors;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

import wbs.console.forms.FormField.FormType;
import wbs.console.forms.FormFieldLogic;
import wbs.console.forms.FormFieldLogic.UpdateResultSet;
import wbs.console.forms.FormFieldSet;
import wbs.console.helper.ConsoleHelper;
import wbs.console.html.ScriptRef;
import wbs.console.misc.JqueryScriptRef;
import wbs.console.part.AbstractPagePart;
import wbs.framework.application.annotations.PrototypeComponent;

@Accessors (fluent = true)
@PrototypeComponent ("objectSearchPart")
public
class ObjectSearchPart
	extends AbstractPagePart {

	// dependencies

	@Inject
	FormFieldLogic formFieldLogic;

	// properties

	@Getter @Setter
	ConsoleHelper<?> consoleHelper;

	@Getter @Setter
	Class<?> searchClass;

	@Getter @Setter
	String sessionKey;

	@Getter @Setter
	FormFieldSet formFieldSet;

	@Getter @Setter
	String fileName;

	// state

	Object search;
	Optional<UpdateResultSet> updateResultSet;

	// details

	@Override
	public
	Set<ScriptRef> scriptRefs () {

		return ImmutableSet.<ScriptRef>builder ()

			.addAll (
				super.scriptRefs ())

			.add (
				JqueryScriptRef.instance)

			.build ();

	}

	// implementation

	@Override
	@SneakyThrows ({
		IllegalAccessException.class,
		InstantiationException.class
	})
	public
	void prepare () {

		search =
			requestContext.session (
				sessionKey + "Fields");

		if (
			isNull (
				search)
		) {

			search =
				searchClass.newInstance ();

			requestContext.session (
				sessionKey + "Fields",
				(Serializable)
				search);

		}

		updateResultSet =
			Optional.fromNullable (
				(UpdateResultSet)
				requestContext.request (
					"objectSearchUpdateResultSet"));

	}

	@Override
	public
	void renderHtmlBodyContent () {

		printFormat (
			"<form",
			" method=\"post\"",
			" action=\"%h\"",
			requestContext.resolveLocalUrl (
				"/" + fileName),
			">\n");

		printFormat (
			"<table",
			" class=\"details\"",
			">\n");

		formFieldLogic.outputFormRows (
			requestContext,
			formatWriter,
			formFieldSet,
			updateResultSet,
			search,
			ImmutableMap.of (),
			FormType.search);

		printFormat (
			"</table>\n");

		printFormat (
			"<p><input",
			" type=\"submit\"",
			" value=\"search\"",
			">\n");

		printFormat (
			"<input",
			" type=\"button\"",
			" value=\"reset form\"",
			" onclick=\"resetSearchForm (); return false;\"",
			">\n");

		printFormat (
			"</form>\n");

		printFormat (
			"<script type=\"text/javascript\">\n");

		printFormat (
			"\tfunction resetSearchForm () {\n");

		formFieldLogic.outputFormReset (
			formatWriter,
			"\t\t",
			formFieldSet,
			FormType.search,
			search);

		printFormat (
			"\t}\n");

		printFormat (
			"</script>\n");

	}

}
