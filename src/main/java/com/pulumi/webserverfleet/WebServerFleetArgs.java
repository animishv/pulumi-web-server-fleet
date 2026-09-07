package com.pulumi.webserverfleet;

import com.pulumi.core.Output;

import java.util.List;

public class WebServerFleetArgs {

    private final List<Output<String>> subnets;
    private final List<MachineConfig> machines;
    private final Output<String> securityGroupId;

    public WebServerFleetArgs(
            List<Output<String>> subnets,
            List<MachineConfig> machines,
            Output<String> securityGroupId) {

        this.subnets = subnets;
        this.machines = machines;
        this.securityGroupId = securityGroupId;
    }

    public List<Output<String>> getSubnets() {
        return subnets;
    }

    public List<MachineConfig> getMachines() {
        return machines;
    }

    public Output<String> getSecurityGroupId() {
        return securityGroupId;
    }
}