package wbs.framework.data.tools;

import static wbs.utils.collection.CollectionUtils.emptyList;

import java.io.InputStream;
import java.util.List;

import wbs.framework.logging.TaskLogger;

public
interface DataFromXml {

	Object readInputStream (
			TaskLogger parentTaskLogger,
			InputStream inputStream,
			String filename,
			List <Object> parents);

	default
	Object readInputStream (
			TaskLogger parentTaskLogger,
			InputStream inputStream,
			String filename) {

		return readInputStream (
			parentTaskLogger,
			inputStream,
			filename,
			emptyList ());

	}

	Object readClasspath (
			TaskLogger parentTaskLogger,
			String filename,
			List <Object> parents);

	default
	Object readClasspath (
			TaskLogger parentTaskLogger,
			String filename) {

		return readClasspath (
			parentTaskLogger,
			filename,
			emptyList ());

	}

	Object readFilename (
			TaskLogger parentTaskLogger,
			String filename,
			List <Object> parents);

	default
	Object readFilename (
			TaskLogger parentTaskLogger,
			String filename) {

		return readFilename (
			parentTaskLogger,
			filename,
			emptyList ());

	}

}

