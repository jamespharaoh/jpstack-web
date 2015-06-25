package wbs.platform.console.forms;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import wbs.framework.data.annotations.DataAttribute;
import wbs.framework.data.annotations.DataChildren;
import wbs.framework.data.annotations.DataClass;
import wbs.platform.console.html.ScriptRef;

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
			new HashSet<ScriptRef> ();

		for (
			FormField formField
				: formFields
		) {

			scriptRefs.addAll (
				formField.scriptRefs ());

		}

		return scriptRefs;

	}

}
