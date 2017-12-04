package ro.cs.tao.persistence.data.util;
//
//import ro.cs.tao.persistence.data.Task;
//
//import java.io.Serializable;
//
///**
// * Created by oana on 7/28/2017.
// */
//public class TaskInputKey implements Serializable {
//
//    private Task task;
//    private String inputName;
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
//        TaskInputKey that = (TaskInputKey) o;
//
//        if (task != null ?
//          !task.equals(that.task) : that.task !=null)
//            return false;
//        if (inputName != null ?
//          !inputName.equals(that.inputName) : that.inputName !=null)
//            return false;
//
//        return true;
//    }
//
//    @Override
//    public int hashCode() {
//        int result;
//        result = (task != null ? task.hashCode() : 0);
//        result = 31 * result + (inputName != null ? inputName.hashCode() : 0);
//        return result;
//    }
//}
