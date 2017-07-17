package wbs.framework.data.tools;

import static wbs.utils.collection.CollectionUtils.emptyList;
import static wbs.utils.etc.OptionalUtils.optionalGetRequired;
import static wbs.utils.etc.OptionalUtils.optionalOrThrow;

import java.io.InputStream;
import java.util.List;

import com.google.common.base.Optional;

import lombok.NonNull;

import wbs.framework.logging.TaskLogger;

public
interface DataFromXml {

	// --------- read input stream

	Optional <Object> readInputStream (
			TaskLogger parentTaskLogger,
			InputStream inputStream,
			String filename,
			List <Object> parents);

	default
	Object readInputStreamRequired (
			TaskLogger parentTaskLogger,
			InputStream inputStream,
			String filename,
			List <Object> parents) {

		return optionalGetRequired (
			readInputStream (
				parentTaskLogger,
				inputStream,
				filename,
				parents));

	}

	default
	Optional <Object> readInputStream (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull InputStream inputStream,
			@NonNull String filename) {

		return readInputStream (
			parentTaskLogger,
			inputStream,
			filename,
			emptyList ());

	}

	default
	Object readInputStreamRequired (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull InputStream inputStream,
			@NonNull String filename) {

		return optionalGetRequired (
			readInputStream (
				parentTaskLogger,
				inputStream,
				filename,
				emptyList ()));

	}

	// --------- read classpath

	Optional <Object> readClasspath (
			TaskLogger parentTaskLogger,
			String filename,
			List <Object> parents);

	default
	Object readClasspathRequired  (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull String filename,
			@NonNull List <Object> parents) {

		return optionalOrThrow (
			readClasspath (
				parentTaskLogger,
				filename,
				parents),
			() -> parentTaskLogger.makeException ());

	}

	default
	Optional <Object> readClasspath (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull String filename) {

		return readClasspath (
			parentTaskLogger,
			filename,
			emptyList ());

	}

	default
	Object readClasspathRequired (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull String filename) {

		return optionalOrThrow (
			readClasspath (
				parentTaskLogger,
				filename,
				emptyList ()),
			() -> parentTaskLogger.makeException ());

	}

	// --------- read filename

	Object readFilenameRequired (
			TaskLogger parentTaskLogger,
			String filename,
			List <Object> parents);

	default
	Object readFilenameRequired (
			TaskLogger parentTaskLogger,
			String filename) {

		return readFilenameRequired (
			parentTaskLogger,
			filename,
			emptyList ());

	}

}

