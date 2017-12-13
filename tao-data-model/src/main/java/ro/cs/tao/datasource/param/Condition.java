package ro.cs.tao.datasource.param;

/**
 * @author Cosmin Cara
 */
public abstract class Condition {

    public static final Condition EQ = new Condition() {
        @Override
        public boolean evaluate(Comparable<Comparable> arg1, Comparable arg2) {
            return arg1!= null && arg2 != null && arg1.compareTo(arg2) == 0;
        }
    };
    public static final Condition NE = new Condition() {
        @Override
        public boolean evaluate(Comparable<Comparable> arg1, Comparable arg2) {
            return arg1 == null || arg2 == null || arg1.compareTo(arg2) != 0;
        }
    };
    public static final Condition LT = new Condition() {
        @Override
        public boolean evaluate(Comparable<Comparable> arg1, Comparable arg2) {
            return arg1!= null && arg2 != null && arg1.compareTo(arg2) < 0;
        }
    };
    public static final Condition LTE = new Condition() {
        @Override
        public boolean evaluate(Comparable<Comparable> arg1, Comparable arg2) {
            return arg1!= null && arg2 != null && arg1.compareTo(arg2) <= 0;
        }
    };
    public static final Condition GT = new Condition() {
        @Override
        public boolean evaluate(Comparable<Comparable> arg1, Comparable arg2) {
            return arg1!= null && arg2 != null && arg1.compareTo(arg2) > 0;
        }
    };
    public static final Condition GTE = new Condition() {
        @Override
        public boolean evaluate(Comparable<Comparable> arg1, Comparable arg2) {
            return arg1!= null && arg2 != null && arg1.compareTo(arg2) >= 0;
        }
    };

    private Condition() { }

    public abstract boolean evaluate(Comparable<Comparable> arg1, Comparable arg2);

}
