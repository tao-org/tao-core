package ro.cs.tao.component;

/**
 * @author Cosmin Cara
 */
public class DataSourceComponent extends TaoComponent {
    @Override
    public String defaultName() { return "NewDatasource"; }

    @Override
    public Identifiable copy() {
        return null;
    }
}
