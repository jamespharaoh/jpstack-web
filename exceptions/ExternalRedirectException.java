package wbs.web.exceptions;

import lombok.Getter;
import lombok.NonNull;

public
class ExternalRedirectException
	extends RuntimeException {

	@Getter
	String location;

	public
	ExternalRedirectException (
			@NonNull String newLocation) {

		super (
			newLocation);

		location =
			newLocation;

	}

	public
	ExternalRedirectException (
			@NonNull Throwable cause) {

		super (
			cause);

	}

}
