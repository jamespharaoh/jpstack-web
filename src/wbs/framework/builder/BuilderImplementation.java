package wbs.framework.builder;

import static wbs.utils.etc.OptionalUtils.optionalOf;
import static wbs.utils.etc.ReflectionUtils.fieldSet;
import static wbs.utils.etc.ReflectionUtils.methodInvoke;
import static wbs.utils.string.StringUtils.stringFormat;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.logging.TaskLogger;

@PrototypeComponent ("builderImplementation")
@Accessors (fluent = true)
public
class BuilderImplementation <Context extends TaskLogger>
	implements Builder <Context> {

	// properties

	@Getter @Setter
	Class <Context> contextClass;

	@Getter @Setter
	List <BuilderInfo> builderInfos;

	// implementation

	@Override
	public
	void descend (
			@NonNull Context context,
			Object parentObject,
			List <?> sourceObjects,
			Object targetObject,
			MissingBuilderBehaviour missingBuilderBehaviour) {

		for (
			Object sourceObject
				: sourceObjects
		) {

			build (
				context,
				parentObject,
				sourceObject,
				targetObject,
				missingBuilderBehaviour);

		}

	}

	void build (
			@NonNull Context context,
			@NonNull Object parentObject,
			@NonNull Object sourceObject,
			@NonNull Object targetObject,
			@NonNull MissingBuilderBehaviour missingBuilderBehaviour) {

		// select builder

		BuilderInfo builderInfo =
			selectBuilder (
				parentObject.getClass (),
				sourceObject.getClass ());

		if (builderInfo == null) {

			switch (missingBuilderBehaviour) {

			case error:

				throw new RuntimeException (
					stringFormat (
						"No builder found for parent %s ",
						parentObject.getClass ().getSimpleName (),
						"and source %s",
						sourceObject.getClass ().getSimpleName ()));

			case ignore:

				return;

			default:

				throw new RuntimeException ();

			}

		}

		// instantiate

		Object builderObject =
			builderInfo.builderProvider.provide (
				context);

		// inject context

		fieldSet (
			builderInfo.parentField,
			builderObject,
			optionalOf (
				parentObject));

		fieldSet (
			builderInfo.sourceField,
			builderObject,
			optionalOf (
				sourceObject));

		fieldSet (
			builderInfo.targetField,
			builderObject,
			optionalOf (
				targetObject));

		// call builder

		methodInvoke (
			builderInfo.buildMethod,
			builderObject,
			context,
			this);

	}

	// private implementation

	BuilderInfo selectBuilder (
			@NonNull Class <?> parentClass,
			@NonNull Class <?> sourceClass) {

		List <BuilderInfo> candidateBuilderInfos =
			new ArrayList<> ();

		for (
			BuilderInfo builderInfo
				: builderInfos
		) {

			if (! builderInfo.parentField.getType ().isAssignableFrom (
					parentClass))
				continue;

			if (! builderInfo.sourceField.getType ().isAssignableFrom (
					sourceClass))
				continue;

			candidateBuilderInfos.add (
				builderInfo);

		}

		if (candidateBuilderInfos.isEmpty ()) {

			/*
			log.warn (sf (
				"No builder for %s with parent %s",
				sourceClass.getSimpleName (),
				parentClass.getSimpleName ()));
			*/

			return null;

		}

		if (candidateBuilderInfos.size () > 1) {

			throw new RuntimeException (
				stringFormat (
					"Multiple builders for %s with parent %s",
					sourceClass.getSimpleName (),
					parentClass.getSimpleName ()));

		}

		return candidateBuilderInfos.get (0);

	}

}
