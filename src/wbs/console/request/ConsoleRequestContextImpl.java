package wbs.console.request;

import static wbs.framework.utils.etc.Misc.ifNull;
import static wbs.framework.utils.etc.Misc.isNotNull;
import static wbs.framework.utils.etc.Misc.joinWithoutSeparator;
import static wbs.framework.utils.etc.Misc.pluralise;
import static wbs.framework.utils.etc.Misc.stringFormat;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;
import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

import org.apache.commons.fileupload.FileItem;
import org.joda.time.Instant;

import wbs.console.context.ConsoleContext;
import wbs.console.context.ConsoleContextStuff;
import wbs.console.helper.ConsoleObjectManager;
import wbs.console.priv.PrivDataLoader;
import wbs.console.tab.Tab;
import wbs.console.tab.TabContext;
import wbs.console.tab.TabList;
import wbs.framework.application.annotations.ProxiedRequestComponent;
import wbs.framework.record.Record;
import wbs.framework.utils.etc.Html;
import wbs.framework.web.RequestContext;

import com.google.common.base.Optional;
import com.google.common.base.Predicates;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;

/**
 * Extends RequestContext to provide loads of extra functionality useful in all
 * console code. This object is completely stateless, with all stateful
 * information being stored into and retrieved from the request, response and
 * context objects. As such an instance can be constructed at any time from
 * these three references and be functionally identical.
 *
 * The static factory function make (...) takes a RequestContext and returns it
 * as is if it is already a ConsoleRequestContext or constructs a new
 * ConsoleRequestContext from it if not.
 */
@Accessors (fluent = true)
@ProxiedRequestComponent (
	value = "consoleRequestContext",
	proxyInterface = ConsoleRequestContext.class)
