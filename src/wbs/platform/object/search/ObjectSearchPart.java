package wbs.platform.object.search;

import static wbs.utils.etc.Misc.isNotNull;
import static wbs.utils.etc.Misc.isNull;
import static wbs.utils.etc.OptionalUtils.optionalCast;

import java.io.Serializable;
import java.util.Map;
import java.util.Set;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.experimental.Accessors;

import wbs.console.forms.FormField.FormType;
import wbs.console.forms.FormFieldLogic;
import wbs.console.forms.FormFieldLogic.UpdateResultSet;
import wbs.console.forms.FormFieldSet;
import wbs.console.helper.ConsoleHelper;
import wbs.console.helper.ConsoleHelperRegistry;
import wbs.console.html.ScriptRef;
import wbs.console.misc.JqueryScriptRef;
import wbs.console.part.AbstractPagePart;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.entity.record.Record;

@Accessors (fluent = true)
@PrototypeComponent ("objectSearchPart")
public
class ObjectSearchPart
	extends AbstractPagePart {

	// singleton dependencies

	@SingletonDependency
	ConsoleHelperRegistry consoleHelperRegistry;

	@SingletonDependency
	FormFieldLogic formFieldLogic;

	// properties

	@Getter @Setter
	ConsoleHelper <?> consoleHelper;

	@Getter @Setter
	Class <?> searchClass;

	@Getter @Setter
	String sessionKey;

	@Getter @Setter
	FormFieldSet formFieldSet;

	@Getter @Setter
	String fileName;

	// state

	Object search;
	Optional <UpdateResultSet> updateResultSet;
	Map <String, Object> formHints;

	// details

	@Override
	public
	Set <ScriptRef> scriptRefs () {

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
			optionalCast (
				UpdateResultSet.class,
				requestContext.request (
					"objectSearchUpdateResultSet"));

		ImmutableMap.Builder<String,Object> formHintsBuilder =
			ImmutableMap.builder ();

		if (consoleHelper.parentExists ()) {

			ConsoleHelper <?> parentHelper =
				consoleHelperRegistry.findByObjectClass (
					consoleHelper.parentClass ());

			Long parentId =
				requestContext.stuffInteger (
					parentHelper.idKey ());

			if (
				isNotNull (
					parentId)
			) {

				Record <?> parent =
					parentHelper.findRequired (
						requestContext.stuffInteger (
							parentHelper.idKey ()));

				formHintsBuilder.put (
					consoleHelper.parentFieldName (),
					parent);

			}

		}

		formHints =
			formHintsBuilder.build ();

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
			formHints,
			FormType.search,
			"search");

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
			search,
			formHints,
			"search");

		printFormat (
			"\t}\n");

		printFormat (
			"</script>\n");

	}

}
