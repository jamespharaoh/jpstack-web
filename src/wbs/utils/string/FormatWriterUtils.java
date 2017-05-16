package wbs.utils.string;

import static wbs.utils.etc.LogicUtils.referenceNotEqualWithClass;
import static wbs.utils.etc.NullUtils.isNotNull;
import static wbs.utils.etc.NullUtils.isNull;

import java.util.function.Consumer;

import lombok.NonNull;

public
class FormatWriterUtils {

	private static
	ThreadLocal <FormatWriter> currentFormatWriterThreadLocal =
		new ThreadLocal <FormatWriter> ();

	public static
	void setCurrentFormatWriter (
			@NonNull FormatWriter formatWriter) {

		if (
			isNotNull (
				currentFormatWriterThreadLocal.get ())
		) {
			throw new IllegalStateException ();
		}

		currentFormatWriterThreadLocal.set (
			formatWriter);

	}

	@SuppressWarnings ("resource")
	public static
	void clearCurrentFormatWriter (
			@NonNull FormatWriter formatWriter) {

		FormatWriter currentFormatWriter =
			currentFormatWriterThreadLocal.get ();

		if (

			isNull (
				currentFormatWriter)

			|| referenceNotEqualWithClass (
				FormatWriter.class,
				currentFormatWriter,
				formatWriter)
		) {
			throw new IllegalStateException ();
		}

		currentFormatWriterThreadLocal.remove ();

	}

	public static
	FormatWriter currentFormatWriter () {

		FormatWriter currentFormatWriter =
			currentFormatWriterThreadLocal.get ();

		if (
			isNull (
				currentFormatWriter)
		) {
			throw new IllegalStateException ();
		}

		return currentFormatWriter;

	}

	public static
	String formatWriterConsumerToString (
			@NonNull Consumer <FormatWriter> consumer) {

		try (

			StringFormatWriter formatWriter =
				new StringFormatWriter ();

		) {

			consumer.accept (
				formatWriter);

			return formatWriter.toString ();

		}

	}

}