public
class ConsoleRequestContextImpl
	implements ConsoleRequestContext {

	// dependencies

	@Inject
	ConsoleObjectManager objectManager;

	@Inject
	PrivDataLoader privChecker;

	@Inject
	RequestContext requestContext;

	// properties

	@Getter @Setter
	String foreignContextPath;

	@Getter @Setter
	String changedContextPath;

	// TODO store a single copy of us in request instead of allowing multiple
	// copies and sharing data via request

	// TODO remove anything which doesn't belong here

	// TODO use spring scopes to make much of this redundant

	@Override
	public
	Integer userId () {

		return (Integer)
			session ("myUserId");

	}

	@Override
	public
	Object request (
			@NonNull String key) {

		return requestContext
			.request (key);

	}

	@Override
	public
	String servletPath () {

		return requestContext
			.servletPath ();

	}

	@Override
	public
	void addError (
			@NonNull String message) {

		Notices.addError (
			requestContext.request (),
			message);

	}

	@Override
	public
	void addNotice (
			@NonNull String message) {

		Notices.addNotice (
			requestContext.request (),
			message);

	}

	@Override
	public
	void addNotice (
			@NonNull String message,
			@NonNull String type) {

		Notices.add (
			requestContext.request (),
			message,
			type);

	}

	@Override
	public
	void addScript (
			@NonNull String script) {

		@SuppressWarnings ("unchecked")
		List<String> scripts =
			(List<String>) request ("scripts");

		if (scripts == null) {

			scripts =
				new ArrayList<String> ();

			request (
				"scripts",
				scripts);

		}

		scripts.add (script);

	}

	@Override
	public
	void addWarning (
			@NonNull String message) {

		Notices.addWarning (
			requestContext.request (),
			message);

	}

	@Override
	public
	String applicationPathPrefix () {

		return requestContext.applicationPathPrefix ();

	}

	@Override
	public
	String resolveContextUrl (
			@NonNull String contextUrl) {

		if (foreignContextPath () == null) {

			throw new IllegalStateException (
				stringFormat (
					"Unable due resolve a context URL, as there is no current ",
					"context."));

		}

		return joinWithoutSeparator (
			foreignContextPath (),
			contextUrl);

	}

	@Override
	public
	String parameter (
			@NonNull String key) {

		return requestContext
			.parameter (key);

	}

	@Override
	public
	void session (
			@NonNull String key,
			Object object) {

		requestContext.session (
			key,
			object);

	}

	@Override
	public
	Object session (
			@NonNull String key) {

		return requestContext.session (
			key);

	}

	@Override
	public
	void copyIntParamToSession (
			@NonNull String name) {

		String str =
			parameter (name);

		if (str == null)
			return;

		try {

			session (
				name,
				new Integer (str));

		} catch (NumberFormatException e) {
		}

	}

	@Override
	public
	void flushNotices () {

		flushNotices (
			writer ());

	}

	@Override
	public
	void flushNotices (
			@NonNull PrintWriter out) {

		Notices notices =
			(Notices)
			requestContext.request ().getAttribute (
				"wbs.notices");

		if (notices != null)
			notices.flush (out);

	}

	@Override
	public
	PrintWriter writer () {

		return requestContext.writer ();

	}

	@Override
	public
	void flushScripts () {

		PrintWriter out =
			writer ();

		@SuppressWarnings ("unchecked")
		List<String> scripts = (List<String>)
			request ("scripts");

		if (scripts == null)
			return;

		out.println ("<script language=\"JavaScript\">");

		for (String s : scripts) {
			out.println (s);
		}

		out.println ("</script>");

		scripts.clear ();

	}

	@Override
	public
	Object context (
			String key) {

		return requestContext
			.context (key);

	}

	@Override
	public
	List<String> getParameterValues (
			@NonNull String name) {

		return Arrays.asList (
			requestContext
				.request ()
				.getParameterValues (name));

	}

	@Override
	public
	Map<String,String> requestFormData () {

		return parameterMapSimple ();

	}

	@Override
	public
	String magicTdCheck (
			@NonNull String name,
			@NonNull String label,
			boolean value,
			int colspan) {

		StringBuilder stringBuilder =
			new StringBuilder ();

		// open the td

		stringBuilder.append (
			stringFormat (

				"<td",

				" id=\"%h_td\"",
				name,

				" class=\"%h\"",
				value
					? "selected"
					: "unselected",

				" style=\"cursor: pointer;\"",

				" onclick=\"%h\"",
				stringFormat (
					"tdcheck_td ('%j');",
					name),

				" onmouseover=\"%h\"",
				stringFormat (
					"tdcheck_focus ('%j');",
					name),

				" onmouseout=\"%h\"",
				stringFormat (
					"tdcheck_update ('%j');",
					name),

				">"));

		stringBuilder.append (
			stringFormat (
				"<table class=\"layout\">",
				"<tr>"));

		stringBuilder.append (
			stringFormat (

				"<td><input",
				" type=\"checkbox\"",

				" id=\"%h\"",
				name,

				" name=\"%h\"",
				name,

				" onfocus=\"%h\"",
				stringFormat (
					"tdcheck_focus ('%j');",
					name),

				" onblur=\"%h\"",
				stringFormat (
					"tdcheck_update ('%j');",
					name),

				" onclick=\"%h\"",
				stringFormat (
					"tdcheck_checkbox ('%j', event);",
					name),

				"%s",
				value
					? " checked"
					: "",

				">",

				"</td>"));

		stringBuilder.append (
			stringFormat (
				"<td>&nbsp;</td>"));

		stringBuilder.append (
			stringFormat (
				"<td>%h</td>",
				label));

		stringBuilder.append (
			stringFormat (
				"</tr>",
				"</table>"));

		stringBuilder.append (
			stringFormat (
				"</td>"));

		// add this to the list of script bits still to do

		addScript (
			stringFormat (
				"tdcheck_update ('%j');",
				name));

		// and return

		return stringBuilder.toString ();

	}

	@Override
	public
	String magicTdCheck (
			@NonNull String name,
			@NonNull String label,
			boolean value) {

		return magicTdCheck (
			name,
			label,
			value,
			1);

	}

	@Override
	public
	void magicTdRadio (
			@NonNull String name,
			String value,
			@NonNull String label,
			boolean selected,
			@NonNull Map<String,Object> options) {

		PrintWriter out =
			writer();

		String nameValue = name + "_" + value;

		String onChangeStr =
			(String)
			options.get ("onChange");

		out.print("<td");
		out.print(" id=\"" + Html.encode(nameValue + "_td") + "\"");
		out.print(" class=\"" + (selected ? "selected" : "unselected") + "\"");
		out.print(" style=\"cursor: pointer;\"");
		out.print(" onclick=\"tdcheck_td ('" + Html.jsqe(nameValue) + "');");
		if (onChangeStr != null)
			out.print(" " + Html.encode(onChangeStr));
		out.print("\"");
		out.print(" onmouseover=\"tdcheck_focus ('" + Html.jsqe(nameValue)
				+ "');\"");
		out.print(" onmouseout=\"tdcheck_update ('" + Html.jsqe(nameValue)
				+ "');\"");
		out
				.print("><table class=\"layout\" border=\"0\" cellspacing=\"0\" cellpadding=\"0\"><tr>");

		out.print("<td>");
		out.print("<input type=\"radio\"");
		out.print(" id=\"" + Html.encode(nameValue) + "\"");
		out.print(" name=\"" + Html.encode(name) + "\"");
		out.print(" onfocus=\"tdcheck_focus ('" + Html.jsqe(nameValue)
				+ "');\"");
		out.print(" onblur=\"tdcheck_update ('" + Html.jsqe(nameValue)
				+ "');\"");

		out.print(" onclick=\"tdcheck_checkbox ('" + Html.jsqe(nameValue)
				+ "', event);");
		if (onChangeStr != null)
			out.print(" " + Html.encode(onChangeStr));
		out.print("\"");

		out.print(" value=\"" + Html.encode(value) + "\"");
		if (selected)
			out.print(" selected");
		out.print("></td><td>&nbsp;</td><td>" + Html.encode(label) + "</td>");

		out.println("</tr></table></td>");

		// add this to the list of script bits still to do
		addScript("tdcheck_update ('" + Html.jsqe(nameValue) + "');");

	}

	@Override
	public
	void magicTdRadio (
			@NonNull String name,
			String value,
			@NonNull String label,
			boolean selected) {

		magicTdRadio (
			name,
			value,
			label,
			selected,
			null);

	}

	@Override
	public TabContext setTabContext (
			@NonNull TabContext tabContext) {

		request (
			"tabContext",
			tabContext);

		return tabContext;

	}

	@Override
	public
	void addTabContext (
			@NonNull Tab parentTab,
			@NonNull String title,
			@NonNull TabList tabList) {

		tabContext ().add (
			parentTab,
			title,
			tabList);

	}

	@Override
	public
	TabContext tabContext () {

		return (TabContext)
			request ("tabContext");

	}

	@Override
	public
	void tabContext (
			@NonNull String title1,
			@NonNull TabList tabList1) {

		setTabContext (
			new TabContext (
				title1,
				tabList1));

	}

	@Override
	public
	String resolveLocalUrl (
			@NonNull String wantedPath) {

		if (! wantedPath.startsWith ("/")) {

			throw new IllegalArgumentException (
				stringFormat (
					"Invalid wanted path: %s",
					wantedPath));

		}

		if (isNotNull (
				changedContextPath ())) {

			return joinWithoutSeparator (
				applicationPathPrefix (),
				changedContextPath (),
				wantedPath);

		} else {

			return wantedPath.substring (1);

		}

	}

	@Override
	public
	String prettyDateDiff (
			Instant before,
			Instant after) {

		if (before == null || after == null)
			return null;

		return prettyMsInterval (
			+ after.getMillis ()
			- before.getMillis ());

	}

	@Override
	public
	String prettyMsInterval (
			Long interval) {

		if (interval == null)
			return null;

		if (interval < 2 * 1000L)
			return pluralise (interval, "millisecond");

		if (interval < 2 * 60000L)
			return pluralise (interval / 1000L, "second");

		if (interval < 2 * 3600000L)
			return pluralise (interval / 60000L, "minute");

		if (interval < 2 * 86400000L)
			return pluralise (interval / 3600000L, "hour");

		if (interval < 2 * 2678400000L)
			return pluralise (interval / 86400000L, "day");

		if (interval < 2 * 31557600000L)
			return pluralise (interval / 2592000000L, "month");

		return pluralise (interval / 31556736000L, "year");

	}

	@Override
	public
	String prettyMsInterval (
			Integer interval) {

		if (interval == null)
			return null;

		return prettyMsInterval (
			(long) interval);

	}

	@Override
	public
	String prettySecsInterval (
			Long interval) {

		if (interval == null)
			return null;

		if (interval < 2 * 60L)
			return pluralise (interval, "second");

		if (interval < 2 * 3600L)
			return pluralise (interval / 60L, "minute");

		if (interval < 2 * 86400L)
			return pluralise (interval / 3600L, "hour");

		if (interval < 2 * 2678400L)
			return pluralise (interval / 86400L, "day");

		if (interval < 2 * 31557600L)
			return pluralise (interval / 2592000L, "month");

		return pluralise (interval / 31556736L, "year");

	}

	@Override
	public
	String prettySecsInterval (
			Integer interval) {

		if (interval == null)
			return null;

		return prettySecsInterval (
			(long) interval);

	}

	@Override
	public
	void request (
			@NonNull String key,
			Object value) {

		requestContext.request (
			key,
			value);

	}

	@Override
	public
	void formData (
			@NonNull Map<String,String> newFormData) {

		request (
			"formData",
			newFormData);

	}

	@Override
	public
	void setEmptyFormData () {

		request (
			"formData",
			Collections.<String,String>emptyMap ());

	}

	@Override
	public
	Map<String,String> getFormData () {

		@SuppressWarnings ("unchecked")
		Map<String,String> formData =
			(Map<String,String>)
			request ("formData");

		if (formData != null)
			return formData;

		return requestFormData ();

	}

	@Override
	public
	String getForm (
			@NonNull String key) {

		return getFormData ()
			.get (key);

	}

	@Override
	public
	String getForm (
			@NonNull String key,
			String def) {

		return ifNull (
			getFormData ().get (key),
			def);

	}

	@Override
	public
	boolean isCommitted () {

		return response ().isCommitted ();

	}

	@Override
	public
	void reset () {

		response ().reset ();

	}

	@Override
	public
	ConsoleContext consoleContext () {

		return (ConsoleContext)
			request ("context");

	}

	@Override
	public
	ConsoleContextStuff contextStuff () {

		return (ConsoleContextStuff)
			request ("contextStuff");

	}

	@Override
	public
	Integer stuffInt (
			@NonNull String key) {

		return (Integer)
			contextStuff ().get (key);

	}

	@Override
	public
	Object stuff (
			@NonNull String key) {

		return contextStuff ()
			.get (key);

	}

	@Override
	public
	boolean canContext (
			@NonNull String... privKeys) {

		return contextStuff ()
			.can (privKeys);

	}

	@Override
	public
	boolean canView (
			@NonNull Record<?> object) {

		return objectManager
			.canView (object);

	}

	@Override
	public
	int requestUnique () {

		Integer unique =
			(Integer) request ("unique");

		if (unique == null)
			unique = 0;
		else
			unique = unique + 1;

		request ("unique", unique);

		return unique;
	}

	@Override
	public
	void grant (
			@NonNull String string) {

		contextStuff ()
			.grant (string);

	}

	@Override
	public
	void addNotices (
			@NonNull List<String> notices) {

		for (String notice
				: notices) {

			addNotice (notice);

		}

	}

	@Override
	public
	void hideFormData (
			@NonNull Set<String> keys) {

		formData (
			ImmutableMap.<String,String>copyOf (
				Maps.filterKeys (
					getFormData (),
					Predicates.in (keys))));

	}

	@Override
	public
	String pathInfo () {

		return requestContext
			.pathInfo ();

	}

	@Override
	public
	boolean canGetWriter () {

		return requestContext
			.canGetWriter ();

	}

	@Override
	public
	void sendRedirect (
			@NonNull String location)
		throws IOException {

		requestContext
			.sendRedirect (location);

	}

	@Override
	public
	void status (
			int status) {

		requestContext
			.status (status);

	}

	@Override
	public
	String method () {

		return requestContext.method ();

	}

	@Override
	public
	String requestUri () {

		return requestContext.requestUri ();

	}

	@Override
	public
	Map<String,List<String>> parameterMap () {

		return requestContext.parameterMap ();

	}

	@Override
	public
	void setHeader (
			@NonNull String key,
			@NonNull String value) {

		requestContext.setHeader (
			key,
			value);

	}

	@Override
	public
	OutputStream outputStream ()
		throws IOException {

		return requestContext
			.outputStream ();

	}

	@Override
	public
	String parameter (
			@NonNull String key,
			String defaultValue) {

		return requestContext
			.parameter (
				key,
				defaultValue);

	}

	@Override
	public
	int requestInt (
			@NonNull String key) {

		return requestContext.requestInt (
			key);

	}

	@Override
	public
	int parameterInt (
			@NonNull String key) {

		return requestContext
			.parameterInt (key);

	}

	@Override
	public
	String sessionId () {

		return requestContext
			.sessionId ();

	}

	@Override
	public
	Map<String,String> parameterMapSimple () {

		return requestContext
			.parameterMapSimple ();

	}

	@Override
	public
	RequestDispatcher requestDispatcher (
			@NonNull String path) {

		return requestContext
			.requestDispatcher (path);

	}

	@Override
	public
	String requestPath () {

		return requestContext
			.requestPath ();

	}

	@Override
	public
	boolean post () {

		return requestContext
			.post ();

	}

	@Override
	public
	InputStream getResourceAsStream (
			@NonNull String path) {

		return requestContext.resourceAsStream (
			path);

	}

	@Override
	public
	String realPath (
			@NonNull String path) {

		return requestContext.realPath (
			path);

	}

	@Override
	public
	HttpServletResponse response () {

		return requestContext.response ();

	}

	@Override
	public
	HttpServletRequest request () {

		return requestContext.request ();

	}

	@Override
	public
	String resolveApplicationUrl (
			@NonNull String applicationUrl) {

		return requestContext.resolveApplicationUrl (
			applicationUrl);

	}

	@Override
	public
	boolean isMultipart () {

		return requestContext.isMultipart ();

	}

	@Override
	public
	List<FileItem> fileItems () {

		return requestContext.fileItems ();

	}

	@Override
	public
	FileItem fileItemFile (
			String name) {

		return requestContext.fileItemFile (
			name);

	}

	@Override
	public
	String fileItemField (
			String name) {

		return requestContext.fileItemField (
			name);

	}

	@Override
	public
	Optional<String> header (
			String name) {

		return Optional.fromNullable (
			requestContext.header (
				name));

	}

}
