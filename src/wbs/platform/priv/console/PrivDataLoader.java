package wbs.platform.priv.console;

public interface PrivDataLoader {

	void refresh ();

	UserPrivData getUserPrivData (
			int userId);

}
