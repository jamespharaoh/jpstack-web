package wbs.framework.component.manager;

import static wbs.utils.collection.CollectionUtils.collectionIsEmpty;
import static wbs.utils.collection.IterableUtils.iterableMapToList;
import static wbs.utils.collection.MapUtils.mapDoesNotContainKey;
import static wbs.utils.collection.MapUtils.mapIsNotEmpty;
import static wbs.utils.collection.MapUtils.mapItemForKey;
import static wbs.utils.collection.MapUtils.mapItemForKeyRequired;
import static wbs.utils.etc.EnumUtils.enumEqualSafe;
import static wbs.utils.etc.EnumUtils.enumNotEqualSafe;
import static wbs.utils.etc.Misc.doesNotContain;
import static wbs.utils.etc.Misc.isNotNull;
import static wbs.utils.etc.Misc.isNull;
import static wbs.utils.etc.Misc.requiredValue;
import static wbs.utils.etc.NullUtils.ifNull;
import static wbs.utils.etc.NumberUtils.integerToDecimalString;
import static wbs.utils.etc.NumberUtils.notEqualToOne;
import static wbs.utils.etc.OptionalUtils.optionalAbsent;
import static wbs.utils.etc.OptionalUtils.optionalFromNullable;
import static wbs.utils.etc.OptionalUtils.optionalGetRequired;
import static wbs.utils.etc.OptionalUtils.optionalIsNotPresent;
import static wbs.utils.etc.OptionalUtils.optionalIsPresent;
import static wbs.utils.etc.OptionalUtils.optionalOf;
import static wbs.utils.etc.ReflectionUtils.fieldSet;
import static wbs.utils.etc.ReflectionUtils.methodInvoke;
import static wbs.utils.etc.TypeUtils.classInstantiate;
import static wbs.utils.etc.TypeUtils.classNameSimple;
import static wbs.utils.etc.TypeUtils.genericCastUnchecked;
import static wbs.utils.etc.TypeUtils.isNotSubclassOf;
import static wbs.utils.string.StringUtils.joinWithCommaAndSpace;
import static wbs.utils.string.StringUtils.stringEqualSafe;
import static wbs.utils.string.StringUtils.stringFormat;

import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
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

import wbs.framework.activitymanager.ActiveTask;
import wbs.framework.activitymanager.ActivityManager;
import wbs.framework.activitymanager.RuntimeExceptionWithTask;
import wbs.framework.component.annotations.ComponentManagerShutdownBegun;
import wbs.framework.component.annotations.ComponentManagerStartupComplete;
import wbs.framework.component.annotations.LateLifecycleSetup;
import wbs.framework.component.annotations.NormalLifecycleSetup;
import wbs.framework.component.registry.ComponentDefinition;
import wbs.framework.component.registry.ComponentRegistry;
import wbs.framework.component.registry.InjectedProperty;
import wbs.framework.component.registry.InjectedProperty.CollectionType;
import wbs.framework.component.tools.ComponentFactory;
import wbs.framework.component.tools.EasyReadWriteLock;
import wbs.framework.component.tools.EasyReadWriteLock.HeldLock;
import wbs.framework.component.tools.NoSuchComponentException;
import wbs.framework.logging.DefaultLogContext;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.TaskLogger;

import wbs.utils.etc.PropertyUtils;
import wbs.utils.exception.RuntimeInvocationTargetException;

