package wbs.platform.console.forms;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import wbs.platform.console.html.ScriptRef;

@Accessors (fluent = true)
@SuppressWarnings ({ "rawtypes", "unchecked" })
public
class FormFieldSet {

	@Getter @Setter
	List<FormField> formFields =
		new ArrayList<FormField> ();

	public
	Set<ScriptRef> scriptRefs () {

		Set<ScriptRef> scriptRefs =
			new HashSet<ScriptRef> ();

		for (FormField formField
				: formFields) {

			scriptRefs.addAll (
				formField.scriptRefs ());

		}

		return scriptRefs;

	}

}
