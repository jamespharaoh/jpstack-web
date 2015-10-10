package wbs.console.part;

import static wbs.framework.utils.etc.Misc.stringFormat;

import java.io.PrintWriter;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;

import wbs.console.html.HtmlLink;
import wbs.console.html.ScriptRef;
import wbs.console.request.ConsoleRequestContext;
import wbs.framework.utils.etc.StringFormatter;

import com.google.common.collect.ImmutableSet;

public
class AbstractPagePart
	implements PagePart {

	@Inject
	protected
	ConsoleRequestContext requestContext;

	protected
	Map<String,Object> parameters;

	protected
	PrintWriter out;

	private
	boolean withMarkup = false;

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
			Map<String,Object> parameters) {

		if (requestContext == null) {

			throw new IllegalStateException (
				stringFormat (
					"%s not autowired correctl",
					getClass ().getName ().toString ()));

		}

		this.parameters =
			parameters;

		out =
			requestContext.writer ();

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
			Object... args) {

		out.print (
			StringFormatter.standard (
				args));

	}

}
