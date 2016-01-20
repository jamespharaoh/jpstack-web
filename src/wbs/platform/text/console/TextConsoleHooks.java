package wbs.platform.text.console;

import lombok.NonNull;

import com.google.common.base.Optional;

import wbs.console.helper.ConsoleHooks;
import wbs.framework.utils.etc.Html;
import wbs.platform.text.model.TextRec;

public
class TextConsoleHooks
	implements ConsoleHooks<TextRec> {

	@Override
	public
	Optional<String> getHtml (
			@NonNull TextRec text,
			@NonNull Boolean mini) {

		return Optional.of (
			Html.encode (
				"\"" + text.getText () + "\""));

	}

}
