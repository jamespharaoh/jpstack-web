package wbs.console.forms.object;

import static wbs.utils.etc.LogicUtils.referenceEqualWithClass;
import static wbs.utils.etc.OptionalUtils.optionalAbsent;
import static wbs.utils.etc.OptionalUtils.optionalGetRequired;
import static wbs.utils.etc.OptionalUtils.optionalIsPresent;
import static wbs.utils.etc.OptionalUtils.optionalOfFormat;
import static wbs.utils.etc.TypeUtils.genericCastUnchecked;
import static wbs.utils.string.StringUtils.camelToSpaces;

import java.util.Set;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableSet;

import lombok.NonNull;

import wbs.console.forms.types.FormFieldConstraintValidator;
import wbs.console.helper.core.ConsoleHelper;
import wbs.console.helper.manager.ConsoleObjectManager;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.WeakSingletonDependency;
import wbs.framework.database.NestedTransaction;
import wbs.framework.database.Transaction;
import wbs.framework.entity.model.ModelFieldType;
import wbs.framework.entity.record.Record;
import wbs.framework.logging.LogContext;

@PrototypeComponent ("codeFormFieldConstraintValidator")
public
class CodeFormFieldConstraintValidator <
	Container extends Record <Container>
>
	implements FormFieldConstraintValidator <Container, String> {

	// singleton dependencies

	@ClassSingletonDependency
	LogContext logContext;

	@WeakSingletonDependency
	ConsoleObjectManager objectManager;

	// transaction

	@Override
	public
	Optional <String> validate (
			@NonNull Transaction parentTransaction,
			@NonNull Container container,
			@NonNull Optional <String> nativeValue) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"validate");

		) {

			String code =
				optionalGetRequired (
					nativeValue);

			ConsoleHelper <Container> consoleHelper =
				objectManager.consoleHelperForObjectRequired (
					container);

			if (consoleHelper.nameExists ()) {
				return optionalAbsent ();
			}

			// check for duplicate with parent

			if (

				referenceEqualWithClass (
					Set.class,
					ImmutableSet.of (
						ModelFieldType.parent,
						ModelFieldType.code),
					consoleHelper.objectModel ().identityFieldTypes ())

				|| referenceEqualWithClass (
					Set.class,
					ImmutableSet.of (
						ModelFieldType.code),
					consoleHelper.objectModel ().identityFieldTypes ())

			) {

				// check for duplicate

				Record <?> parent =
					consoleHelper.getParentRequired (
						transaction,
						container);

				ConsoleHelper <?> parentHelper =
					objectManager.consoleHelperForObjectRequired (
						genericCastUnchecked (
							parent));

				if (
					optionalIsPresent (
						parentHelper.findByCode (
							transaction,
							parent,
							code))
				) {

					return optionalOfFormat (
						"There is already a %s ",
						camelToSpaces (
							consoleHelper.objectName ()),
						"with parent %s ",
						objectManager.objectPath (
							transaction,
							parent),
						"and code %s",
						code);

				}

			}

			// return ok

			return optionalAbsent ();

		}

	}

}
