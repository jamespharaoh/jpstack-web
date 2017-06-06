package wbs.console.formaction;

import static wbs.utils.collection.CollectionUtils.emptyList;
import static wbs.utils.collection.MapUtils.emptyMap;
import static wbs.utils.etc.DebugUtils.debugFormat;
import static wbs.utils.etc.Misc.doNothing;
import static wbs.utils.etc.OptionalUtils.optionalFromJava;
import static wbs.utils.etc.OptionalUtils.optionalGetRequired;
import static wbs.utils.etc.OptionalUtils.optionalIsNotPresent;
import static wbs.utils.etc.TypeUtils.classAllGenericInterfaces;
import static wbs.utils.etc.TypeUtils.classInstantiate;
import static wbs.utils.etc.TypeUtils.classNameFull;
import static wbs.utils.etc.TypeUtils.genericCastUnchecked;
import static wbs.utils.etc.TypeUtils.isSubclassOf;
import static wbs.utils.string.StringUtils.objectToString;
import static wbs.utils.string.StringUtils.stringFormat;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

import com.google.common.base.Optional;

import lombok.Data;
import lombok.NonNull;
import lombok.experimental.Accessors;

import wbs.framework.database.Transaction;

import wbs.utils.string.FormatWriter;

import wbs.web.responder.Responder;

public
interface ConsoleFormActionHelper <FormState, History> {

	default
	Permissions canBePerformed (
			@NonNull Transaction parentTransaction) {

		return new Permissions ()
			.canView (true)
			.canPerform (true);

	}

	default
	void writePreamble (
			@NonNull Transaction parentTransaction,
			@NonNull FormatWriter formatWriter,
			@NonNull Boolean submit) {

		doNothing ();

	}

	default
	Map <String, String> placeholderValues () {

		return emptyMap ();

	}

	default
	FormState constructFormState (
			@NonNull Transaction parentTransaction) {

		debugFormat (
			"# START #");

		for (
			Type interfaceClass
				: classAllGenericInterfaces (
					getClass ())
		) {

			debugFormat (
				"Class %s interface %s",
				classNameFull (
					getClass ()),
				objectToString (
					interfaceClass));

		}

		debugFormat (
			"# END #");

		Optional <Class <?>> formStateClassOptional =
			optionalFromJava (
				classAllGenericInterfaces (
					getClass ()).stream ()

			.map (
				interfaceType ->
					(ParameterizedType)
					interfaceType)

			.filter (
				interfaceType ->
					isSubclassOf (
						ConsoleFormActionHelper.class,
						(Class <?>) interfaceType.getRawType ()))

			.map (
				interfaceParameterizedType ->
					interfaceParameterizedType
						.getActualTypeArguments () [0])

			.map (
				typeArgumentType ->
					(Class <?>) typeArgumentType)

			.findFirst ()

		);

		if (
			optionalIsNotPresent (
				formStateClassOptional)
		) {

			throw new ClassCastException (
				stringFormat (
					"Can't determine form state for %s",
					classNameFull (
						getClass ())));

		}

		Class <?> formStateClass =
			optionalGetRequired (
				formStateClassOptional);

		return genericCastUnchecked (
			classInstantiate (
				formStateClass));

	}

	default
	Map <String, Object> formHints (
			@NonNull Transaction parentTransaction) {

		return emptyMap ();

	}

	default
	void updatePassiveFormState (
			@NonNull Transaction parentTransaction,
			@NonNull FormState formState) {

		doNothing ();

	}

	Optional <Responder> processFormSubmission (
			Transaction parentTransaction,
			FormState formState);

	default
	List <History> history (
			@NonNull Transaction parentTransaction) {

		return emptyList ();

	}

	@Accessors (fluent = true)
	@Data
	public static
	class Permissions {

		Boolean canView;
		Boolean canPerform;

	}

}
