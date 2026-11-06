package ro.cs.tao.user;

import ro.cs.tao.topology.NodeFlavor;

/**
 * Represents a machine associated with a user in the system.
 * A machine is defined by its type (represented by a {@code NodeFlavor}) and its quantity.
 *
 * This class provides getter and setter methods to access and modify the machine's type and quantity.
 */
public class UserMachine {
    private NodeFlavor machineType;
    private int quantity;

    public NodeFlavor getMachineType() { return machineType; }

    public void setMachineType(NodeFlavor machineType) { this.machineType = machineType; }

    public int getQuantity() { return quantity; }

    public void setQuantity(int quantity) { this.quantity = quantity; }
}
