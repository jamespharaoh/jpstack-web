package wbs.utils.string;

import java.util.function.Consumer;

import lombok.NonNull;

public
class FormatWriterUtils {

	public static
	String formatWriterConsumerToString (
			@NonNull Consumer <FormatWriter> consumer) {

		try (

			LazyFormatWriter formatWriter =
				new LazyFormatWriter ();

		) {

			consumer.accept (
				formatWriter);

			return formatWriter.toString ();

		}

	}

}
