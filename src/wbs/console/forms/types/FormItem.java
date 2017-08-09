package wbs.console.forms.types;

import static wbs.utils.collection.SetUtils.emptySet;
import static wbs.utils.etc.Misc.doNothing;

import java.util.Collection;
import java.util.Set;

import com.google.common.collect.ImmutableList;

import lombok.NonNull;

import wbs.console.forms.core.ConsoleForm;
import wbs.console.html.HtmlLink;
import wbs.console.html.ScriptRef;

import wbs.framework.database.Transaction;

public
interface FormItem <Container> {

	default
	String name () {

		throw new UnsupportedOperationException ();

	}

	default
	String label () {

		throw new UnsupportedOperationException ();

	}

	default
	Collection <FormItem <Container>> children () {

		return ImmutableList.of ();

	}

	default
	Set <ScriptRef> scriptRefs () {
		return emptySet ();
	}

	default
	Set <HtmlLink> styles () {
		return emptySet ();
	}

	default
	boolean canView (
			@NonNull Transaction parentTransaction,
			@NonNull ConsoleForm <Container> formContext,
			@NonNull Container object) {

		return true;

	}

	default
	void init (
			@NonNull String fieldSetName) {

		doNothing ();

	}

	default
	Iterable <FormField <Container, ?, ?, ?>> formFields () {

		return ImmutableList.of ();

	}

}
