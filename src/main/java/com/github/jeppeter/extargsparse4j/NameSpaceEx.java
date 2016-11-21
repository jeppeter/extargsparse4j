package com.github.jeppeter.extargsparse4j;

import java.util.Map;
import java.util.List;
import net.sourceforge.argparse4j.inf.Namespace;

public class NameSpaceEx {
	Map<String,Object> attr_;
	public NameSpaceEx(Namespace ns) {
		attr_ = ns.getAttrs();
	}

    /**
     * Returns attribute with given attribute name {@code dest}.
     * 
     * @param dest
     *            The attribute name
     * @return The attribute value, or {@code null} if it is not found.
     */
    @SuppressWarnings("unchecked")
    public <T> T get(String dest) {
        return (T) attr_.get(dest);
    }

    /**
     * Returns attribute as {@link String} with given attribute name
     * {@code dest}. This method calls {@link Object#toString()} method of a
     * found object to get string representation unless object is {@code null}.
     * 
     * @param dest
     *            The attribute name
     * @return The attribute value casted to {@link String}, or {@code null} if
     *         is not found.
     */
    public String getString(String dest) {
        Object o = get(dest);

        if(o == null) {
            return null;
        }

        return o.toString();
    }

    /**
     * Returns attribute as {@link Byte} with given attribute name {@code dest}.
     * 
     * @param dest
     *            The attribute name
     * @return The attribute value casted to {@link Byte}, or {@code null} if it
     *         is not found.
     */
    public Byte getByte(String dest) {
        return get(dest);
    }

    /**
     * Returns attribute as {@link Short} with given attribute name {@code dest}
     * .
     * 
     * @param dest
     *            The attribute name
     * @return The attribute value casted to {@link Short}, or {@code null} if
     *         it is not found.
     */
    public Short getShort(String dest) {
        return get(dest);
    }

    /**
     * Returns attribute as {@link Integer} with given attribute name
     * {@code dest}.
     * 
     * @param dest
     *            The attribute name
     * @return The attribute value casted to {@link Integer}, or {@code null} if
     *         it is not found.
     */
    public Integer getInt(String dest) {
        return get(dest);
    }

    /**
     * Returns attribute as {@link Long} with given attribute name {@code dest}.
     * 
     * @param dest
     *            The attribute name
     * @return The attribute value casted to {@link Long}, or {@code null} if it
     *         is not found.
     */
    public Long getLong(String dest) {
        return get(dest);
    }

    /**
     * Returns attribute as {@link Float} with given attribute name {@code dest}
     * .
     * 
     * @param dest
     *            The attribute name
     * @return The attribute value casted to {@link Float}, or {@code null} if
     *         it is not found.
     */
    public Float getFloat(String dest) {
        return get(dest);
    }

    /**
     * Returns attribute as {@link Double} with given attribute name
     * {@code dest}.
     * 
     * @param dest
     *            The attribute name
     * @return The attribute value casted to {@link Double}, or {@code null} if
     *         it is not found.
     */
    public Double getDouble(String dest) {
        return get(dest);
    }

    /**
     * Returns attribute as {@link Boolean} with given attribute name
     * {@code dest}.
     * 
     * @param dest
     *            The attribute name
     * @return The attribute value casted to {@link Boolean}, or {@code null} if
     *         it is not found.
     */
    public Boolean getBoolean(String dest) {
        return get(dest);
    }

    /**
     * Returns attribute as {@link List} with given attribute name {@code dest}.
     * 
     * @param dest
     *            The attribute name
     * @return The attribute value casted to {@link List}, or {@code null} if it
     *         is not found.
     */
    public <E> List<E> getList(String dest) {
        return get(dest);
    }

    /**
     * <p>
     * Returns {@link Map} object holding attribute values.
     * </p>
     * <p>
     * The application code can freely use returned object.
     * </p>
     * 
     * @return {@link Map} object holding attribute values.
     */
    public Map<String, Object> getAttrs() {
        return attr_;
    }

    public Object set(String key,Object val) {
    	return this.attr_.put(key,val);
    }

    @Override
    public String toString() {
        return this.attr_.toString();
    }
}