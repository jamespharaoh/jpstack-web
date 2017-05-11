package wbs.console.forms.core;

import lombok.Getter;
import lombok.experimental.Accessors;

import wbs.console.forms.types.FormItem;

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
