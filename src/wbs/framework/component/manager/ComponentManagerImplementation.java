package wbs.framework.component.manager;

import static wbs.utils.collection.CollectionUtils.collectionDoesNotHaveOneElement;
import static wbs.utils.collection.CollectionUtils.collectionIsEmpty;
import static wbs.utils.collection.CollectionUtils.collectionIsNotEmpty;
import static wbs.utils.collection.CollectionUtils.listFirstElementRequired;
import static wbs.utils.collection.IterableUtils.iterableFilter;
import static wbs.utils.collection.IterableUtils.iterableMap;
import static wbs.utils.collection.IterableUtils.iterableMapToList;
import static wbs.utils.collection.MapUtils.iterableTransformToMap;
import static wbs.utils.collection.MapUtils.mapIsNotEmpty;
import static wbs.utils.collection.MapUtils.mapItemForKey;
import static wbs.utils.etc.EnumUtils.enumEqualSafe;
import static wbs.utils.etc.EnumUtils.enumNotEqualSafe;
import static wbs.utils.etc.Misc.doesNotContain;
import static wbs.utils.etc.Misc.fullClassName;
import static wbs.utils.etc.Misc.isNotNull;
import static wbs.utils.etc.Misc.isNull;
import static wbs.utils.etc.Misc.requiredValue;
import static wbs.utils.etc.Misc.todo;
import static wbs.utils.etc.NullUtils.ifNull;
import static wbs.utils.etc.NumberUtils.integerToDecimalString;
import static wbs.utils.etc.NumberUtils.notEqualToOne;
import static wbs.utils.etc.OptionalUtils.optionalAbsent;
import static wbs.utils.etc.OptionalUtils.optionalFromNullable;
import static wbs.utils.etc.OptionalUtils.optionalGetRequired;
import static wbs.utils.etc.OptionalUtils.optionalIsNotPresent;
import static wbs.utils.etc.OptionalUtils.optionalIsPresent;
import static wbs.utils.etc.OptionalUtils.optionalOf;
import static wbs.utils.etc.OptionalUtils.optionalOrElseOptional;
import static wbs.utils.etc.PropertyUtils.propertySetSimple;
import static wbs.utils.etc.ReflectionUtils.fieldSet;
import static wbs.utils.etc.ReflectionUtils.methodInvoke;
import static wbs.utils.etc.TypeUtils.classInstantiate;
import static wbs.utils.etc.TypeUtils.classNameFull;
import static wbs.utils.etc.TypeUtils.classNameSimple;
import static wbs.utils.etc.TypeUtils.classNotEqual;
import static wbs.utils.etc.TypeUtils.genericCastUnchecked;
import static wbs.utils.etc.TypeUtils.isNotInstanceOf;
import static wbs.utils.etc.TypeUtils.isNotSubclassOf;
import static wbs.utils.string.StringUtils.joinWithCommaAndSpace;
import static wbs.utils.string.StringUtils.keyEqualsClassSimple;
import static wbs.utils.string.StringUtils.keyEqualsString;
import static wbs.utils.string.StringUtils.stringEqualSafe;
import static wbs.utils.string.StringUtils.stringFormat;

import java.lang.annotation.Annotation;
import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import javax.inject.Provider;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.MapMaker;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

import org.apache.commons.lang3.tuple.Pair;

import wbs.framework.component.annotations.ComponentManagerShutdownBegun;
import wbs.framework.component.annotations.ComponentManagerStartupComplete;
import wbs.framework.component.annotations.LateLifecycleSetup;
import wbs.framework.component.annotations.NormalLifecycleSetup;
import wbs.framework.component.manager.ComponentMetaData.ComponentState;
import wbs.framework.component.registry.ComponentDefinition;
import wbs.framework.component.registry.ComponentRegistry;
import wbs.framework.component.registry.InjectedProperty;
import wbs.framework.component.registry.InjectedProperty.CollectionType;
import wbs.framework.component.tools.ComponentFactory;
import wbs.framework.component.tools.EasyReadWriteLock;
import wbs.framework.component.tools.EasyReadWriteLock.HeldLock;
import wbs.framework.component.tools.NoSuchComponentException;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.LoggedErrorsException;
import wbs.framework.logging.LoggingLogic;
import wbs.framework.logging.OwnedTaskLogger;
import wbs.framework.logging.TaskLogger;

