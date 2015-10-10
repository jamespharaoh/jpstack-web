package wbs.console.tab;

public abstract
class Tab {

	private
	String defaultLabel;

	public
	Tab (
			String defaultLabel) {

		if (defaultLabel == null)
			throw new NullPointerException ();

		this.defaultLabel =
			defaultLabel;

	}

	public
	String getDefaultLabel () {

		return defaultLabel;

	}

	/**
	 * Returns the URL set in the constructor. Override this to dynamically
	 * change the URL by request.
	 *
	 * @param requestContext
	 *            the request context.
	 * @return the url to link to.
	 */

	public abstract
	String getUrl ();

	/**
	 * Called to check if the tab should be displayed. Override this to make a
	 * tab appear and disappear dynamically depending on the request.
	 *
	 * @param requestContext
	 *            the request context.
	 * @return true if the tab should be displayed.
	 */

	public
	boolean isAvailable () {

		return true;

	}

}
