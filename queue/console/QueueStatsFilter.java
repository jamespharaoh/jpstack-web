package wbs.platform.queue.console;

import static wbs.utils.collection.CollectionUtils.collectionDoesNotHaveTwoElements;
import static wbs.utils.collection.CollectionUtils.listFirstElementRequired;
import static wbs.utils.collection.CollectionUtils.listSecondElementRequired;
import static wbs.utils.collection.IterableUtils.iterableMap;
import static wbs.utils.collection.MapUtils.mapItemForKeyRequired;
import static wbs.utils.etc.Misc.doesNotContain;
import static wbs.utils.etc.OptionalUtils.optionalAbsent;
import static wbs.utils.etc.OptionalUtils.optionalGetRequired;
import static wbs.utils.etc.OptionalUtils.optionalIsNotPresent;
import static wbs.utils.etc.OptionalUtils.optionalIsPresent;
import static wbs.utils.etc.OptionalUtils.optionalOf;
import static wbs.utils.etc.OptionalUtils.presentInstances;
import static wbs.utils.string.StringUtils.stringSplitFullStop;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableSet;

import lombok.NonNull;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.NestedTransaction;
import wbs.framework.database.Transaction;
import wbs.framework.entity.record.GlobalId;
import wbs.framework.logging.LogContext;
import wbs.framework.object.ObjectManager;

import wbs.platform.object.core.model.ObjectTypeObjectHelper;
import wbs.platform.object.core.model.ObjectTypeRec;
import wbs.platform.queue.model.QueueItemRec;
import wbs.platform.queue.model.QueueObjectHelper;
import wbs.platform.queue.model.QueueRec;
import wbs.platform.queue.model.QueueTypeObjectHelper;
import wbs.platform.queue.model.QueueTypeRec;
import wbs.platform.scaffold.model.SliceObjectHelper;
import wbs.platform.scaffold.model.SliceRec;

@PrototypeComponent ("queueStatsFilter")
public
class QueueStatsFilter {

	// singleton dependencies

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	ObjectManager objectManager;

	@SingletonDependency
	ObjectTypeObjectHelper objectTypeHelper;

	@SingletonDependency
	QueueObjectHelper queueHelper;

	@SingletonDependency
	QueueTypeObjectHelper queueTypeHelper;

	@SingletonDependency
	SliceObjectHelper sliceHelper;

	// state

	Optional <Set <SliceRec>> slices;
	Optional <Set <QueueTypeRec>> queueTypes;

	// implementation

	public
	void conditions (
			@NonNull Transaction parentTransaction,
			@NonNull Map <String, Set <String>> conditions) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"conditions");

		) {

			// handle slice code

			if (conditions.containsKey ("sliceCode")) {

				Set <String> sliceCodes =
					mapItemForKeyRequired (
						conditions,
						"sliceCode");

				slices =
					optionalOf (
						ImmutableSet.copyOf (
							presentInstances (
								iterableMap (
									sliceCodes,
									sliceCode ->
										sliceHelper.findByCode (
											transaction,
											GlobalId.root,
											sliceCode)))));

			} else {

				slices =
					optionalAbsent ();

			}

			// handle queue type code

			if (conditions.containsKey ("queueTypeCode")) {

				Set <String> queueTypeCodes =
					mapItemForKeyRequired (
						conditions,
						"queueTypeCode");

				queueTypes =
					optionalOf (
						ImmutableSet.copyOf (
							presentInstances (
								iterableMap (
									queueTypeCodes,
									queueTypeCode ->
										this.lookupQueueType (
											transaction,
											queueTypeCode)))));

			} else {

				queueTypes =
					optionalAbsent ();

			}

		}

	}

	public
	boolean filterQueue (
			@NonNull Transaction parentTransaction,
			@NonNull QueueRec queue) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"filterQueue");

		) {

			// filter by slice

			if (

				optionalIsPresent (
					slices)

				&& doesNotContain (
					optionalGetRequired (
						slices),
					queue.getSlice ())

			) {
				return false;
			}

			// filter by queue type

			if (

				optionalIsPresent (
					queueTypes)

				&& doesNotContain (
					optionalGetRequired (
						queueTypes),
					queue.getQueueType ())

			) {
				return false;
			}

			// return success

			return true;

		}

	}

	public
	List <QueueItemRec> filterQueueItems (
			@NonNull Transaction parentTransaction,
			@NonNull List <QueueItemRec> allQueueItems) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"filterQueueItems");

		) {

			List <QueueItemRec> filteredQueueItems =
				new ArrayList<> ();

			for (
				QueueItemRec queueItem
					: allQueueItems
			) {

				QueueRec queue =
					queueItem.getQueueSubject ().getQueue ();

				if (
					! filterQueue (
						transaction,
						queue)
				) {
					continue;
				}

				filteredQueueItems.add (
					queueItem);

			}

			return filteredQueueItems;

		}

	}

	// private implementation

	private
	Optional <QueueTypeRec> lookupQueueType (
			@NonNull Transaction parentTransaction,
			@NonNull String queueTypeCodeCombined) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"lookupQueueType");

		) {

			// deconstruct combined queue type code

			List <String> queueTypeCodeParts =
				stringSplitFullStop (
					queueTypeCodeCombined);

			if (
				collectionDoesNotHaveTwoElements (
					queueTypeCodeParts)
			) {
				throw new IllegalArgumentException ();
			}

			String parentTypeCode =
				listFirstElementRequired (
					queueTypeCodeParts);

			String queueTypeCode =
				listSecondElementRequired (
					queueTypeCodeParts);

			// lookup parent type

			Optional <ObjectTypeRec> parentTypeOptional =
				objectTypeHelper.findByCode (
					transaction,
					GlobalId.root,
					parentTypeCode);

			if (
				optionalIsNotPresent (
					parentTypeOptional)
			) {
				return optionalAbsent ();
			}

			ObjectTypeRec parentType =
				optionalGetRequired (
					parentTypeOptional);

			// lookup queue type

			return queueTypeHelper.findByCode (
				transaction,
				parentType,
				queueTypeCode);

		}

	}

}
