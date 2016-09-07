package wbs.console.part;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import wbs.framework.component.annotations.PrototypeComponent;

@Accessors (fluent = true)
@PrototypeComponent ("textPart")
public
class TextPart
	extends AbstractPagePart {

	@Getter @Setter
	String text;

	@Override
	public
	void renderHtmlBodyContent () {

		printFormat (
			"%s",
			text);

	}

}
