package wbs.framework.entity.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import wbs.framework.entity.annotations.meta.FieldMeta;
import wbs.framework.entity.annotations.meta.FieldMetaElement;
import wbs.framework.entity.annotations.meta.FieldMetaIndex;
import wbs.framework.entity.annotations.meta.FieldMetaKey;
import wbs.framework.entity.annotations.meta.FieldMetaOrderBy;
import wbs.framework.entity.annotations.meta.FieldMetaTable;
import wbs.framework.entity.annotations.meta.FieldMetaWhere;
import wbs.framework.entity.model.ModelFieldType;

@Retention (RetentionPolicy.RUNTIME)
@Target (ElementType.FIELD)
@FieldMeta (
	modelFieldType = ModelFieldType.collection)
public
@interface CollectionField {

	@FieldMetaTable
	String table ()
	default "";

	@FieldMetaKey
	String key ()
	default "";

	@FieldMetaIndex
	String index ()
	default "";

	@FieldMetaOrderBy
	String orderBy ()
	default "";

	@FieldMetaWhere
	String where ()
	default "";

	@FieldMetaElement
	String element ()
	default "";

}
