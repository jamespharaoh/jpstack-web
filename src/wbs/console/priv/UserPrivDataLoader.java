package wbs.console.priv;

public
interface UserPrivDataLoader {

	void refresh ();

	UserPrivData getUserPrivData (
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
