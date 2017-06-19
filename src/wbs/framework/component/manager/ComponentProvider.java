package wbs.framework.component.manager;

import static wbs.utils.etc.TypeUtils.classNameFull;
import static wbs.utils.string.StringUtils.stringFormat;

import javax.inject.Provider;

import wbs.framework.logging.TaskLogger;

public
interface ComponentProvider <Type>
	extends Provider <Type> {

	Type provide (
			TaskLogger taskLogger);

	@Override
	@Deprecated
	default
	Type get () {

		throw new UnsupportedOperationException (
			stringFormat (
				"%s.get",
				classNameFull (
					getClass ())));

	}

}
