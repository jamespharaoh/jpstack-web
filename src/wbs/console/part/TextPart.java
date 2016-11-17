package wbs.console.part;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.logging.TaskLogger;

@Accessors (fluent = true)
@PrototypeComponent ("textPart")
public
class TextPart
	extends AbstractPagePart {

	@Getter @Setter
	String text;

	@Override
	public
	void renderHtmlBodyContent (
			@NonNull TaskLogger parentTaskLogger) {

		formatWriter.writeString (
			text);

	}

}
