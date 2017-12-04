package ro.cs.tao.persistence.data.util;
//
//import ro.cs.tao.persistence.data.ProcessingOperation;
//
//import java.io.Serializable;
//
///**
// * Created by oana on 8/2/2017.
// */
//public class OperationVariableKey implements Serializable {
//
//    private ProcessingOperation processingOperation;
//    private String variableName;
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
//        OperationVariableKey that = (OperationVariableKey) o;
//
//        if (processingOperation != null ?
//          !processingOperation.equals(that.processingOperation) : that.processingOperation !=null)
//            return false;
//        if (variableName != null ?
//          !variableName.equals(that.variableName) : that.variableName !=null)
//            return false;
//
//        return true;
//    }
//
//    @Override
//    public int hashCode() {
//        int result;
//        result = (processingOperation != null ? processingOperation.hashCode() : 0);
//        result = 31 * result + (variableName != null ? variableName.hashCode() : 0);
//        return result;
//    }
//}
