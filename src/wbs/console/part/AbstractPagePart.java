package wbs.console.part;

import static wbs.framework.utils.etc.StringUtils.stringFormat;

import java.io.PrintWriter;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.ImmutableSet;

import lombok.NonNull;

import wbs.console.html.HtmlLink;
import wbs.console.html.ScriptRef;
import wbs.console.request.ConsoleRequestContext;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.utils.formatwriter.FormatWriter;
import wbs.framework.utils.formatwriter.WriterFormatWriter;

public
class AbstractPagePart
	implements PagePart {

	// singleton dependencies

	@SingletonDependency
	protected
	Database database;

	@SingletonDependency
	protected
	ConsoleRequestContext requestContext;

	// state

	protected
	Map <String, Object> parameters;

	protected
	PrintWriter printWriter;

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
	Set<ScriptRef> scriptRefs () {

		return ImmutableSet.<ScriptRef>of ();

	}

	@Override
	public
	Set<HtmlLink> links () {

		return ImmutableSet.<HtmlLink>of ();

	}

	@Override
	public
	void setup (
			@NonNull Map<String,Object> parameters) {

		if (requestContext == null) {

			throw new IllegalStateException (
				stringFormat (
					"%s not autowired correctl",
					getClass ().getName ().toString ()));

		}

		this.parameters =
			parameters;

		printWriter =
			requestContext.writer ();

		formatWriter =
			new WriterFormatWriter (
				printWriter);

		transaction =
			database.currentTransaction ();

	}

	@Override
	public
	void prepare () {
	}

	@Override
	public
	void renderHtmlHeadContent () {
	}

	@Override
	public
	void renderHtmlBodyContent () {
	}

	public
	void printFormat (
			@NonNull Object... arguments) {

		formatWriter.writeFormatArray (
			arguments);

	}

}
