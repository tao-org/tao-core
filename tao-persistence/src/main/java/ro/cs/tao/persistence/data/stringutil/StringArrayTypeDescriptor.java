package ro.cs.tao.persistence.data.stringutil;

/**
 * Created by cosmin on 6/28/2018.
 */
public class StringArrayTypeDescriptor
        extends AbstractArrayTypeDescriptor<String[]> {

    public static final StringArrayTypeDescriptor INSTANCE =
            new StringArrayTypeDescriptor();

    public StringArrayTypeDescriptor() {
        super( String[].class );
    }

    @Override
    protected String getSqlArrayType() {
        return "text";
    }
}