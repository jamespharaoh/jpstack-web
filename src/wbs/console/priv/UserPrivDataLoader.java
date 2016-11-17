package wbs.console.priv;

import wbs.framework.logging.TaskLogger;

public
interface UserPrivDataLoader {

	void refresh (
			TaskLogger taskLogger);

	UserPrivData getUserPrivData (
			TaskLogger taskLogger,
			Long userId);

	public static
	class UnknownObjectException
		extends RuntimeException {

		public
		UnknownObjectException (
				String message) {

			super (
				message);

		}

	}

}
