package wbs.web.exceptions;

import static wbs.utils.collection.CollectionUtils.emptyList;
import static wbs.utils.collection.CollectionUtils.singletonList;
import static wbs.utils.collection.MapUtils.mapItemForKey;
import static wbs.utils.etc.NumberUtils.fromJavaInteger;
import static wbs.utils.etc.NumberUtils.integerToDecimalString;
import static wbs.utils.etc.OptionalUtils.optionalGetRequired;
import static wbs.utils.etc.OptionalUtils.optionalIsPresent;
import static wbs.utils.etc.TypeUtils.classInstantiate;
import static wbs.utils.string.StringUtils.stringFormat;

import java.util.List;
import java.util.Map;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import lombok.NonNull;

import wbs.web.misc.HttpStatus;

public
class HttpStatusException
	extends RuntimeException {

	private final
	Long statusCode;

	private final
	String statusMessage;

	private
	List <String> errors =
		ImmutableList.of ();

	public
	HttpStatusException (
			@NonNull Long statusCode,
			@NonNull String statusMessage,
			@NonNull List <String> errors) {

		super (
			stringFormat (
				"%s: %s",
				integerToDecimalString (
					statusCode),
				statusMessage));

		this.statusCode =
			statusCode;

		this.statusMessage =
			statusMessage;

		this.errors =
			errors;

	}

	public
	HttpStatusException (
			@NonNull Long statusCode,
			@NonNull String statusMessage) {

		this (
			statusCode,
			statusMessage,
			emptyList ());

	}

	public
	Long statusCode () {
		return statusCode;
	}

	public
	String statusMessage () {
		return statusMessage;
	}

	public
	List <String> errors () {
		return errors;
	}

	public static
	HttpStatusException forStatus (
			@NonNull Long statusCode,
			@NonNull String statusMessage) {

		Optional <Class <? extends HttpStatusException>>
			exceptionClassOptional =
				mapItemForKey (
					exceptionsByStatusCode,
					statusCode);

		if (
			optionalIsPresent (
				exceptionClassOptional)
		) {

			Class <? extends HttpStatusException> exceptionClass =
				optionalGetRequired (
					exceptionClassOptional);

			return classInstantiate (
				exceptionClass,
				singletonList (
					String.class),
				singletonList (
					statusMessage));

		} else {

			return new HttpStatusException (
				statusCode,
				statusMessage);

		}

	}

	public static
	HttpStatusException forStatus (
			@NonNull Integer statusCode,
			@NonNull String statusMessage) {

		return forStatus (
			fromJavaInteger (
				statusCode),
			statusMessage);

	}

	public static
	Map <Long, Class <? extends HttpStatusException>> exceptionsByStatusCode =
		ImmutableMap.<Long, Class <? extends HttpStatusException>> builder ()

		.put (
			HttpStatus.httpBadRequest,
			HttpBadRequestException.class)

		.put (
			HttpStatus.httpMethodNotAllowed,
			HttpMethodNotAllowedException.class)

		.put (
			HttpStatus.httpNotFound,
			HttpNotFoundException.class)

		.put (
			HttpStatus.httpTooManyRequests,
			HttpTooManyRequestsException.class)

		.put (
			HttpStatus.httpUnprocessableEntity,
			HttpUnprocessableEntityException.class)

		.build ()

	;

}
