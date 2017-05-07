package wbs.framework.component.tools;

import static wbs.utils.collection.CollectionUtils.collectionHasMoreThanOneElement;
import static wbs.utils.collection.CollectionUtils.collectionIsEmpty;
import static wbs.utils.collection.CollectionUtils.emptyList;
import static wbs.utils.collection.CollectionUtils.listFirstElementRequired;
import static wbs.utils.collection.IterableUtils.iterableMap;
import static wbs.utils.etc.Misc.isNotNull;
import static wbs.utils.etc.Misc.shouldNeverHappen;
import static wbs.utils.etc.OptionalUtils.optionalAbsent;
import static wbs.utils.etc.OptionalUtils.optionalFromNullable;
import static wbs.utils.etc.OptionalUtils.optionalOf;
import static wbs.utils.etc.OptionalUtils.presentInstances;
import static wbs.utils.etc.TypeUtils.classNameFull;
import static wbs.utils.etc.TypeUtils.classNameSimple;
import static wbs.utils.etc.TypeUtils.genericCastUnchecked;
import static wbs.utils.string.StringUtils.joinWithCommaAndSpace;
import static wbs.utils.string.StringUtils.stringFormat;

import java.lang.annotation.Annotation;
import java.util.List;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;

import lombok.NonNull;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.ProxiedRequestComponent;
import wbs.framework.component.annotations.SingletonComponent;
import wbs.framework.component.registry.ComponentDefinition;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.OwnedTaskLogger;
import wbs.framework.logging.TaskLogger;

@SingletonComponent ("annotatedClassComponentTools")
public
class AnnotatedClassComponentTools {

	// state

	@ClassSingletonDependency
	LogContext logContext;

	// implementation

	public
	Optional <ComponentDefinition> definitionForClass (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull Class <?> componentClass) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"definitionForClass");

		) {

			List <ComponentDefinition> definitions =
				definitionsForClass (
					taskLogger,
					componentClass);

			if (
				collectionIsEmpty (
					definitions)
			) {
				return optionalAbsent ();
			}

			if (
				collectionHasMoreThanOneElement (
					definitions)
			) {
				throw new RuntimeException ();
			}

			return optionalOf (
				listFirstElementRequired (
					definitions));

		}

	}

	public
	List <ComponentDefinition> definitionsForClass (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull Class <?> componentClass) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"definitionsForClass");

		) {

			// check for annotations

			SingletonComponent singletonComponent =
				componentClass.getAnnotation (
					SingletonComponent.class);

			PrototypeComponent prototypeComponent =
				componentClass.getAnnotation (
					PrototypeComponent.class);

			ProxiedRequestComponent proxiedRequestComponent =
				componentClass.getAnnotation (
					ProxiedRequestComponent.class);

			List <Annotation> presentAnnotations =
				genericCastUnchecked (
					ImmutableList.of (
						presentInstances (
							optionalFromNullable (
								singletonComponent),
							optionalFromNullable (
								prototypeComponent),
							optionalFromNullable (
								proxiedRequestComponent))));

			if (
				collectionIsEmpty (
					presentAnnotations)
			) {

				taskLogger.errorFormat (
					"Could not find component annotation on %s",
					componentClass.getName ());

				return emptyList ();

			}

			if (
				collectionHasMoreThanOneElement (
					presentAnnotations)
			) {

				taskLogger.errorFormat (
					"Component class %s ",
					classNameFull (
						componentClass),
					"has multiple component annotations: %s",
					joinWithCommaAndSpace (
						iterableMap (
							annotation ->
								classNameSimple (
									annotation.getClass ()),
							presentAnnotations)));

			}

			// handle singleton componentannotation

			if (
				isNotNull (
					singletonComponent)
			) {

				String componentName =
					singletonComponent.value ();

				return ImmutableList.of (

					new ComponentDefinition ()

						.name (
							componentName)

						.componentClass (
							componentClass)

						.scope (
							"singleton")

						.fromAnnotatedClass (
							true)

				);

			}

			// handle prototype component

			if (
				isNotNull (
					prototypeComponent)
			) {

				String componentName =
					prototypeComponent.value ();

				return ImmutableList.of (

					new ComponentDefinition ()

						.name (
							componentName)

						.componentClass (
							componentClass)

						.scope (
							"prototype")

						.fromAnnotatedClass (
							true)

				);

			}

			// handle proxied request component

			if (
				isNotNull (
					proxiedRequestComponent)
			) {

				String componentName =
					proxiedRequestComponent.value ();

				String targetComponentName =
					stringFormat (
						"%sTarget",
						componentName);

				return ImmutableList.of (

					new ComponentDefinition ()

						.name (
							targetComponentName)

						.componentClass (
							componentClass)

						.scope (
							"prototype")

						.hide (
							true)

						.fromAnnotatedClass (
							true)

					,

					new ComponentDefinition ()

						.name (
							componentName)

						.componentClass (
							proxiedRequestComponent.proxyInterface ())

						.factoryClass (
							genericCastUnchecked (
								ThreadLocalProxyComponentFactory.class))

						.scope (
							"singleton")

						.addValueProperty (
							"componentName",
							componentName)

						.addValueProperty (
							"componentClass",
							proxiedRequestComponent.proxyInterface ())

				);

			}

			throw shouldNeverHappen ();

		}

	}

}
