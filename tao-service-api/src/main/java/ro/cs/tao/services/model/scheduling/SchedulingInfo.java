/**
 * 
 */
package ro.cs.tao.services.model.scheduling;

/**
 * @author Lucian Barbulescu
 *
 */
public class SchedulingInfo {
	private String id;
	private String friendlyName;
	private String state;
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getFriendlyName() {
		return friendlyName;
	}
	public void setFriendlyName(String friendlyName) {
		this.friendlyName = friendlyName;
	}
	public String getState() {
		return state;
	}
	public void setState(String state) {
		this.state = state;
	}
	
	
}
