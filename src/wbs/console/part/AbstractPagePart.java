package wbs.console.part;

import static wbs.utils.etc.Misc.doNothing;
import static wbs.utils.etc.TypeUtils.classNameSimple;
import static wbs.utils.string.FormatWriterUtils.currentFormatWriter;
import static wbs.utils.string.StringUtils.stringFormat;

import java.util.Map;
import java.util.Set;

import com.google.common.collect.ImmutableSet;

import lombok.NonNull;

import wbs.console.html.HtmlLink;
import wbs.console.html.ScriptRef;
import wbs.console.request.ConsoleRequestContext;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.TaskLogger;

import wbs.utils.string.FormatWriter;

public
class AbstractPagePart
	implements PagePart {

	// singleton dependencies

	@SingletonDependency
	protected
	Database database;

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	protected
	ConsoleRequestContext requestContext;

	// state

	protected
	Map <String, Object> parameters;

	//protected
	//PrintWriter printWriter;

	protected
	FormatWriter formatWriter;

	protected
	Transaction transaction;

	private
	boolean withMarkup = false;

	// accessors

	public
	boolean isWithMarkup () {
		return withMarkup;
	}

	@Override
	public
	void setWithMarkup (
			boolean withMarkup) {

		this.withMarkup =
			withMarkup;

	}

	// details

	@Override
	public
	Set <ScriptRef> scriptRefs () {

		return ImmutableSet.of ();

	}

	@Override
	public
	Set <HtmlLink> links () {

		return ImmutableSet.of ();

	}

	@Override
	public
	void setup (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull Map <String, Object> parameters) {

		if (requestContext == null) {

			throw new IllegalStateException (
				stringFormat (
					"%s not autowired correctl",
					classNameSimple (
						getClass ())));

		}

		this.parameters =
			parameters;

		formatWriter =
			currentFormatWriter ();

		transaction =
			database.currentTransaction ();

	}

	@Override
	public
	void prepare (
			@NonNull TaskLogger parentTaskLogger) {

	}

	@Override
	public
	void renderHtmlHeadContent (
			@NonNull TaskLogger parentTaskLogger) {

	}

	@Override
	public
	void renderHtmlBodyContent (
			@NonNull TaskLogger parentTaskLogger) {

	}

	@Override
	public
	void cleanup () {

		doNothing ();

	}

}
