package wbs.framework.web;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletException;

import lombok.NonNull;

public
class RegexpPathHandler
	implements PathHandler {

	public abstract static
	class Entry {

		Pattern pattern;

		public
		Entry (
				String patternString) {

			pattern =
				Pattern.compile (
					patternString);

		}

		protected abstract
		WebFile handle (
				Matcher matcher)
			throws ServletException;

	}

	private
	List<Entry> entries =
		new ArrayList<Entry> ();

	public
	RegexpPathHandler () {

	}

	public
	RegexpPathHandler (
			@NonNull Entry... newEntries) {

		for (Entry entry
				: newEntries) {

			add (
				entry);

		}

	}

	public
	void add (
			@NonNull Entry entry) {

		entries.add (
			entry);

	}

	@Override
	public
	WebFile processPath (
			@NonNull String path)
		throws ServletException {

		for (Entry entry
				: entries) {

			Matcher matcher =
				entry.pattern.matcher (path);

			if (matcher.matches ()) {

				return entry.handle (
					matcher);

			}

		}

		return null;

	}

}
