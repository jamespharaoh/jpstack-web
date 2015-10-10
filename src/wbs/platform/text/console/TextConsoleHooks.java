package wbs.platform.text.console;

import wbs.console.helper.AbstractConsoleHooks;
import wbs.framework.utils.etc.Html;
import wbs.platform.text.model.TextRec;

import com.google.common.base.Optional;

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