import wbs.utils.etc.PropertyUtils;

@Accessors (fluent = true)
public
class ComponentManagerImplementation
	implements ComponentManager {

	// properties

	@Getter @Setter
	ComponentRegistry registry;

	// state

	LoggingLogic loggingLogic;

	LogContext logContext;

	EasyReadWriteLock lock =
		EasyReadWriteLock.instantiate ();

	State state =
		State.creation;

	Map <String, ComponentData> singletonComponents =
		new HashMap<> ();

	Set <String> singletonComponentsInCreation =
		new LinkedHashSet<> ();

	Set <String> singletonComponentsFailed =
		new HashSet<> ();

	Map <Object, ComponentData> componentDatas =
		new MapMaker ()
			.weakKeys ()
			.makeMap ();

	Map <String, List <Injection>> pendingInjectionsByDependencyName =
		new HashMap<> ();

	// constructors

	public
	ComponentManagerImplementation (
			@NonNull LoggingLogic loggingLogic) {

		this.loggingLogic =
			loggingLogic;

		logContext =
			loggingLogic.findOrCreateLogContext (
				classNameFull (
					getClass ()));

	}

	// component manager implementation

	@Override
	public
	List <String> requestComponentNames () {

		return registry.requestComponentNames ();

	}

	@Override
	public <ComponentType>
	Optional <ComponentType> getComponent (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull String componentName,
			@NonNull Class <ComponentType> componentClass) {

		TaskLogger taskLogger =
			logContext.nestTaskLogger (
				parentTaskLogger,
				"getComponent");

		try (

			HeldLock heldLock =
				lock.read ();

		) {

			Optional <ComponentDefinition> componentDefinitionOptional =
				registry.byName (
					componentName);

			if (
				optionalIsNotPresent (
					componentDefinitionOptional)
			) {

				return optionalAbsent ();

			}

			ComponentDefinition componentDefinition =
				optionalGetRequired (
					componentDefinitionOptional);

			return optionalOf (
				componentClass.cast (
					getComponent (
						taskLogger,
						componentDefinition,
						true)));

		}

	}

	@Override
	public <ComponentType>
	ComponentType getComponentRequired (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull String componentName,
			@NonNull Class <ComponentType> componentClass) {

		TaskLogger taskLogger =
			logContext.nestTaskLogger (
				parentTaskLogger,
				"getComponentRequired");

		try (

			HeldLock heldLock =
				lock.read ();

		) {

			Optional <ComponentDefinition> componentDefinitionOptional =
				registry.byName (
					componentName);

			if (
				optionalIsNotPresent (
					componentDefinitionOptional)
			) {

				throw new NoSuchComponentException (
					stringFormat (
						"Component definition with name %s does not exist",
						componentName));

			}

			ComponentDefinition componentDefinition =
				optionalGetRequired (
					componentDefinitionOptional);

			return componentClass.cast (
				getComponent (
					taskLogger,
					componentDefinition,
					true));

		}

	}

	@Override
	public <ComponentType>
	ComponentType getComponentOrElse (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull String componentName,
			@NonNull Class <ComponentType> componentClass,
			@NonNull Supplier <ComponentType> orElse) {

		TaskLogger taskLogger =
			logContext.nestTaskLogger (
				parentTaskLogger,
				"getComponentOrElse");

		try (

			HeldLock heldLock =
				lock.read ();

		) {

			Optional <ComponentDefinition> componentDefinitionOptional =
				registry.byName (
					componentName);

			if (
				optionalIsNotPresent (
					componentDefinitionOptional)
			) {
				return orElse.get ();
			}

			ComponentDefinition componentDefinition =
				optionalGetRequired (
					componentDefinitionOptional);

			return componentClass.cast (
				getComponent (
					taskLogger,
					componentDefinition,
					true));

		}

	}

	@Override
	public <ComponentType>
	Optional <Provider <ComponentType>> getComponentProvider (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull String componentName,
			@NonNull Class <ComponentType> componentClass) {

		TaskLogger taskLogger =
			logContext.nestTaskLogger (
				parentTaskLogger,
				"getComponentProviderRequired");

		try (

			HeldLock heldlock =
				lock.read ();

		) {

			Optional <ComponentDefinition> componentDefinitionOptional =
				registry.byName (
					componentName);

			if (
				optionalIsNotPresent (
					componentDefinitionOptional)
			) {
				return optionalAbsent ();
			}

			ComponentDefinition componentDefinition =
				optionalGetRequired (
					componentDefinitionOptional);

			if (
				isNotSubclassOf (
					componentClass,
					componentDefinition.componentClass ())
			) {

				throw new NoSuchComponentException (
					stringFormat (
						"Component definition with name %s ",
						componentName,
						"is of type %s ",
						componentDefinition.componentClass ().getName (),
						"instead of %s",
						componentClass.getName ()));

			}

			return optionalOf (
				genericCastUnchecked (
					getComponentProvider (
						taskLogger,
						componentDefinition)));

		}

	}

	@Override
	public <ComponentType>
	Provider <ComponentType> getComponentProviderRequired (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull String componentName,
			@NonNull Class <ComponentType> componentClass) {

		TaskLogger taskLogger =
			logContext.nestTaskLogger (
				parentTaskLogger,
				"getComponentProviderRequired");

		try (

			HeldLock heldlock =
				lock.read ();

		) {

			Optional <ComponentDefinition> componentDefinitionOptional =
				registry.byName (
					componentName);

			if (
				optionalIsNotPresent (
					componentDefinitionOptional)
			) {

				throw new NoSuchComponentException (
					stringFormat (
						"Component definition with name %s does not exist",
						componentName));

			}

			ComponentDefinition componentDefinition =
				optionalGetRequired (
					componentDefinitionOptional);

			if (
				isNotSubclassOf (
					componentClass,
					componentDefinition.componentClass ())
			) {

				throw new NoSuchComponentException (
					stringFormat (
						"Component definition with name %s ",
						componentName,
						"is of type %s ",
						componentDefinition.componentClass ().getName (),
						"instead of %s",
						componentClass.getName ()));

			}

			return genericCastUnchecked (
				getComponentProvider (
					taskLogger,
					componentDefinition));

		}

	}

	@Override
	public
	Map <String, Pair <Class <?>, Object>> allSingletonComponents (
			@NonNull TaskLogger parentTaskLogger) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"allSingletonComponents");

		) {

			throw todo ();

		}

	}

	// private implementation

	private
	Object getComponent (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull ComponentDefinition componentDefinition,
			@NonNull Boolean initialize) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"getComponent");

			HeldLock heldlock =
				lock.read ();

		) {

			if (
				stringEqualSafe (
					componentDefinition.scope (),
					"prototype")
			) {

				return getPrototypeComponent (
					taskLogger,
					componentDefinition,
					initialize);

			} else if (
				stringEqualSafe (
					componentDefinition.scope (),
					"singleton")
			) {

				return getSingletonComponent (
					taskLogger,
					componentDefinition,
					initialize);

			} else {

				throw taskLogger.fatalFormat (
					"Unrecognised scope %s for component %s",
					componentDefinition.scope (),
					componentDefinition.name ());

			}

		}

	}

	private
	Object getPrototypeComponent (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull ComponentDefinition componentDefinition,
			@NonNull Boolean initialize) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"getPrototypeComponent");

		) {

			ComponentData componentData =
				instantiateComponent (
					taskLogger,
					componentDefinition,
					initialize);

			Object component =
				optionalGetRequired (
					componentData.component ());

			if (
				optionalIsPresent (
					componentData.optionalComponent)
			) {

				componentData.weakComponent =
					new WeakReference<> (
						optionalGetRequired (
							componentData.optionalComponent));

				componentData.optionalComponent =
					optionalAbsent ();

			}

			return component;

		}

	}

	private
	Object getSingletonComponent (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull ComponentDefinition componentDefinition,
			@NonNull Boolean initialize) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"getSingletonComponent");

		) {

			if (! initialize) {
				throw new IllegalArgumentException ();
			}

			Optional <ComponentData> componentDataOptional =
				mapItemForKey (
					singletonComponents,
					componentDefinition.name ());

			ComponentData componentData;

			if (
				optionalIsPresent (
					componentDataOptional)
			) {

				componentData =
					optionalGetRequired (
						componentDataOptional);

			} else {

				if (
					singletonComponentsInCreation.contains (
						componentDefinition.name ())
				) {

					throw taskLogger.fatalFormat (
						"Singleton component %s already in creation (%s)",
						componentDefinition.name (),
						joinWithCommaAndSpace (
							singletonComponentsInCreation));

				}

				if (
					singletonComponentsFailed.contains (
						componentDefinition.name ())
				) {

					throw taskLogger.fatalFormat (
						"Singleton component %s already failed",
						componentDefinition.name ());

				}

				singletonComponentsInCreation.add (
					componentDefinition.name ());

				componentData = null;

				try {

					componentData =
						instantiateComponent (
							taskLogger,
							componentDefinition,
							true);

					singletonComponents.put (
						componentDefinition.name (),
						componentData);

				} finally {

					singletonComponentsInCreation.remove (
						componentDefinition.name ());

					if (
						isNull (
							componentData)
					) {

						taskLogger.errorFormat (
							"Failed to instantiate component %s",
							componentDefinition.name ());

						singletonComponentsFailed.add (
							componentDefinition.name ());

					}

				}

			}

			return optionalGetRequired (
				componentData.component ());

		}

	}

	private
	ComponentData instantiateComponent (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull ComponentDefinition componentDefinition,
			@NonNull Boolean initialize) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLoggerFormat (
					parentTaskLogger,
					"instantiateComponent (%s)",
					componentDefinition.name ());

			HeldLock heldlock =
				lock.read ();

		) {

			// instantiate

			Object protoComponent =
				classInstantiate (
					ifNull (
						componentDefinition.factoryClass (),
						componentDefinition.componentClass ()));

			// set properties

			setComponentValueProperties (
				taskLogger,
				componentDefinition,
				protoComponent);

			setComponentReferenceProperties (
				taskLogger,
				componentDefinition,
				protoComponent);

			setComponentInjectedProperties (
				taskLogger,
				componentDefinition,
				protoComponent);

			// call factory

			Object component;
			ComponentData componentData;

			if (
				isNotNull (
					componentDefinition.factoryClass ())
			) {

				ComponentFactory <?> componentFactory =
					genericCastUnchecked (
						protoComponent);

				component =
					componentFactory.makeComponent (
						taskLogger);

				if (
					isNull (
						component)
				) {

					throw taskLogger.fatalFormat (
						"Factory component returned null for %s",
						componentDefinition.name ());

				}

				componentData =
					findOrCreateDataForComponent (
						componentDefinition,
						component);

			} else {

				component =
					protoComponent;

				componentData =
					findOrCreateDataForComponent (
						componentDefinition,
						component);

			}

			// initialize

			if (

				initialize

				&& enumEqualSafe (
					componentData.state (),
					ComponentState.uninitialized)

			) {

				initializeComponent (
					taskLogger,
					componentDefinition,
					component,
					componentData);

			}

			// and finish

			taskLogger.debugFormat (
				"Component %s instantiated successfully",
				componentDefinition.name ());

			return componentData;

		} catch (Exception exception) {

			throw new RuntimeException (
				stringFormat (
					"Error instantiating %s",
					componentDefinition.name ()),
				exception);

		}

	}

	private
	void initializeComponent (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull ComponentDefinition componentDefinition,
			@NonNull Object component,
			@NonNull ComponentData componentData) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"initializeComponent");

		) {

			synchronized (componentData) {

				if (
					enumNotEqualSafe (
						componentData.state (),
						ComponentState.uninitialized)
				) {

					throw new IllegalStateException (
						stringFormat (
							"Tried to initialize component %s ",
							componentData.definition.name (),
							"in %s state",
							componentData.state.name ()));

				}

				try {

					invokeLifecycleMethods (
						taskLogger,
						NormalLifecycleSetup.class,
						"Eager lifecycle setup",
						componentData);

					componentData.state =
						ComponentState.active;

				} finally {

					if (
						enumNotEqualSafe (
							componentData.state,
							ComponentState.active)
					) {

						componentData.state =
							ComponentState.error;

					}

				}

			}

		}

	}

	@Override
	public
	void bootstrapComponent (
			@NonNull Object component) {

		bootstrapComponent (
			component,
			registry.nameForAnnotatedClass (
				component.getClass ()));

	}

	@Override
	public
	void bootstrapComponent (
			@NonNull Object component,
			@NonNull String componentName) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.createTaskLogger (
					"bootstrapComponent");

		) {

			ComponentDefinition componentDefinition =
				registry.byNameRequired (
					componentName);

			if (
				isNotInstanceOf (
					componentDefinition.componentClass (),
					component)
			) {
				throw new ClassCastException ();
			}

			ComponentData componentData =
				findOrCreateDataForComponent (
					componentDefinition,
					component);

			// set properties

			setComponentValueProperties (
				taskLogger,
				componentDefinition,
				component);

			setComponentReferenceProperties (
				taskLogger,
				componentDefinition,
				component);

			setComponentInjectedProperties (
				taskLogger,
				componentDefinition,
				component);

			// initialize component

			initializeComponent (
				taskLogger,
				componentDefinition,
				component,
				componentData);

		}

	}

	private
	ComponentData findOrCreateDataForComponent (
			@NonNull ComponentDefinition componentDefinition,
			@NonNull Object component) {

		// create component info

		return componentDatas.computeIfAbsent (
			component,
			_component -> {

			ComponentData newComponentData =
				new ComponentData ();

			newComponentData.definition =
				componentDefinition;

			newComponentData.optionalComponent =
				optionalOf (
					component);

			newComponentData.state =
				componentDefinition.owned ()
					? ComponentState.uninitialized
					: ComponentState.unmanaged;

			return newComponentData;

		});

	}

	private
	void setComponentValueProperties (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull ComponentDefinition componentDefinition,
			@NonNull Object component) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"setComponentValueProperties");

			HeldLock heldlock =
				lock.read ();

		) {

			for (
				Map.Entry <String,Object> valuePropertyEntry
					: componentDefinition.valueProperties ().entrySet ()
			) {

				taskLogger.debugFormat (
					"Setting value property %s.%s",
					componentDefinition.name (),
					valuePropertyEntry.getKey ());

				propertySetSimple (
					component,
					valuePropertyEntry.getKey (),
					valuePropertyEntry.getValue ());

			}

		}

	}

	private
	void setComponentReferenceProperties (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull ComponentDefinition componentDefinition,
			@NonNull Object component) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"setComponentReferenceProperties");

			HeldLock heldlock =
				lock.read ();

		) {

			for (
				Map.Entry <String,String> entry
					: componentDefinition.referenceProperties ().entrySet ()
			) {

				taskLogger.debugFormat (
					"Setting reference property %s.%s",
					componentDefinition.name (),
					entry.getKey ());

				Object target =
					getComponentRequired (
						taskLogger,
						entry.getValue (),
						Object.class);

				PropertyUtils.propertySetSimple (
					component,
					entry.getKey (),
					target);

			}

		}

	}

	private
	void setComponentInjectedProperties (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull ComponentDefinition componentDefinition,
			@NonNull Object component) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"setComponentInjectedProperties");

			HeldLock heldlock =
				lock.read ();

		) {

			for (
				InjectedProperty injectedProperty
					: componentDefinition.injectedProperties ()
			) {

				injectProperty (
					taskLogger,
					componentDefinition,
					component,
					injectedProperty);

			}

		}

	}

	private static
	class Injection {

		String componentName;
		Object component;

		InjectedProperty injectedProperty;
		List <ComponentDefinition> targetComponents;

		Function <Provider <?>, Object> transformer;
		Function <List <Pair <ComponentDefinition, Object>>, Object> aggregator;

		Set <String> missingComponents;

	}

	private
	void injectProperty (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull ComponentDefinition componentDefinition,
			@NonNull Object component,
			@NonNull InjectedProperty injectedProperty) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"injectProperty");

		) {

			taskLogger.debugFormat (
				"Setting injected property %s.%s",
				classNameSimple (
					injectedProperty.field ().getDeclaringClass ()),
				injectedProperty.field ().getName ());

			Injection injection =
				new Injection ();

			injection.componentName =
				componentDefinition.name ();

			injection.component =
				component;

			injection.injectedProperty =
				injectedProperty;

			injection.targetComponents =
				iterableMapToList (
					injectedProperty.targetComponentNames (),
					registry::byNameRequired);

			// define transformer

			if (injectedProperty.prototype ()) {

				injection.transformer =
					provider -> provider;

			} else {

				injection.transformer =
					provider -> provider.get ();

			}

			// define aggregator

			if (
				enumEqualSafe (
					injectedProperty.collectionType (),
					CollectionType.componentClassMap)
			) {

				injection.aggregator =
					targetComponents ->
						iterableTransformToMap (
							targetComponents,
							item ->
								item.getLeft ().componentClass (),
							item ->
								item.getRight ());

			} else if (
				enumEqualSafe (
					injectedProperty.collectionType (),
					CollectionType.componentNameMap)
			) {

				injection.aggregator =
					targetComponents ->
						iterableTransformToMap (
							targetComponents,
							item ->
								item.getLeft ().name (),
							item ->
								item.getRight ());

			} else if (
				enumEqualSafe (
					injectedProperty.collectionType (),
					CollectionType.list)
			) {

				injection.aggregator =
					targetComponents ->
						iterableMapToList (
							targetComponents,
							item ->
								item.getRight ());

			} else if (
				enumEqualSafe (
					injectedProperty.collectionType (),
					CollectionType.single)
			) {

				injection.aggregator =
					targetComponents -> {

					if (targetComponents.size () != 1) {

						throw taskLogger.fatalFormat (
							"Trying to inject %s ",
							integerToDecimalString (
								targetComponents.size ()),
							"components into a single field %s.%s",
							classNameSimple (
								injectedProperty.field ().getDeclaringClass ()),
							injectedProperty.field ().getName ());

					}

					return targetComponents.get (0).getRight ();

				};

			} else {

				throw new LoggedErrorsException (
					taskLogger);

			}

			if (injectedProperty.weak ()) {

				injection.missingComponents =
					injection.targetComponents.stream ()

					.filter (
						definition ->
							doesNotContain (
								singletonComponents.keySet (),
								definition.name ()))

					.map (
						definition ->
							definition.name ())

					.collect (
						Collectors.toSet ());

			} else {

				injection.missingComponents =
					ImmutableSet.of ();

			}

			if (
				collectionIsEmpty (
					injection.missingComponents)
			) {

				performInjection (
					taskLogger,
					injection);

			} else {

				for (
					String missingComponentName
						: injection.missingComponents
				) {

					List <Injection> injectionsByDependency =
						pendingInjectionsByDependencyName.computeIfAbsent (
							missingComponentName,
							name -> new ArrayList<> ());

					injectionsByDependency.add (
						injection);

				}

			}

		}

	}

	private
	void performInjection (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull Injection injection) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"performInjection");

		) {

			List <Pair <ComponentDefinition, Provider <?>>> targetProviders =
				iterableMapToList (
					injection.targetComponents,
					targetDefinition ->
						Pair.of (
							targetDefinition,
							getComponentProvider (
								taskLogger,
								targetDefinition,
								injection
									.injectedProperty
									.initialized ())));

			List <String> missingRawValueNames =
				iterableMapToList (
					iterableFilter (
						(defintion, rawValue) ->
							isNull (
								rawValue),
						targetProviders),
					unaggregatedValue ->
						unaggregatedValue.getLeft ().name ());

			if (
				collectionIsNotEmpty (
					missingRawValueNames)
			) {

				throw taskLogger.errorFormatThrow (
					"Missing target components for %s.%s: %s",
					injection.componentName,
					injection.injectedProperty.field ().getName (),
					joinWithCommaAndSpace (
						missingRawValueNames));

			}

			List <Pair <ComponentDefinition, Object>> unaggregatedValues =
				iterableMapToList (
					targetProviders,
					(targetDefinition, targetProvider) ->
						Pair.of (
							targetDefinition,
							injection.transformer.apply (
								targetProvider)));

			/*
			List <String> missingValueNames =
				iterableMapToList (
					iterableFilter (
						unaggregatedValue ->
							isNull (
								unaggregatedValue.getRight ()),
						unaggregatedValues),
					unaggregatedValue ->
						unaggregatedValue.getLeft ().name ());

			if (
				collectionIsNotEmpty (
					missingValueNames)
			) {

				throw taskLogger.errorFormatThrow (
					"Missing target components for %s.%s: %s",
					injection.componentName,
					injection.injectedProperty.field ().getName (),
					joinWithCommaAndSpace (
						missingValueNames));

			}*/

			Object aggregatedValue =
				injection.aggregator.apply (
					unaggregatedValues);

			if (
				isNull (
					aggregatedValue)
			) {

				throw taskLogger.errorFormatThrow (
					"Aggregator for %s.%s returned null",
					injection.componentName,
					injection.injectedProperty.field ().getName ());

			}

			Field field =
				injection.injectedProperty.field ();

			fieldSet (
				field,
				injection.component,
				optionalOf (
					aggregatedValue));

		}

	}

	public
	ComponentManager init (
			@NonNull TaskLogger parentTaskLogger) {

		TaskLogger taskLogger =
			logContext.nestTaskLogger (
				parentTaskLogger,
				"init");

		try (

			HeldLock heldlock =
				lock.write ();

		) {

			if (
				enumNotEqualSafe (
					state,
					State.creation)
			) {
				throw new IllegalStateException ();
			}

			state =
				State.initialization;

			// set all fields to accessible

			for (
				ComponentDefinition componentDefinition
					: registry.all ()
			) {

				for (
					InjectedProperty injectedProperty
						: componentDefinition.injectedProperties ()
				) {

					injectedProperty.field ().setAccessible (
						true);

				}

			}

			// instantiate singletons

			for (
				ComponentDefinition componentDefinition
					: registry.singletons ()
			) {

				getComponentRequired (
					taskLogger,
					componentDefinition.name (),
					Object.class);

				// fill in weak links as we go

				Optional <List <Injection>> pendingInjectionsOptional =
					mapItemForKey (
						pendingInjectionsByDependencyName,
						componentDefinition.name ());

				if (
					optionalIsPresent (
						pendingInjectionsOptional)
				) {

					ListIterator <Injection> pendingInjectionIterator =
						pendingInjectionsOptional.get ().listIterator ();

					while (pendingInjectionIterator.hasNext ()) {

						Injection pendingInjection =
							pendingInjectionIterator.next ();

						pendingInjection.missingComponents.remove (
							componentDefinition.name ());

						if (
							collectionIsEmpty (
								pendingInjection.missingComponents)
						) {

							performInjection (
								taskLogger,
								pendingInjection);

							pendingInjectionIterator.remove ();

						}

					}

					pendingInjectionsByDependencyName.remove (
						componentDefinition.name ());

				}

			}

			// check we filled all weak dependencies

			if (
				mapIsNotEmpty (
					pendingInjectionsByDependencyName)
			) {

				throw new RuntimeException (
					stringFormat (
						"Pending injections not satisfied: %s",
						joinWithCommaAndSpace (
							pendingInjectionsByDependencyName.keySet ())));

			}

			// run late setup

			invokeLifecycleMethods (
				taskLogger,
				LateLifecycleSetup.class,
				"Late lifecycle setup",
				singletonComponents.values ());

			taskLogger.makeException ();

			// run startup complete

			invokeLifecycleMethods (
				taskLogger,
				ComponentManagerStartupComplete.class,
				"Component manager startup complete",
				singletonComponents.values ());

			taskLogger.makeException ();

			// return

			state =
				State.running;

			return this;

		}

	}

	private <AnnotationType extends Annotation>
	void invokeLifecycleMethods (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull Class <AnnotationType> annotationClass,
			@NonNull String label,
			@NonNull Iterable <ComponentData> componentDatas) {

		componentDatas.forEach (
			componentData ->
				invokeLifecycleMethods (
					parentTaskLogger,
					annotationClass,
					label,
					componentData));

	}

	private <AnnotationType extends Annotation>
	void invokeLifecycleMethods (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull Class <AnnotationType> annotationClass,
			@NonNull String label,
			@NonNull ComponentData componentData) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLoggerFormat (
					parentTaskLogger,
					"invokeLifecycleMethods (%s, %s, %s)",
					keyEqualsClassSimple (
						"annotationClass",
						annotationClass),
					keyEqualsString (
						"label",
						label),
					keyEqualsString (
						"componentName",
						componentData.name ()));

		) {

			Optional <Object> componentOptional =
				componentData.component ();

			if (
				optionalIsNotPresent (
					componentOptional)
			) {
				return;
			}

			Object component =
				optionalGetRequired (
					componentData.component ());

			Class <?> componentClass =
				component.getClass ();

			for (
				Method method
					: componentClass.getMethods ()
			) {

				AnnotationType annotation =
					method.getDeclaredAnnotation (
						annotationClass);

				if (
					isNull (
						annotation)
				) {
					continue;
				}

				List <Class <?>> methodParameterTypes =
					Arrays.asList (
						method.getParameterTypes ());

				if (

					collectionDoesNotHaveOneElement (
						methodParameterTypes)

					|| classNotEqual (
						listFirstElementRequired (
							methodParameterTypes),
						TaskLogger.class)

				) {

					taskLogger.errorFormat (
						"Build method %s.%s ",
						fullClassName (
							componentClass),
						method.getName (),
						"has invalid type signature (%s)",
						joinWithCommaAndSpace (
							iterableMap (
								parameterType ->
									classNameSimple (
										parameterType),
								methodParameterTypes)));

					continue;

				}


				if (

					notEqualToOne (
						method.getParameterCount ())

				) {

					taskLogger.errorFormat (
						"%s ",
						componentData.definition.name (),
						"method %s.%s ",
						classNameSimple (
							componentClass),
						method.getName (),
						"must have exactly one parameter");

					return;

				}

				methodInvoke (
					method,
					component,
					taskLogger);

			}

		}

	}

	@Override
	public
	void close () {

		try (

			OwnedTaskLogger taskLogger =
				logContext.createTaskLogger (
					"close ()");

			HeldLock heldlock =
				lock.write ();

		) {

			taskLogger.noticeFormat (
				"Closing component manager");

			if (
				! enumEqualSafe (
					state,
					State.running)
			) {
				return;
			}

			state =
				State.teardown;

			List <ComponentDefinition> singletonDefinitions =
				new ArrayList<> (
					registry.singletons ());

			Lists.reverse (
				singletonDefinitions);

			// run shutdown begun

			invokeLifecycleMethods (
				taskLogger,
				ComponentManagerShutdownBegun.class,
				"Component manager shutdown begun",
				singletonComponents.values ());

			// run tear down

			for (
				ComponentDefinition singletonDefinition
					: singletonDefinitions
			) {

				Optional <ComponentData> componentDataOptional =
					mapItemForKey (
						singletonComponents,
						singletonDefinition.name ());

				if (
					optionalIsNotPresent (
						componentDataOptional)
				) {
					continue;
				}

				ComponentData componentData =
					optionalGetRequired (
						componentDataOptional);

				Optional <Object> componentOptional =
					componentData.component ();

				if (
					optionalIsNotPresent (
						componentOptional)
				) {
					continue;
				}

				Object component =
					optionalGetRequired (
						componentOptional);

				taskLogger.debugFormat (
					"Tearing down component: %s",
					singletonDefinition.name ());

				for (
					Method teardownMethod
						: singletonDefinition.normalTeardownMethods ()
				) {

					try {

						methodInvoke (
							teardownMethod,
							component,
							taskLogger);

					} catch (Exception exception) {

						taskLogger.errorFormatException (
							exception,
							"Error calling %s.%s for %s",
							classNameSimple (
								teardownMethod.getDeclaringClass ()),
							teardownMethod.getName (),
							singletonDefinition.name ());

					}

				}

			}

			state =
				State.closed;

		}

	}

	public
	Provider <?> getComponentProvider (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull ComponentDefinition componentDefinition) {

		return getComponentProvider (
			parentTaskLogger,
			componentDefinition,
			true);

	}

	public
	Provider <?> getComponentProvider (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull ComponentDefinition componentDefinition,
			@NonNull Boolean initialized) {

		try (

			HeldLock heldlock =
				lock.read ();

		) {

			return () -> {

				try (

					OwnedTaskLogger taskLogger =
						logContext.createTaskLogger (
							"getComponentProvider.get");

				) {

					return getComponent (
						taskLogger,
						componentDefinition,
						initialized);

				}

			};

		}

	}

	@Override
	public
	ComponentMetaData componentMetaData (
			@NonNull Object component) {

		return requiredValue (
			componentDatas.get (
				component));

	}

	private static
	enum State {
		creation,
		initialization,
		running,
		teardown,
		closed;
	}

	public static
	class ComponentData
		implements ComponentMetaData {

		ComponentDefinition definition;
		Optional <Object> optionalComponent;
		WeakReference <Object> weakComponent;
		ComponentState state;

		@Override
		public
		String name () {
			return definition.name ();
		}

		@Override
		public
		ComponentDefinition definition () {
			return definition;
		}

		@Override
		public
		Optional <Object> component () {

			return optionalOrElseOptional (
				optionalComponent,
				() -> optionalFromNullable (
					weakComponent.get ()));

		}

		@Override
		public
		ComponentState state () {
			return state;
		}

	}

}
