package wbs.framework.database;

import lombok.NonNull;

import wbs.framework.logging.TaskLogger;

public
interface Database {

	OwnedTransaction beginTransaction (
			TaskLogger parentTaskLogger,
			String summary,
			Object owner,
			boolean readWrite,
			boolean canJoin,
			boolean canCreateNew,
			boolean makeCurrent);

	default
	OwnedTransaction beginReadWrite (
			TaskLogger parentTaskLogger,
			String summary,
			Object owner) {

		return beginTransaction (
			parentTaskLogger,
			summary,
			owner,
			true,
			false,
			true,
			true);

	}

	default
	OwnedTransaction beginReadOnly (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull String summary,
			@NonNull Object owner) {

		return beginTransaction (
			parentTaskLogger,
			summary,
			owner,
			false,
			true,
			true,
			true);

	}

	default
	OwnedTransaction beginReadOnlyJoin (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull String summary,
			@NonNull Object owner) {

		return beginTransaction (
			parentTaskLogger,
			summary,
			owner,
			false,
			false,
			true,
			true);

	}

	BorrowedTransaction currentTransaction ();

	void flush ();
	void clear ();

	void flushAndClear ();

}
