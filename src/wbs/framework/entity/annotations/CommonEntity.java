package wbs.framework.entity.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import wbs.framework.entity.annotations.meta.EntityMeta;
import wbs.framework.entity.annotations.meta.EntityMetaCreate;
import wbs.framework.entity.annotations.meta.EntityMetaMutable;
import wbs.framework.entity.annotations.meta.EntityMetaTable;

@Retention (RetentionPolicy.RUNTIME)
@Target (ElementType.TYPE)
@EntityMeta
public
@interface CommonEntity {

	@EntityMetaTable
	String table ()
	default "";

	@EntityMetaCreate
	boolean create ()
	default true;

	@EntityMetaMutable
	boolean mutable ()
	default true;

}
