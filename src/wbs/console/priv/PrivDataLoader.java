package wbs.console.priv;

public
interface PrivDataLoader {

	void refresh ();

	UserPrivData getUserPrivData (
			int userId);

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
