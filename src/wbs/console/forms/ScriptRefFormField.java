package wbs.console.forms;

import java.util.Set;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import wbs.console.html.ScriptRef;
import wbs.framework.application.annotations.PrototypeComponent;

@Accessors (fluent = true)
@PrototypeComponent ("scriptRefFormField")
public
class ScriptRefFormField
	implements FormField<Object,Object,Object,Object> {

	@Getter
	Boolean virtual = true;

	@Getter
	Boolean large;

	@Getter @Setter
	Set<ScriptRef> scriptRefs;

	@Override
	public
	Boolean fileUpload () {
		return false;
	}

}
