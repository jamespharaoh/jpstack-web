package wbs.console.tab;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.inject.Inject;

import wbs.console.request.ConsoleRequestContext;
import wbs.framework.utils.etc.Html;

public
class TabList {

	@Inject
	ConsoleRequestContext requestContext;

	List<TabRef> tabRefs =
		new ArrayList<TabRef> ();

	static
	class PreparedTab {
		String url;
		String label;
		boolean selected;

		PreparedTab (
				String newUrl,
				String newLabel,
				boolean newSelected) {

			url = newUrl;
			label = newLabel;
			selected = newSelected;

		}

	}

	public
	class Prepared {

		List<PreparedTab> preparedTabs =
			new ArrayList<PreparedTab> ();

		Prepared () {
		}

		public
		void go (
				ConsoleRequestContext requestContext) {

			PrintWriter out =
				requestContext.writer ();

			out.println("<p class=\"links\">");
			for (PreparedTab preparedTab : preparedTabs) {
				out.print("<a");
				if (preparedTab.selected)
					out.print(" class=\"selected\"");
				out.println(" href=\"" + Html.encode(preparedTab.url) + "\">"
						+ Html.encode(preparedTab.label) + "</a>");
			}
			out.println("</p>");

		}

	}

	public
	TabList () {
	}

	/**
	 * Initialise with a list of TabRefs, supplied var-arg style.
	 */
	public
	TabList (
			TabRef... tabRefs) {

		for (TabRef tabRef : tabRefs)
			add (tabRef);

	}

	public
	TabList (
			Tab... tabs) {

		for (Tab tab : tabs)
			add (tab);

	}

	public
	void add (
			TabRef tabRef) {

		if (tabRef == null)
			throw new NullPointerException ();

		tabRefs.add (tabRef);

	}

	public
	void add (
			Tab tab) {

		add (
			new TabRef (tab));

	}

	public
	void add (
			Tab tab,
			String label) {

		add (
			new TabRef (
				tab,
				label));
	}

	public
	Prepared prepare (
			Tab currentTab) {

		Prepared prepared =
			new Prepared ();

		boolean foundCurrent = false;

		for (TabRef tabRef
				: tabRefs) {

			if (tabRef.getTab () == currentTab) {

				foundCurrent = true;

			} else if (! tabRef.getTab ().isAvailable ()) {

				continue;

			}

			prepared.preparedTabs.add (
				new PreparedTab (
					tabRef.getTab ().getUrl (),
					tabRef.getLabel (),
					tabRef.getTab () == currentTab));

		}

		if (! foundCurrent) {

			prepared.preparedTabs.add (
				new PreparedTab (
					currentTab.getUrl (),
					currentTab.getDefaultLabel (),
					true));

		}

		return prepared;

	}

	public
	boolean contains (
			Tab tab) {

		for (TabRef tabRef
				: tabRefs) {

			if (tabRef.getTab () == tab)
				return true;

		}

		return false;

	}

	public
	Collection<TabRef> getTabRefs () {

		return tabRefs;

	}

}
