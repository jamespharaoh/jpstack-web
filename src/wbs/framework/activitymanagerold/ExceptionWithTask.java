package wbs.framework.activitymanagerold;

import static wbs.utils.etc.NullUtils.isNotNull;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

public
interface ExceptionWithTask {

	Task task ();

	default
	List <Task> stack () {

		List <Task> reverseStack =
			new ArrayList<> ();

		Task currentTask =
			task ();

		while (
			isNotNull (
				currentTask)
		) {

			reverseStack.add (
				currentTask);

			currentTask =
				currentTask.parent ();

		}

		return ImmutableList.copyOf (
			Lists.reverse (
				reverseStack));

	}

	/*
	default
	void printStackTrace (
			@NonNull PrintStream printStream) {

		printStream.print (
			"Stack tracek to print stream\n");

	}

	default
	void printStackTrace (
			@NonNull PrintWriter printWriter) {

		printWriter.print (
			"Stack tracek to print writer\n");

	}
	*/

}
