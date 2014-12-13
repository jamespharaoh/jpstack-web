package wbs.platform.object.settings;

import java.util.HashSet;
import java.util.Set;

import javax.inject.Inject;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.record.Record;
import wbs.platform.console.forms.FormFieldLogic;
import wbs.platform.console.forms.FormFieldSet;
import wbs.platform.console.html.ScriptRef;
import wbs.platform.console.lookup.ObjectLookup;
import wbs.platform.console.part.AbstractPagePart;

@Accessors (fluent = true)
@PrototypeComponent ("objectSettingsPart")
public
class ObjectSettingsPart
	extends AbstractPagePart {

	// dependencies

	@Inject
	FormFieldLogic formFieldLogic;

	// properties

	@Getter @Setter
	ObjectLookup<?> objectLookup;

	@Getter @Setter
	String editPrivKey;

	@Getter @Setter
	String localName;

	@Getter @Setter
	FormFieldSet formFieldSet;

	@Getter @Setter
	String removeLocalName;

	// state

	Record<?> object;
	boolean canEdit;

	// implementation

	@Override
	public
	Set<ScriptRef> scriptRefs () {

		Set<ScriptRef> scriptRefs =
			new HashSet<ScriptRef> ();

		scriptRefs.addAll (
			formFieldSet.scriptRefs ());

		return scriptRefs;

	}

	@Override
	public
	void prepare () {

		object =
			(Record<?>)
			objectLookup.lookupObject (
				requestContext.contextStuff ());

		canEdit =
			editPrivKey != null
				&& requestContext.canContext (editPrivKey);

	}

	@Override
	public
	void goHeadStuff () {

		//for (PagePart pagePart : pageParts)
		//	pagePart.goHeadStuff();

	}

	@Override
	public
	void goBodyStuff () {

		if (canEdit) {

			printFormat (
				"<form",
				" method=\"post\"",
				" action=\"%s\"",
				requestContext.resolveLocalUrl (localName),
				">\n");

		}

		printFormat (
			"<table class=\"details\">\n");

		formFieldLogic.outputFormRows (
			out,
			formFieldSet,
			object);

		printFormat (
			"</table>");

		if (canEdit) {

			printFormat (
				"<p><input",
				" type=\"submit\"",
				" value=\"save changes\"",
				"></p>\n");

			printFormat (
				"</form>\n");

			if (removeLocalName != null) {

				printFormat (
					"<h2>Remove</h2>");

				printFormat (
					"<form",
					" method=\"post\"",
					" action=\"%h\"",
					requestContext.resolveLocalUrl (
						removeLocalName),
					">\n");

				printFormat (
					"<p><input",
					" type=\"submit\"",
					" value=\"remove\"",
					"></p>\n");

				printFormat (
					"</form>\n");

			}

		}

	}

}
