package wbs.platform.text.console;

import com.google.common.base.Optional;

import wbs.console.helper.AbstractConsoleHooks;
import wbs.framework.utils.etc.Html;
import wbs.platform.text.model.TextRec;

public
class TextConsoleHooks
	extends AbstractConsoleHooks<TextRec> {

	@Override
	public
	Optional<String> getHtml (
			TextRec text) {

		return Optional.of (
			Html.encode (
				"\"" + text.getText () + "\""));

	}

}
