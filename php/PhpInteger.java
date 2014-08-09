package wbs.platform.php;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.google.common.collect.ImmutableMap;

class PhpInteger extends AbstractPhpEntity {

	private int value;

	public PhpInteger(int newValue) {
		super(PhpType.pInteger);
		value = newValue;
	}

	@Override
	public Boolean asBoolean() {
		return value != 0;
	}

	@Override
	public Integer asInteger() {
		return (int) value;
	}

	@Override
	public Long asLong() {
		return (long) value;
	}

	@Override
	public Double asDouble() {
		return (double) value;
	}

	@Override
	public String asString() {
		return Long.toString(value);
	}

	@Override
	public String asString(String encoding) {
		return Long.toString(value);
	}

	@Override
	public byte[] asByteArray() {
		try {
			return Long.toString(value).getBytes("iso-8859-1");
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public Map<Object,PhpEntity> asMap () {
		return ImmutableMap.<Object,PhpEntity>builder ()
			.put (0, this)
			.build ();
	}

	@Override
	public List<PhpEntity> asList() {
		List<PhpEntity> ret = new ArrayList<PhpEntity>();
		ret.add(this);
		return ret;
	}

	@Override
	public Map<Object,PhpEntity> asObjectMap () {
		return ImmutableMap.<Object,PhpEntity>builder ()
			.put ("scalar", this)
			.build ();
	}

	@Override
	public Object asObject() {
		return value;
	}

	@Override
	public int hashCode() {
		return (int) (value & 0xffffffff);
	}

	@Override
	public boolean equals(Object object) {
		if (!(object instanceof PhpInteger))
			return false;
		PhpInteger other = (PhpInteger) object;
		return value == other.value;
	}

	public final static PhpInteger zero = new PhpInteger(0),
			one = new PhpInteger(1);
}
