package wbs.web.pathhandler;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lombok.NonNull;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.TaskLogger;

import wbs.web.file.WebFile;

@PrototypeComponent ("regexpPathHandler")
public
class RegexpPathHandler
	implements PathHandler {

	// singleton dependencies

	@ClassSingletonDependency
	LogContext logContext;

	// state

	private
	List <Entry> entries =
		new ArrayList<> ();

	// constructors

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

	// utility methods

	public
	void add (
			@NonNull Entry entry) {

		entries.add (
			entry);

	}

	// implementation

	@Override
	public
	WebFile processPath (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull String path) {

		try (

			TaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"processPath");

		) {

			for (
				Entry entry
					: entries
			) {

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

	// entry class

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
				Matcher matcher);

	}

}
