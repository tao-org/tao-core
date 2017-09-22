package ro.cs.tao.component.execution;

import ro.cs.tao.component.enums.ProcessingComponentVisibility;

import javax.xml.bind.annotation.XmlEnumValue;

/**
 * Created by cosmin on 9/21/2017.
 */
public enum ExecutionStatus {
    /**
     * Job status cannot be determined.
     */
    @XmlEnumValue("0")
    UNDETERMINED(0),
    /**
     *  Job is queued and active.
     */
    @XmlEnumValue("1")
    QUEUED_ACTIVE(1),
    /**
     * Job is running.
     */
    @XmlEnumValue("2")
    RUNNING(2),
    /**
     * Job is suspended.
     */
    @XmlEnumValue("3")
    SUSPENDED(3),
    /**
     * Job has finished normally.
     */
    @XmlEnumValue("4")
    DONE(4),
    /**
     * Job finished, but terminated abnormally.
     */
    @XmlEnumValue("5")
    FAILED(5);

    /**
     * Numerical value for enum constants
     */
    private final int value;

    /**
     * Constructor
     * @param s - the integer value identifier
     */
    ExecutionStatus(final int s)
    {
        value = s;
    }

    @Override
    public String toString()
    {
        return String.valueOf(this.value);
    }

    /**
     * Retrieve string enum token corresponding to the integer identifier
     * @param value the integer value identifier
     * @return the string token corresponding to the integer identifier
     */
    public static String getEnumConstantNameByValue(final int value)
    {
        for (ExecutionStatus type : values())
        {
            if ((String.valueOf(value)).equals(type.toString()))
            {
                // return the name of the enum constant having the given value
                return type.name();
            }
        }
        return null;
    }

}
