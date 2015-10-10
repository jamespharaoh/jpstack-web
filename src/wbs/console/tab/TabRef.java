package wbs.console.tab;

import lombok.Getter;

public
class TabRef {

	@Getter
	Tab tab;

	@Getter
	String label;

	public
	TabRef (
			Tab tab) {

		if (tab == null)
			throw new NullPointerException ();

		this.tab = tab;
		this.label = tab.getDefaultLabel ();

	}

	public
	TabRef (
			Tab tab,
			String label) {

		if (tab == null || label == null)
			throw new NullPointerException ();

		this.tab = tab;
		this.label = label;

	}

}
