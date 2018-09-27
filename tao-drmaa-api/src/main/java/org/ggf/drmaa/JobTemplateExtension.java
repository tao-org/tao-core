package org.ggf.drmaa;

/**
 * Extension interface to DRMAA JobTemplate that allows testing for and invoking optional attributes that are not
 * part of the DRMAA required set of attributes.
 * An attribute is then implemented as a getter (if the attribute name would be 'Xyz' - case sensitive - the implementation
 * should define a getter 'getXyz' - case sensitive).
 *
 * @author Cosmin Cara
 */
public interface JobTemplateExtension {
    /**
     * Tests the existence of the given attribute.
     * @param attributeName The name of the attribute.
     */
    boolean hasAttribute(String attributeName);
    /**
     * Sets the value of the given attribute. If it does not exist, a DrmaaException is thrown.
     * @param attributeName The name of the attribute.
     * @param value The value of the attribute.
     */
    void setAttribute(String attributeName, Object value) throws DrmaaException;
    /**
     * Gets the value of the given attribute. If it does not exist, a DrmaaException is thrown.
     * @param attributeName The name of the attribute.
     * @return  The value of the attribute.
     */
    Object getAttribute(String attributeName) throws DrmaaException;
}
