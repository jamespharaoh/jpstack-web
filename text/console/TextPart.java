package wbs.platform.text.console;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.platform.console.part.AbstractPagePart;

@Accessors (fluent = true)
@PrototypeComponent ("textPart")
public
class TextPart
	extends AbstractPagePart {

	@Getter @Setter
	String text;

	@Override
	public
	void goBodyStuff () {

		printFormat (
			"%s",
			text);

	}

}
