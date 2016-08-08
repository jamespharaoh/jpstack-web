package wbs.framework.builder;

import static wbs.framework.utils.etc.StringUtils.stringFormat;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.inject.Provider;

import lombok.NonNull;

import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.builder.annotations.BuildMethod;
import wbs.framework.builder.annotations.BuilderParent;
import wbs.framework.builder.annotations.BuilderSource;
import wbs.framework.builder.annotations.BuilderTarget;

@PrototypeComponent ("builderFactory")
public
class BuilderFactoryImplementation
	implements BuilderFactory {

	List<BuilderInfo> builderInfos =
		new ArrayList<BuilderInfo> ();

	@Override
	public
	BuilderFactory addBuilder (
			Class<?> builderClass,
			Provider<?> builderProvider) {

		BuilderInfo builderInfo =
			new BuilderInfo ();

		builderInfo.builderClass =
			builderClass;

		builderInfo.builderProvider =
			builderProvider;

		for (
			Field field
				: builderClass.getDeclaredFields ()
		) {

			BuilderParent builderParentAnnotation =
				field.getAnnotation (
					BuilderParent.class);

			if (builderParentAnnotation != null) {

				if (builderInfo.parentField != null)
					throw new RuntimeException ();

				builderInfo.parentField =
					field;

			}

			BuilderSource builderSourceAnnotation =
				field.getAnnotation (
					BuilderSource.class);

			if (builderSourceAnnotation != null) {

				if (builderInfo.sourceField != null)
					throw new RuntimeException ();

				builderInfo.sourceField =
					field;

			}

			BuilderTarget builderTargetAnnotation =
				field.getAnnotation (
					BuilderTarget.class);

			if (builderTargetAnnotation != null) {

				if (builderInfo.targetField != null)
					throw new RuntimeException ();

				builderInfo.targetField =
					field;

			}

		}

		for (Method method
				: builderClass.getDeclaredMethods ()) {

			BuildMethod buildMethodAnnotation =
				method.getAnnotation (
					BuildMethod.class);

			if (buildMethodAnnotation == null)
				continue;

			if (builderInfo.buildMethod != null)
				throw new RuntimeException ();

			builderInfo.buildMethod =
				method;

		}

		if (builderInfo.parentField == null) {

			throw new RuntimeException (
				stringFormat (
					"No @BuilderParent field for %s",
					builderClass));

		}

		if (builderInfo.sourceField == null)
			throw new RuntimeException ();

		if (builderInfo.targetField == null)
			throw new RuntimeException ();

		if (builderInfo.buildMethod == null)
			throw new RuntimeException ();

		if (builderInfo.buildMethod.getParameterTypes ().length != 1) {

			throw new RuntimeException (
				stringFormat (
					"Build method %s.%s should have a single argument",
					builderClass.getSimpleName (),
					builderInfo.buildMethod.getName ()));

		}

		builderInfo.parentField.setAccessible (true);
		builderInfo.sourceField.setAccessible (true);
		builderInfo.targetField.setAccessible (true);

		builderInfos.add (
			builderInfo);

		return this;

	}

	@Override
	public
	BuilderFactory addBuilders (
			Map<Class<?>,Provider<Object>> buildersMap) {

		for (
			Map.Entry<Class<?>,Provider<Object>> builderEntry
				: buildersMap.entrySet ()
		) {

			addBuilder (
				builderEntry.getKey (),
				builderEntry.getValue ());

		}

		return this;

	}

	@Override
	public
	Builder create () {

		return new BuilderImplementation ();

	}

	BuilderInfo selectBuilder (
			Class<?> parentClass,
			Class<?> sourceClass) {

		List<BuilderInfo> candidateBuilderInfos =
			new ArrayList<BuilderInfo> ();

		for (BuilderInfo builderInfo
				: builderInfos) {

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

	public static
	class BuilderInfo {

		Class<?> builderClass;
		Provider<?> builderProvider;

		Field parentField;
		Field targetField;
		Field sourceField;

		Method buildMethod;

	}

	class BuilderImplementation
		implements Builder {

		@Override
		public
		void descend (
				Object parentObject,
				List<?> sourceObjects,
				Object targetObject,
				MissingBuilderBehaviour missingBuilderBehaviour) {

			for (
				Object sourceObject
					: sourceObjects
			) {

				build (
					parentObject,
					sourceObject,
					targetObject,
					missingBuilderBehaviour);

			}

		}

		void build (
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
				builderInfo.builderProvider.get ();

			try {

				// inject context

				builderInfo.parentField.set (
					builderObject,
					parentObject);

				builderInfo.sourceField.set (
					builderObject,
					sourceObject);

				builderInfo.targetField.set (
					builderObject,
					targetObject);

				// call builder

				builderInfo.buildMethod.invoke (
					builderObject,
					this);

			} catch (Exception exception) {

				throw new RuntimeException (exception);

			}

		}

	}

}
