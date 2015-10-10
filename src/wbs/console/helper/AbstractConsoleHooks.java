package wbs.console.helper;

import wbs.framework.record.Record;

import com.google.common.base.Optional;

public
class AbstractConsoleHooks<Type extends Record<Type>>
	implements ConsoleHooks<Type> {

	@Override
	public
	Optional<String> getHtml (
			Type object) {

		return Optional.<String>absent ();

	}

}
