package ro.cs.tao.user;

import ro.cs.tao.topology.NodeFlavor;

public class UserMachine {
    private NodeFlavor machineType;
    private int quantity;

    public NodeFlavor getMachineType() { return machineType; }

    public void setMachineType(NodeFlavor machineType) { this.machineType = machineType; }

    public int getQuantity() { return quantity; }

    public void setQuantity(int quantity) { this.quantity = quantity; }
}
