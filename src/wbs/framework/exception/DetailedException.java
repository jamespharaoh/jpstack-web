package wbs.framework.exception;

import java.util.List;
import java.util.Map;

import lombok.NonNull;

public
class DetailedException
	extends RuntimeException {

	private
	Map <String, List <String>> details;

	public
	DetailedException (
			@NonNull String message,
			@NonNull Throwable cause,
			@NonNull Map <String, List <String>> details) {

		super (
			message,
			cause);

		this.details = details;

	}

	public
	String message () {
		return getMessage ();
	}

	public
	Throwable cause () {
		return getCause ();
	}

	public
	Map <String, List <String>> details () {
		return this.details;
	}

}
