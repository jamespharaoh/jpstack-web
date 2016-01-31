package wbs.console.forms;

import static wbs.framework.utils.etc.Misc.contains;
import static wbs.framework.utils.etc.Misc.stringFormat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

import wbs.console.html.ScriptRef;
import wbs.framework.data.annotations.DataAttribute;
import wbs.framework.data.annotations.DataChildren;
import wbs.framework.data.annotations.DataChildrenIndex;
import wbs.framework.data.annotations.DataClass;

@Accessors (fluent = true)
@SuppressWarnings ({ "rawtypes", "unchecked" })
@DataClass ("form-field-set")
public
class FormFieldSet {

	// properties

	@DataAttribute
	@Getter @Setter
	String name;

	@DataAttribute
	@Getter @Setter
	Boolean fileUpload;

	@DataChildren
	@Getter @Setter
	List<FormField> formFields =
		new ArrayList<FormField> ();

	// state

	@DataChildrenIndex
	@Getter @Setter
	Map<String,FormField> formFieldsByName =
		new HashMap<String,FormField> ();

	// utility methods

	public
	Set<ScriptRef> scriptRefs () {

		Set<ScriptRef> scriptRefs =
			new LinkedHashSet<ScriptRef> ();

		for (
			FormField formField
				: formFields
		) {

			scriptRefs.addAll (
				formField.scriptRefs ());

		}

		return scriptRefs;

	}

	public
	int columns () {

		int ret = 0;

		for (
			FormField formField
				: formFields
		) {

			if (formField.virtual ()) {
				continue;
			}

			ret ++;

		}

		return ret;

	}

	public
	FormFieldSet addFormField (
			@NonNull FormField formField) {

		if (formField.virtual ()) {

			formFields.add (
				formField);

		} else {

			if (
				contains (
					formFieldsByName,
					formField.name ())
			) {

				throw new RuntimeException (
					stringFormat (
						"Duplicated field name: %s.%s",
						name (),
						formField.name ()));

			}

			formFields.add (
				formField);

			formFieldsByName.put (
				formField.name (),
				formField);

		}

		return this;

	}

	public
	FormField formField (
			@NonNull String name) {

		return formFieldsByName.get (
			name);

	}

}
