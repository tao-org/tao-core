package ro.cs.tao.persistence.data.util;
//
//import ro.cs.tao.persistence.data.DataSource;
//import ro.cs.tao.persistence.data.User;
//
//import java.io.Serializable;
//
///**
// * Created by oana on 8/4/2017.
// */
//public class UserDataSourceConnectionKey implements Serializable {
//
//    private User user;
//    private DataSource dataSource;
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
//        UserDataSourceConnectionKey that = (UserDataSourceConnectionKey) o;
//
//        if (user != null ?
//          !user.equals(that.user) : that.user !=null)
//            return false;
//        if (dataSource != null ?
//          !dataSource.equals(that.dataSource) : that.dataSource !=null)
//            return false;
//
//        return true;
//    }
//
//    @Override
//    public int hashCode() {
//        int result;
//        result = (user != null ? user.hashCode() : 0);
//        result = 31 * result + (dataSource != null ? dataSource.hashCode() : 0);
//        return result;
//    }
//}
