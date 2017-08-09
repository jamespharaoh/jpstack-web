package wbs.console.part;

import static wbs.utils.etc.Misc.doNothing;

import java.util.Set;

import com.google.common.collect.ImmutableSet;

import lombok.NonNull;

import wbs.console.html.HtmlLink;
import wbs.console.html.ScriptRef;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.database.Transaction;
import wbs.framework.logging.LogContext;

import wbs.utils.string.FormatWriter;

public
class AbstractPagePart
	implements PagePart {

	// singleton dependencies

	@ClassSingletonDependency
	LogContext logContext;

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
	void prepare (
			@NonNull Transaction parentTransaction) {

		doNothing ();

	}

	@Override
	public
	void renderHtmlHeadContent (
			@NonNull Transaction parentTransaction,
			@NonNull FormatWriter formatWriter) {

		doNothing ();

	}

	@Override
	public
	void renderHtmlBodyContent (
			@NonNull Transaction parentTransaction,
			@NonNull FormatWriter formatWriter) {

		doNothing ();

	}

	@Override
	public
	void cleanup () {

		doNothing ();

	}

}