@Accessors (fluent = true)
public
class ComponentManagerImplementation
	implements ComponentManager {

	private final static
	LogContext logContext =
		DefaultLogContext.forClass (
			ComponentManagerImplementation.class);

	// properties

	@Getter @Setter
	ComponentRegistry registry;

	@Getter @Setter
	ActivityManager activityManager;

	// state

	EasyReadWriteLock lock =
		EasyReadWriteLock.instantiate ();

	State state =
		State.creation;

	Map <String, Object> singletonComponents =
		new HashMap<> ();

	Set <String> singletonComponentsInCreation =
		new LinkedHashSet<> ();

	Set <String> singletonComponentsFailed =
		new HashSet<> ();

	Map <Object, ComponentMetaDataImplementation> componentMetaDatas =
		new MapMaker ()
			.weakKeys ()
			.makeMap ();

	Map <String, List <Injection>> pendingInjectionsByDependencyName =
		new HashMap<> ();

	// public implementation

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

	public
	Map <String, Object> getAllSingletonComponents (
			@NonNull TaskLogger parentTaskLogger) {

		TaskLogger taskLogger =
			logContext.nestTaskLogger (
				parentTaskLogger,
				"getAllSingletonComponents");

		try (

			HeldLock heldlock =
				lock.read ();

		) {

			return registry.all ().stream ()

				.filter (
					componentDefinition ->
						stringEqualSafe (
							componentDefinition.scope (),
							"singleton"))

				.collect (
					Collectors.toMap (
						ComponentDefinition::name,
						componentDefinition ->
							getComponent (
								taskLogger,
								componentDefinition,
								true)));

		}

	}

	private
	Object getComponent (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull ComponentDefinition componentDefinition,
			@NonNull Boolean initialize) {

		TaskLogger taskLogger =
			logContext.nestTaskLogger (
				parentTaskLogger,
				"getComponent");

		try (

			HeldLock heldlock =
				lock.read ();

		) {

			if (
				stringEqualSafe (
					componentDefinition.scope (),
					"prototype")
			) {

				return instantiateComponent (
					taskLogger,
					componentDefinition,
					initialize);

			} else if (
				stringEqualSafe (
					componentDefinition.scope (),
					"singleton")
			) {

				if (! initialize) {
					throw new IllegalArgumentException ();
				}

				Object component =
					singletonComponents.get (
						componentDefinition.name ());

				if (component != null)
					return component;

				if (
					singletonComponentsInCreation.contains (
						componentDefinition.name ())
				) {

					throw new RuntimeExceptionWithTask (
						activityManager.currentTask (),
						stringFormat (
							"Singleton component %s already in creation (%s)",
							componentDefinition.name (),
							joinWithCommaAndSpace (
								singletonComponentsInCreation)));

				}

				if (
					singletonComponentsFailed.contains (
						componentDefinition.name ())
				) {

					throw new RuntimeExceptionWithTask (
						activityManager.currentTask (),
						stringFormat (
							"Singleton component %s already failed",
							componentDefinition.name ()));

				}

				singletonComponentsInCreation.add (
					componentDefinition.name ());

				try {

					component =
						instantiateComponent (
							taskLogger,
							componentDefinition,
							true);

					singletonComponents.put (
						componentDefinition.name (),
						component);

				} finally {

					singletonComponentsInCreation.remove (
						componentDefinition.name ());

					if (component == null) {

						singletonComponentsFailed.add (
							componentDefinition.name ());

					}

				}

				return component;

			} else {

				throw new RuntimeExceptionWithTask (
					activityManager.currentTask (),
					stringFormat (
						"Unrecognised scope %s for component %s",
						componentDefinition.scope (),
						componentDefinition.name ()));

			}

		}

	}

	private
	Object instantiateComponent (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull ComponentDefinition componentDefinition,
			@NonNull Boolean initialize) {

		TaskLogger taskLogger =
			logContext.nestTaskLogger (
				parentTaskLogger,
				"instantiateComponent");

		try (

			HeldLock heldlock =
				lock.read ();

			ActiveTask activeTask =
				activityManager.start (
					"application-context",
					stringFormat (
						"instantiateComponent (%s)",
						componentDefinition.name ()),
					this);

		) {

			taskLogger.debugFormat (
				"Instantiating %s (%s)",
				componentDefinition.name (),
				componentDefinition.scope ());

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
			ComponentMetaDataImplementation componentMetaData;

			if (
				isNotNull (
					componentDefinition.factoryClass ())
			) {

				ComponentFactory componentFactory =
					(ComponentFactory)
					protoComponent;

				component =
					componentFactory.makeComponent (
						taskLogger);

				if (
					isNull (
						component)
				) {

					throw new RuntimeExceptionWithTask (
						activityManager.currentTask (),
						stringFormat (
							"Factory component returned null for %s",
							componentDefinition.name ()));

				}

				componentMetaData =
					findOrCreateMetaDataForComponent (
						componentDefinition,
						component);

			} else {

				component =
					protoComponent;

				componentMetaData =
					findOrCreateMetaDataForComponent (
						componentDefinition,
						component);

			}

			// initialize

			if (

				initialize

				&& enumEqualSafe (
					componentMetaData.state,
					ComponentState.uninitialized)

			) {

				initializeComponent (
					taskLogger,
					componentDefinition,
					component,
					componentMetaData);

			}

			// and finish

			taskLogger.debugFormat (
				"Component %s instantiated successfully",
				componentDefinition.name ());

			return component;

		}

	}

	private
	void initializeComponent (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull ComponentDefinition componentDefinition,
			@NonNull Object component,
			@NonNull ComponentMetaDataImplementation componentMetaData) {

		try (

			TaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"initializeComponent");

		) {

			synchronized (componentMetaData) {

				if (
					enumNotEqualSafe (
						componentMetaData.state,
						ComponentState.uninitialized)
				) {

					throw new IllegalStateException (
						stringFormat (
							"Tried to initialize component %s ",
							componentMetaData.definition.name (),
							"in %s state",
							componentMetaData.state.name ()));

				}

				try {

					// run eager lifecycle setup

					for (
						Method method
							: component.getClass ().getMethods ()
					) {

						NormalLifecycleSetup eagerLifecycleSetupAnnotation =
							method.getAnnotation (
								NormalLifecycleSetup.class);

						if (
							isNull (
								eagerLifecycleSetupAnnotation)
						) {
							continue;
						}

						taskLogger.debugFormat (
							stringFormat (
								"Running eager lifecycle setup method %s.%s",
								componentDefinition.name (),
								method.getName ()));

						if (method.getParameterCount () == 0) {

							methodInvoke (
								method,
								component);

						} else if (method.getParameterCount () == 1) {

							methodInvoke (
								method,
								component,
								taskLogger);

						} else {

							throw new RuntimeException ();

						}

					}

					componentMetaData.state =
						ComponentState.active;

				} finally {

					if (
						enumNotEqualSafe (
							componentMetaData.state,
							ComponentState.active)
					) {

						componentMetaData.state =
							ComponentState.error;

					}

				}

			}

		}

	}

	private
	ComponentMetaDataImplementation findOrCreateMetaDataForComponent (
			@NonNull ComponentDefinition componentDefinition,
			@NonNull Object component) {

		// create component info

		return componentMetaDatas.computeIfAbsent (
			component,
			_component -> {

			ComponentMetaDataImplementation newComponentMetaData =
				new ComponentMetaDataImplementation ();

			newComponentMetaData.definition =
				componentDefinition;

			newComponentMetaData.component =
				new WeakReference <Object> (
					component);

			newComponentMetaData.state =
				componentDefinition.owned ()
					? ComponentState.uninitialized
					: ComponentState.unmanaged;

			return newComponentMetaData;

		});

	}

	private
	void setComponentValueProperties (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull ComponentDefinition componentDefinition,
			@NonNull Object component) {

		try (

			TaskLogger taskLogger =
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

				PropertyUtils.propertySetSimple (
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

			TaskLogger taskLogger =
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

			TaskLogger taskLogger =
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

		@SuppressWarnings ("unused")
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

			TaskLogger taskLogger =
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
					registry::byNameRequired,
					injectedProperty.targetComponentNames ());

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
					targetComponents -> {

					Map <Class <?>, Object> componentClassMap =
						new LinkedHashMap<> ();

					for (
						Pair <ComponentDefinition, Object> pair
							: targetComponents
					) {

						componentClassMap.put (
							pair.getLeft ().componentClass (),
							pair.getRight ());

					}

					return componentClassMap;

				};

			} else if (
				enumEqualSafe (
					injectedProperty.collectionType (),
					CollectionType.componentNameMap)
			) {

				injection.aggregator =
					targetComponents -> {

					Map <String, Object> componentNameMap =
						new LinkedHashMap<> ();

					for (
						Pair <ComponentDefinition, Object> pair
							: targetComponents
					) {

						componentNameMap.put (
							pair.getLeft ().name (),
							pair.getRight ());

					}

					return componentNameMap;

				};

			} else if (
				enumEqualSafe (
					injectedProperty.collectionType (),
					CollectionType.list)
			) {

				injection.aggregator =
					targetComponents -> {

					List <Object> componentsList =
						new ArrayList <> ();

					for (
						Pair <ComponentDefinition, Object> pair
							: targetComponents
					) {

						componentsList.add (
							pair.getRight ());

					}

					return componentsList;

				};

			} else if (
				enumEqualSafe (
					injectedProperty.collectionType (),
					CollectionType.single)
			) {

				injection.aggregator =
					targetComponents -> {

					if (targetComponents.size () != 1) {

						throw new RuntimeExceptionWithTask (
							activityManager.currentTask (),
							stringFormat (
								"Trying to inject %s ",
								integerToDecimalString (
									targetComponents.size ()),
								"components into a single field %s.%s",
								classNameSimple (
									injectedProperty.field ().getDeclaringClass ()),
								injectedProperty.field ().getName ()));

					}

					return targetComponents.get (0).getRight ();

				};

			} else {

				throw new RuntimeExceptionWithTask (
					activityManager.currentTask ());

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

			TaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"performInjection");

		) {

			List <Pair <ComponentDefinition, Object>> unaggregatedValues =
				iterableMapToList (
					targetComponentDefinition ->
						Pair.of (
							targetComponentDefinition,
							injection.transformer.apply (
								getComponentProvider (
									taskLogger,
									targetComponentDefinition,
									injection.injectedProperty.initialized ()))),
					injection.targetComponents);

			Object aggregatedValue =
				injection.aggregator.apply (
					unaggregatedValues);

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

			for (
				Object component
					: singletonComponents.values ()
			) {

				for (
					Method method
						: component.getClass ().getMethods ()
				) {

					LateLifecycleSetup lateLifecycleSetupAnnotation =
						method.getDeclaredAnnotation (
							LateLifecycleSetup.class);

					if (
						isNull (
							lateLifecycleSetupAnnotation)
					) {
						continue;
					}

					if (
						notEqualToOne (
							method.getParameterCount ())
					) {

						taskLogger.errorFormat (
							"Late lifecycle setup method %s.%s ",
							classNameSimple (
								component.getClass ()),
							method.getName (),
							"must have exactly one parameter");

					}

					methodInvoke (
						method,
						component,
						taskLogger);

				}

			}

			taskLogger.makeException ();

			// run startup complete

			for (
				Object component
					: singletonComponents.values ()
			) {

				for (
					Method method
						: component.getClass ().getMethods ()
				) {

					ComponentManagerStartupComplete startupCompleteAnnotation =
						method.getDeclaredAnnotation (
							ComponentManagerStartupComplete.class);

					if (
						isNull (
							startupCompleteAnnotation)
					) {
						continue;
					}

					if (
						notEqualToOne (
							method.getParameterCount ())
					) {

						taskLogger.errorFormat (
							"Startup complete method %s.%s ",
							classNameSimple (
								component.getClass ()),
							method.getName (),
							"must have exactly one parameter");

					}

					methodInvoke (
						method,
						component,
						taskLogger);

				}

			}

			taskLogger.makeException ();

			// return

			state =
				State.running;

			return this;

		}

	}

	@Override
	public
	void close () {

		try (

			TaskLogger taskLogger =
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

			for (
				Object component
					: singletonComponents.values ()
			) {

				for (
					Method method
						: component.getClass ().getMethods ()
				) {

					ComponentManagerShutdownBegun shutdownBegunAnnotation =
						method.getDeclaredAnnotation (
							ComponentManagerShutdownBegun.class);

					if (
						isNull (
							shutdownBegunAnnotation)
					) {
						continue;
					}

					if (
						notEqualToOne (
							method.getParameterCount ())
					) {
						throw new RuntimeException (
							stringFormat (
								"Shutdown begun method %s.%s ",
								classNameSimple (
									component.getClass ()),
								method.getName (),
								"must have exactly one parameter"));
					}

					try {

						methodInvoke (
							method,
							component,
							taskLogger);

					} catch (RuntimeInvocationTargetException exception) {

						taskLogger.errorFormatException (
							exception,
							"Error invoking %s.%s",
							classNameSimple (
								component.getClass ()),
							method.getName ());

					}

				}

			}

			// run tear down

			for (
				ComponentDefinition singletonDefinition
					: singletonDefinitions
			) {

				if (
					mapDoesNotContainKey (
						singletonComponents,
						singletonDefinition.name ())
				) {
					continue;
				}

				taskLogger.debugFormat (
					"Tearing down component: %s",
					singletonDefinition.name ());

				Object component =
					mapItemForKeyRequired (
						singletonComponents,
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

			return new Provider <Object> () {

				@Override
				public
				Object get () {

					try (

						TaskLogger taskLogger =
							logContext.createTaskLogger (
								"getComponentProvider.Provider.get");

					) {

						return getComponent (
							taskLogger,
							componentDefinition,
							initialized);

					}

				}

			};

		}

	}

	@Override
	public
	ComponentMetaData componentMetaData (
			@NonNull Object component) {

		return requiredValue (
			componentMetaDatas.get (
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
	class ComponentMetaDataImplementation
		implements ComponentMetaData {

		ComponentDefinition definition;
		WeakReference <Object> component;
		ComponentState state;

		@Override
		public
		ComponentDefinition definition () {
			return definition;
		}

		@Override
		public
		Optional <Object> component () {

			return optionalFromNullable (
				component.get ());

		}

		@Override
		public
		ComponentState state () {
			return state;
		}

	}

	public static
	enum ComponentState {
		uninitialized,
		active,
		tornDown,
		error,
		unmanaged;
	}

}
