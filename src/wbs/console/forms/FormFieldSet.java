package wbs.console.forms;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import wbs.console.html.ScriptRef;
import wbs.framework.data.annotations.DataAttribute;
import wbs.framework.data.annotations.DataChildren;
import wbs.framework.data.annotations.DataClass;

@Accessors (fluent = true)
@SuppressWarnings ({ "rawtypes", "unchecked" })
@DataClass ("form-field-set")
public
class FormFieldSet {

	// properties

	@DataAttribute
	@Getter @Setter
	Boolean fileUpload;

	@DataChildren
	@Getter @Setter
	List<FormField> formFields =
		new ArrayList<FormField> ();

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

}
