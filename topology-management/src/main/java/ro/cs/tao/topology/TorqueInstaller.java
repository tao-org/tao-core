package ro.cs.tao.topology;

/**
 * Created by cosmin on 7/17/2017.
 */
public class TorqueInstaller implements ITopologyComponentInstaller {
    @Override
    public void doInstall(NodeDescription info) {
        // TODO:
        //  - run a SSH to install torque on the remote node
        //  - during install on remote node, check if Torque is already installed
        //  - after installation, update the master node Torque config files in order to add the
        //      new node
    }

    @Override
    public void doUninstall(NodeDescription info) {
        //
        //  TODO: Maybe we should have a flag telling that the Torque should not be removed from
        //        the remote node but only removed from the master node configuration
    }

    private void installTorqueOnExecutionNode() {
        // TODO: Execute the installation on the execution node using SSH and the
        //       provided credentials
    }

    private void updateExecutionNodeTorqueFiles() {
        // TODO: Update the configuration files on the execution node
    }

    private void updateMasterNodeTorqueFiles() {
        // TODO: update the Torque configuration files from the master node
    }
}
