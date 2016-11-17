package wbs.platform.text.console;

import lombok.NonNull;

import com.google.common.base.Optional;

import wbs.console.helper.core.ConsoleHooks;
import wbs.platform.text.model.TextRec;
import wbs.web.utils.HtmlUtils;

public
class TextConsoleHooks
	implements ConsoleHooks<TextRec> {

	@Override
	public
	Optional<String> getHtml (
			@NonNull TextRec text,
			@NonNull Boolean mini) {

		return Optional.of (
			HtmlUtils.htmlEncode (
				"\"" + text.getText () + "\""));

	}

}
