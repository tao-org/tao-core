package ro.cs.tao.topology;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Cosmin Udroiu
 */
public class ToolCommandsTokens {
    public static final String MASTER_HOSTNAME = "#master_hostname#";
    public static final String NODE_HOSTNAME = "#node_hostname#";
    public static final String NODE_USER = "#node_user#";
    public static final String NODE_PASSWORD = "#node_pass#";
    public static final String NODE_PROCESSORS_CNT = "#procs_cnt#";
    public static final String INSTALL_SCRIPTS_ROOT_PATH = "#scripts_root_path#";
    public static final String STEP_OUTPUT = "#step_output#";

    private static List<String> tokensList = new ArrayList<>();

    static {
        initAllTokensList();
    };

    public static List<String> getDefinedTokensList() {
        return tokensList;
    }

    /**
     * Creates a list with all token values from the variables defined with static final in this class
     */
    private static void initAllTokensList() {
        Field[] declaredFields = ToolCommandsTokens.class.getDeclaredFields();
        List<Field> staticFields = new ArrayList<>();
        for (Field field : declaredFields) {
            if (java.lang.reflect.Modifier.isStatic(field.getModifiers()) && java.lang.reflect.Modifier.isFinal(field.getModifiers()) &&
                    java.lang.reflect.Modifier.isPublic(field.getModifiers()) && field.getType().isAssignableFrom(String.class)) {
                try {
                    tokensList.add((String)field.get(null));
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
