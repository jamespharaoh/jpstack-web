package wbs.console.forms;

import static wbs.utils.etc.Misc.doNothing;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

import lombok.NonNull;

import wbs.console.html.ScriptRef;

import wbs.framework.logging.TaskLogger;

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

		return ImmutableSet.of ();

	}

	default
	boolean canView (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull Container container,
			@NonNull Map <String, Object> hints) {

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
