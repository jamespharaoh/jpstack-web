package wbs.framework.schema.helper;

import static wbs.framework.utils.etc.Misc.camelToUnderscore;
import static wbs.framework.utils.etc.Misc.capitalise;
import static wbs.framework.utils.etc.Misc.stringFormat;

import java.sql.Types;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.extern.log4j.Log4j;

import org.hibernate.type.Type;
import org.hibernate.usertype.CompositeUserType;
import org.joda.time.Instant;
import org.joda.time.LocalDate;

import wbs.framework.application.annotations.SingletonComponent;
import wbs.framework.application.scaffold.PluginCustomTypeSpec;
import wbs.framework.application.scaffold.PluginEnumTypeSpec;
import wbs.framework.application.scaffold.PluginSpec;
import wbs.framework.application.scaffold.ProjectSpec;
import wbs.framework.hibernate.EnumUserType;
import wbs.framework.logging.TaskLog;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

@Accessors (fluent = true)
@Log4j
@SingletonComponent ("schemaTypesHelper")
public
class SchemaTypesHelperImpl
	implements SchemaTypesHelper {

	@Inject
	SchemaNamesHelper schemaNamesHelper;

	@Inject
	List<ProjectSpec> projects;

	@Getter
	Map<Class<?>,List<String>> fieldTypeNames;

	@Getter
	Map<String,List<String>> enumTypes;

	@PostConstruct
	public
	void init () {
		initTypeNames ();
	}

	void initTypeNames () {

		TaskLog taskLog =
			new TaskLog ()
				.log (log);

		ImmutableMap.Builder<Class<?>,List<String>> fieldTypeNamesBuilder =
			ImmutableMap.<Class<?>,List<String>>builder ();

		ImmutableMap.Builder<String,List<String>> enumTypesBuilder =
			ImmutableMap.<String,List<String>>builder ();

		for (
			Map.Entry<Class<?>,String> entry
				: builtinFieldTypeNames.entrySet ()
		) {

			fieldTypeNamesBuilder.put (
				entry.getKey (),
				ImmutableList.<String>of (
					entry.getValue ()));

		}

		for (
			ProjectSpec project
				: projects
		) {

			for (
				PluginSpec plugin
					: project.plugins ()
			) {

				if (plugin.models () == null)
					continue;

				for (
					PluginEnumTypeSpec enumType
						: plugin.models ().enumTypes ()
				) {

					initEnumType (
						taskLog,
						fieldTypeNamesBuilder,
						enumTypesBuilder,
						enumType);

				}

				for (
					PluginCustomTypeSpec customType
						: plugin.models ().customTypes ()
				) {

					initCustomType (
						taskLog,
						fieldTypeNamesBuilder,
						enumTypesBuilder,
						customType);

				}

			}

		}

		if (taskLog.errors () > 0) {

			throw new RuntimeException (
				stringFormat (
					"Aborting due to %s errors",
					taskLog.errors ()));

		}

		fieldTypeNames =
			fieldTypeNamesBuilder.build ();

		enumTypes =
			enumTypesBuilder.build ();

	}

	@SuppressWarnings ({ "unchecked", "rawtypes" })
	void initEnumType (
			TaskLog taskLog,
			ImmutableMap.Builder<Class<?>,List<String>> fieldTypeNamesBuilder,
			ImmutableMap.Builder<String,List<String>> enumTypesBuilder,
			PluginEnumTypeSpec enumType) {

		String enumClassName =
			stringFormat (
				"%s.%s.model.%s",
				enumType.plugin ().project ().packageName (),
				enumType.plugin ().packageName (),
				capitalise (enumType.name ()));

		Class enumClass;

		try {

			enumClass =
				Class.forName (
					enumClassName);

		} catch (ClassNotFoundException exception) {

			taskLog.error (
				"No such class %s",
				enumClassName);

			return;

		}

		EnumUserType enumUserType =
			new EnumUserType ()

			.sqlType (
				1111)

			.enumClass (
				enumClass)

			.auto (
				String.class);

		String typeName =
			camelToUnderscore (
				enumClass.getSimpleName ());

		fieldTypeNamesBuilder.put (
			enumClass,
			ImmutableList.<String>of (
				typeName));

		if (enumUserType.sqlType () == 1111) {

			ImmutableList.Builder<String> enumValuesBuilder =
				ImmutableList.<String>builder ();

			for (
				Object enumValue
					: enumUserType.databaseValues ()
			) {

				enumValuesBuilder.add (
					(String) enumValue);

			}

			enumTypesBuilder.put (
				typeName,
				enumValuesBuilder.build ());

		}

	}

	void initCustomType (
			TaskLog taskLog,
			ImmutableMap.Builder<Class<?>,List<String>> fieldTypeNamesBuilder,
			ImmutableMap.Builder<String,List<String>> enumTypesBuilder,
			PluginCustomTypeSpec customType) {

		String className =
			stringFormat (
				"%s.%s.model.%s",
				customType.plugin ().project ().packageName (),
				customType.plugin ().packageName (),
				capitalise (customType.name ()));

		Class<?> objectClass = null;

		try {

			objectClass =
				Class.forName (className);

		} catch (ClassNotFoundException exception) {

			taskLog.error (
				"No such class %s",
				className);

		}

		String helperClassName =
			stringFormat (
				"%s.%s.hibernate.%sType",
				customType.plugin ().project ().packageName (),
				customType.plugin ().packageName (),
				capitalise (customType.name ()));

		Class<?> helperClass = null;

		try {

			helperClass =
				Class.forName (helperClassName);

		} catch (ClassNotFoundException exception) {

			taskLog.error (
				"No such class %s",
				helperClassName);

		}

		if (objectClass == null
				|| helperClass == null)
			return;

		Object helper;

		try {

			helper =
				helperClass.newInstance ();

		} catch (Exception exception) {

			taskLog.error (
				exception,
				"Error instantiating %s",
				helperClass.getName ());

			return;

		}

		if (helper instanceof EnumUserType) {

			EnumUserType<?,?> enumHelper =
				(EnumUserType<?,?>) helper;

			String typeName =
				enumHelper.sqlType () == 1111
					? camelToUnderscore (
						enumHelper.enumClass ().getSimpleName ())
					: builtinSqlTypeNames.get (
						enumHelper.sqlType ());

			if (typeName == null) {

				taskLog.error (
					"Don't know how to handle sql type %s for %s",
					enumHelper.sqlType (),
					helper.getClass ().getName ());

				return;

			}

			fieldTypeNamesBuilder.put (
				objectClass,
				ImmutableList.<String>of (typeName));

			if (enumHelper.sqlType () == 1111) {

				ImmutableList.Builder<String> enumValuesBuilder =
					ImmutableList.<String>builder ();

				for (Object enumValue
						: enumHelper.databaseValues ()) {

					enumValuesBuilder.add (
						(String) enumValue);

				}

				enumTypesBuilder.put (
					typeName,
					enumValuesBuilder.build ());

			}

			return;

		}

		if (helper instanceof CompositeUserType) {

			CompositeUserType compositeUserType =
				(CompositeUserType) helper;

			ImmutableList.Builder<String> typeNamesBuilder =
				ImmutableList.<String>builder ();

			for (
				Type propertyType
					: compositeUserType.getPropertyTypes ()
			) {

				String typeName =
					builtinFieldTypeNames.get (
						propertyType.getReturnedClass ());

				if (typeName == null) {

					taskLog.error (
						"Don't know how to handle sql type %s for %s",
						propertyType.getReturnedClass (),
						helper.getClass ().getName ());

					return;

				}

				typeNamesBuilder.add (
					typeName);

			}

			fieldTypeNamesBuilder.put (
				compositeUserType.returnedClass (),
				typeNamesBuilder.build ());

			return;

		}

		taskLog.error (
			"Don't know how to handle %s",
			helper.getClass ());

		return;

	}

	Map<Class<?>,String> builtinFieldTypeNames =
		ImmutableMap.<Class<?>,String>builder ()
			.put (Boolean.class, "boolean")
			.put (Double.class, "double precision")
			.put (Integer.class, "integer")
			.put (String.class, "text")
			.put (byte[].class, "bytea")
			.put (LocalDate.class, "date")
			.put (Instant.class, "text")
			.put (Date.class, "timestamp with time zone")
			.put (Character.class, "char (1)")
			.build ();

	Map<Integer,String> builtinSqlTypeNames =
		ImmutableMap.<Integer,String>builder ()
			.put (Types.VARCHAR, "text")
			.put (Types.CHAR, "char (1)")
			.put (Types.INTEGER, "int")
			.build ();

}
