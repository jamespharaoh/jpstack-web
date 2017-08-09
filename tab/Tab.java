package wbs.console.tab;

import wbs.framework.database.Transaction;

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

	public abstract
	String getUrl (
			Transaction parentTransaction);

	public
	boolean isAvailable () {

		return true;

	}

}
