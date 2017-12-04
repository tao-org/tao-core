package ro.cs.tao.persistence.data.util;
//
//import ro.cs.tao.persistence.data.OperationParameter;
//
//import java.io.Serializable;
//
///**
// * Created by oana on 8/2/2017.
// */
//public class OperationParameterValueKey implements Serializable {
//
//    private OperationParameter operationParameter;
//    private String possibleValue;
//
//    @Override
//    public boolean equals(Object o) {
//        if (this == o)
//        {
//            return true;
//        }
//        if (o == null|| getClass() != o.getClass())
//        {
//            return false;
//        }
//
//        OperationParameterValueKey that = (OperationParameterValueKey) o;
//
//        if (operationParameter != null ?
//          !operationParameter.equals(that.operationParameter) : that.operationParameter !=null)
//            return false;
//        if (possibleValue != null ?
//          !possibleValue.equals(that.possibleValue) : that.possibleValue !=null)
//            return false;
//
//        return true;
//    }
//
//    @Override
//    public int hashCode() {
//        int result;
//        result = (operationParameter != null ? operationParameter.hashCode() : 0);
//        result = 31 * result + (possibleValue != null ? possibleValue.hashCode() : 0);
//        return result;
//    }
//}
