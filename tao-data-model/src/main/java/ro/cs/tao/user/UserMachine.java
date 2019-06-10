package ro.cs.tao.user;

import ro.cs.tao.topology.NodeType;

public class UserMachine {
    private NodeType machineType;
    private int quantity;

    public NodeType getMachineType() { return machineType; }

    public void setMachineType(NodeType machineType) { this.machineType = machineType; }

    public int getQuantity() { return quantity; }

    public void setQuantity(int quantity) { this.quantity = quantity; }
}
