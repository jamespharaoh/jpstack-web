package wbs.framework.entity.record;

import static wbs.utils.collection.CollectionUtils.collectionIsNotEmpty;
import static wbs.utils.etc.LogicUtils.referenceNotEqualWithClass;
import static wbs.utils.etc.Misc.contains;
import static wbs.utils.etc.Misc.doesNotContain;
import static wbs.utils.etc.Misc.isNull;
import static wbs.utils.string.StringUtils.stringFormat;

import java.util.Set;

import com.google.common.collect.Sets;

import lombok.NonNull;
import lombok.extern.log4j.Log4j;

@Log4j
public
class UnsavedRecordDetector {

	public final static
	UnsavedRecordDetector instance =
		 new UnsavedRecordDetector ();

	private
	ThreadLocal <Frame> frameThreadLocal =
		new ThreadLocal<> ();

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
			referenceNotEqualWithClass (
				Object.class,
				currentFrame.reference,
				reference)
		) {

			throw new IllegalStateException ();

		}

		if (
			collectionIsNotEmpty (
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
			referenceNotEqualWithClass (
				Object.class,
				currentFrame.reference,
				reference)
		) {

			throw new IllegalStateException (
				"Reference mismatch");

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

			throw new IllegalStateException (
				stringFormat (
					"Record to remove is not in list: %s",
					record));

		}

		currentFrame.unsavedRecords.remove (
			record);

	}

	private static
	class Frame {

		Frame previousFrame;

		Object reference;

		Set <Record <?>> unsavedRecords =
			Sets.newIdentityHashSet ();

	}

}
