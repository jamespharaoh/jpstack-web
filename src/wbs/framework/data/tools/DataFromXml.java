package wbs.framework.data.tools;

import java.io.InputStream;
import java.util.Collections;
import java.util.List;

public
interface DataFromXml {

	Object readInputStream (
			InputStream inputStream,
			String filename,
			List <Object> parents);

	default
	Object readInputStream (
			InputStream inputStream,
			String filename) {

		return readInputStream (
			inputStream,
			filename,
			Collections.emptyList ());

	}

	Object readClasspath (
			String filename,
			List <Object> parents);

	default
	Object readClasspath (
			String filename) {

		return readClasspath (
			filename,
			Collections.emptyList ());

	}

	Object readFilename (
			String filename,
			List <Object> parents);

	default
	Object readFilename (
			String filename) {

		return readFilename (
			filename,
			Collections.emptyList ());

	}

}

