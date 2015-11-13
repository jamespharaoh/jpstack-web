package wbs.framework.record;

import static wbs.framework.utils.etc.Misc.contains;
import static wbs.framework.utils.etc.Misc.doesNotContain;
import static wbs.framework.utils.etc.Misc.isNotEmpty;
import static wbs.framework.utils.etc.Misc.isNull;
import static wbs.framework.utils.etc.Misc.notEqual;
import static wbs.framework.utils.etc.Misc.stringFormat;

import java.util.Set;

import lombok.NonNull;
import lombok.extern.log4j.Log4j;

import com.google.common.collect.Sets;

@Log4j
public
class UnsavedRecordDetector {

	public final static
	UnsavedRecordDetector instance =
		 new UnsavedRecordDetector ();

	private
	ThreadLocal<Frame> frameThreadLocal =
		new ThreadLocal<Frame> ();

	public
	void createFrame (
			@NonNull Object reference) {

		Frame newFrame =
			new Frame ();

		newFrame.previousFrame =
			frameThreadLocal.get ();

		newFrame.reference =
			reference;

		frameThreadLocal.set (
			newFrame);

	}

	public
	void verifyFrame (
			@NonNull Object reference) {

		Frame currentFrame =
			frameThreadLocal.get ();

		if (
			notEqual (
				currentFrame.reference,
				reference)
		) {

			throw new IllegalStateException ();

		}

		if (
			isNotEmpty (
				currentFrame.unsavedRecords)
		) {

			for (
				Record<?> unsavedRecord
					: currentFrame.unsavedRecords
			) {

				log.error (
					stringFormat (
						"Unsaved record %s",
						unsavedRecord));

			}

			throw new IllegalStateException (
				stringFormat (
					"%s unsaved records",
					currentFrame.unsavedRecords.size ()));

		}

	}

	public
	void destroyFrame (
			@NonNull Object reference) {

		Frame currentFrame =
			frameThreadLocal.get ();

		if (
			isNull (
				currentFrame)
		) {

			throw new IllegalStateException (
				"No current frame");

		}

		if (
			notEqual (
				currentFrame.reference,
				reference)
		) {

			throw new IllegalStateException ();

		}

		frameThreadLocal.set (
			currentFrame.previousFrame);

	}

	public
	void addRecord (
			@NonNull Record<?> record) {

		Frame currentFrame =
			frameThreadLocal.get ();

		if (
			isNull (
				currentFrame)
		) {

			throw new IllegalStateException ();

		}

		if (
			contains (
				currentFrame.unsavedRecords,
				record)
		) {

			throw new IllegalStateException ();

		}

		currentFrame.unsavedRecords.add (
			record);

	}

	public
	void removeRecord (
			@NonNull Record<?> record) {

		Frame currentFrame =
			frameThreadLocal.get ();

		if (
			isNull (
				currentFrame)
		) {

			throw new IllegalStateException ();

		}

		if (
			doesNotContain (
				currentFrame.unsavedRecords,
				record)
		) {

			throw new IllegalStateException ();

		}

		currentFrame.unsavedRecords.remove (
			record);

	}

	private static
	class Frame {

		Frame previousFrame;

		Object reference;

		Set<Record<?>> unsavedRecords =
			Sets.newIdentityHashSet ();

	}

}
