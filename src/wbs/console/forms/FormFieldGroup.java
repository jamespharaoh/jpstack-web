package wbs.console.forms;

import lombok.Getter;
import lombok.experimental.Accessors;

@Accessors (fluent = true)
public
class FormFieldGroup <Container>
	implements FormItem <Container> {

	@Getter
	Boolean fileUpload = false;

	@Getter
	Boolean virtual = false;

	@Getter
	Boolean group = true;

	@Getter
	Boolean large = false;

}
