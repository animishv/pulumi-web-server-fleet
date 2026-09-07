package com.pulumi.webserverfleet;

public class MachineConfig {

    private final OperatingSystem os;
    private final MachineSize size;
    private final int count;

    public MachineConfig(
            OperatingSystem os,
            MachineSize size,
            int count) {

        this.os = os;
        this.size = size;
        this.count = count;
    }

    public OperatingSystem getOs() {
        return os;
    }

    public MachineSize getSize() {
        return size;
    }

    public int getCount() {
        return count;
    }
}