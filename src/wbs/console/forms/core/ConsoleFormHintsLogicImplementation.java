package wbs.console.forms.core;

import static wbs.utils.etc.OptionalUtils.optionalGetRequired;
import static wbs.utils.etc.OptionalUtils.optionalIsPresent;
import static wbs.utils.string.StringUtils.joinWithFullStop;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;

import lombok.NonNull;

import wbs.console.helper.core.ConsoleHelper;
import wbs.console.helper.manager.ConsoleObjectManager;
import wbs.console.request.ConsoleRequestContext;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.SingletonComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.NestedTransaction;
import wbs.framework.database.Transaction;
import wbs.framework.entity.record.Record;
import wbs.framework.logging.LogContext;

@SingletonComponent ("consoleFormHintsLogic")
public
class ConsoleFormHintsLogicImplementation
	implements ConsoleFormHintsLogic {

	// singleton dependencies

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	ConsoleObjectManager objectManager;

	@SingletonDependency
	ConsoleRequestContext requestContext;

	// public implementation

	@Override
	public
	void prepareParentHints (
			@NonNull Transaction parentTransaction,
			@NonNull ImmutableMap.Builder <String, Object> formHintsBuilder,
			@NonNull ConsoleHelper <?> objectHelper) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"prepareParentHints");

		) {

			if (! objectHelper.parentExists ()) {
				return;
			}

			ConsoleHelper <?> parentHelper =
				objectManager.consoleHelperForClassRequired (
					objectHelper.parentClassRequired ());

			Optional <Long> parentIdOptional =
				requestContext.stuffInteger (
					parentHelper.idKey ());

			if (
				optionalIsPresent (
					parentIdOptional)
			) {

				Record <?> parent =
					parentHelper.findRequired (
						transaction,
						optionalGetRequired (
							parentIdOptional));

				formHintsBuilder.put (
					objectHelper.parentFieldName (),
					parent);

				formHintsBuilder.put (
					"parent",
					parent);

			}

			prepareGrandparentHints (
				transaction,
				formHintsBuilder,
				objectHelper,
				parentHelper);

		}

	}

	@Override
	public
	void prepareGrandparentHints (
			@NonNull Transaction parentTransaction,
			@NonNull ImmutableMap.Builder <String, Object> formHintsBuilder,
			@NonNull ConsoleHelper <?> objectHelper,
			@NonNull ConsoleHelper <?> parentHelper) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"prepareGrandparentHints");

		) {

			if (! parentHelper.parentExists ()) {
				return;
			}

			ConsoleHelper <?> grandparentHelper =
				objectManager.consoleHelperForClassRequired (
					parentHelper.parentClassRequired ());

			Optional <Long> grandparentIdOptional =
				requestContext.stuffInteger (
					grandparentHelper.idKey ());

			if (
				optionalIsPresent (
					grandparentIdOptional)
			) {

				Record <?> grandparent =
					grandparentHelper.findRequired (
						transaction,
						optionalGetRequired (
							grandparentIdOptional));

				formHintsBuilder.put (
					joinWithFullStop (
						objectHelper.parentFieldName (),
						parentHelper.parentFieldName ()),
					grandparent);

				formHintsBuilder.put (
					joinWithFullStop (
						"parent",
						parentHelper.parentFieldName ()),
					grandparent);

				formHintsBuilder.put (
					joinWithFullStop (
						objectHelper.parentFieldName (),
						"parent"),
					grandparent);

				formHintsBuilder.put (
					"grandparent",
					grandparent);

				formHintsBuilder.put (
					"parent.parent",
					grandparent);

			}

			prepareGreatGrandparentHints (
				transaction,
				formHintsBuilder,
				objectHelper,
				parentHelper,
				grandparentHelper);

		}

	}

	@Override
	public
	void prepareGreatGrandparentHints (
			@NonNull Transaction parentTransaction,
			@NonNull ImmutableMap.Builder <String, Object> formHintsBuilder,
			@NonNull ConsoleHelper <?> objectHelper,
			@NonNull ConsoleHelper <?> parentHelper,
			@NonNull ConsoleHelper <?> grandparentHelper) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"prepareGreatGrandparentHints");

		) {

			if (! grandparentHelper.parentExists ()) {
				return;
			}

			ConsoleHelper <?> greatGrandparentHelper =
				objectManager.consoleHelperForClassRequired (
					grandparentHelper.parentClassRequired ());

			Optional <Long> greatGrandparentIdOptional =
				requestContext.stuffInteger (
					greatGrandparentHelper.idKey ());

			if (
				optionalIsPresent (
					greatGrandparentIdOptional)
			) {

				Record <?> greatGrandparent =
					greatGrandparentHelper.findRequired (
						transaction,
						optionalGetRequired (
							greatGrandparentIdOptional));

				formHintsBuilder.put (
					joinWithFullStop (
						objectHelper.parentFieldName (),
						parentHelper.parentFieldName (),
						grandparentHelper.parentFieldName ()),
					greatGrandparent);

				formHintsBuilder.put (
					joinWithFullStop (
						"parent",
						parentHelper.parentFieldName (),
						grandparentHelper.parentFieldName ()),
					greatGrandparent);

				formHintsBuilder.put (
					joinWithFullStop (
						objectHelper.parentFieldName (),
						"parent",
						grandparentHelper.parentFieldName ()),
					greatGrandparent);

				formHintsBuilder.put (
					joinWithFullStop (
						"parent",
						"parent",
						grandparentHelper.parentFieldName ()),
					greatGrandparent);

				formHintsBuilder.put (
					joinWithFullStop (
						objectHelper.parentFieldName (),
						parentHelper.parentFieldName (),
						"parent"),
					greatGrandparent);

				formHintsBuilder.put (
					joinWithFullStop (
						"parent",
						parentHelper.parentFieldName (),
						"parent"),
					greatGrandparent);

				formHintsBuilder.put (
					joinWithFullStop (
						objectHelper.parentFieldName (),
						"parent",
						"parent"),
					greatGrandparent);

				formHintsBuilder.put (
					joinWithFullStop (
						"parent",
						"parent",
						"parent"),
					greatGrandparent);

				formHintsBuilder.put (
					joinWithFullStop (
						"grandparent",
						grandparentHelper.parentFieldName ()),
					greatGrandparent);

				formHintsBuilder.put (
					joinWithFullStop (
						"grandparent",
						"parent"),
					greatGrandparent);

				formHintsBuilder.put (
					joinWithFullStop (
						objectHelper.parentFieldName (),
						"grandparent"),
					greatGrandparent);

				formHintsBuilder.put (
					joinWithFullStop (
						"parent",
						"grandparent"),
					greatGrandparent);

				formHintsBuilder.put (
					"greatgrandparent",
					greatGrandparent);

			}

		}

	}

}
