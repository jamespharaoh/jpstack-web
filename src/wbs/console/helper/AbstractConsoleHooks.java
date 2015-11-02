package wbs.console.helper;

import static wbs.framework.utils.etc.Misc.doNothing;

import com.google.common.base.Optional;

import wbs.framework.record.Record;

public
class AbstractConsoleHooks<Type extends Record<Type>>
	implements ConsoleHooks<Type> {

	@Override
	public
	Optional<String> getHtml (
			Type object) {

		return Optional.<String>absent ();

	}

	@Override
	public
	Optional<String> getListClass (
			Type object) {

		return Optional.<String>absent ();

	}

	@Override
	public
	void applySearchFilter (
			Object searchObject) {

		doNothing ();

	}

}
