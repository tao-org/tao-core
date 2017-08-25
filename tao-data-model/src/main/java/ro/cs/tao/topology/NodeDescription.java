package ro.cs.tao.topology;

/**
 * Created by cosmin on 7/17/2017.
 */
public class NodeDescription {
    private String ipAddr;
    private String userName;
    private String userPass;

    public NodeDescription() {

    }

    public String getIpAddr() {
        return ipAddr;
    }

    public void setIpAddr(String ipAddr) {
        this.ipAddr = ipAddr;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserPass() {
        return userPass;
    }

    public void setUserPass(String userPass) {
        this.userPass = userPass;
    }
}
