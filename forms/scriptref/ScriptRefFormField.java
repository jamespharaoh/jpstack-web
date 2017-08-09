package wbs.console.forms.scriptref;

import java.util.Set;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import wbs.console.forms.types.VirtualFormItem;
import wbs.console.html.ScriptRef;

import wbs.framework.component.annotations.PrototypeComponent;

@Accessors (fluent = true)
@PrototypeComponent ("scriptRefFormField")
public
class ScriptRefFormField <Container>
	implements VirtualFormItem <Container> {

	@Getter @Setter
	Set <ScriptRef> scriptRefs;

}
