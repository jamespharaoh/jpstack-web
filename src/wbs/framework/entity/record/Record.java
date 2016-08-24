package wbs.framework.entity.record;

public
interface Record<Type extends Record<Type>>
	extends
		Comparable<Record<Type>>,
		IdObject {

}
