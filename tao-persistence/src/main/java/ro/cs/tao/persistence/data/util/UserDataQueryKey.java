package ro.cs.tao.persistence.data.util;
//
//import ro.cs.tao.persistence.data.Job;
//import ro.cs.tao.persistence.data.QueryParameter;
//
//import java.io.Serializable;
//
///**
// * Created by oana on 8/4/2017.
// */
//public class UserDataQueryKey implements Serializable {
//    private Job job;
//    private QueryParameter queryParameter;
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
//        UserDataQueryKey that = (UserDataQueryKey) o;
//
//        if (job != null ?
//          !job.equals(that.job) : that.job !=null)
//            return false;
//        if (queryParameter != null ?
//          !queryParameter.equals(that.queryParameter) : that.queryParameter !=null)
//            return false;
//
//        return true;
//    }
//
//    @Override
//    public int hashCode() {
//        int result;
//        result = (job != null ? job.hashCode() : 0);
//        result = 31 * result + (queryParameter != null ? queryParameter.hashCode() : 0);
//        return result;
//    }
//}
