package com.pulumi.webserverfleet;

import com.pulumi.aws.ec2.Instance;
import com.pulumi.aws.ec2.InstanceArgs;
import com.pulumi.resources.ComponentResource;
import com.pulumi.resources.ComponentResourceOptions;
import com.pulumi.resources.CustomResourceOptions;
import com.pulumi.core.Output;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class WebServerFleet extends ComponentResource {

    private final List<Instance> instances = new ArrayList<>();

    public WebServerFleet(
            String name,
            WebServerFleetArgs args) {

        this(name, args, null);
    }

    public WebServerFleet(
            String name,
            WebServerFleetArgs args,
            ComponentResourceOptions options) {

        super(
            "webserverfleet:index:WebServerFleet",
            name,
            options
        );

        var subnets = args.getSubnets();
        var securityGroupId = args.getSecurityGroupId();

        if (subnets.isEmpty()) {
            throw new IllegalArgumentException(
                "WebServerFleet requires at least one subnet"
            );
        }

        int subnetIndex = 0;

        for (MachineConfig machineConfig : args.getMachines()) {

            if (machineConfig.getCount() <= 0) {
                throw new IllegalArgumentException(
                    "Machine count must be greater than zero"
                );
            }

            String instanceType =
                AwsMachineMapper.getInstanceType(
                    machineConfig.getSize()
                );

            var amiId =
                AwsMachineMapper.getAmiId(
                    machineConfig.getOs()
                );

            String userData =
                UserDataBuilder.build(
                    machineConfig.getOs()
                );

            for (int i = 0; i < machineConfig.getCount(); i++) {

                String instanceName =
                    name
                    + "-"
                    + machineConfig.getOs().name().toLowerCase()
                    + "-"
                    + machineConfig.getSize().name().toLowerCase()
                    + "-"
                    + (i + 1);

                var subnetId =
                    subnets.get(subnetIndex % subnets.size());

                var instance = new Instance(
                    instanceName,
                    InstanceArgs.builder()
                        .ami(amiId)
                        .instanceType(instanceType)
                        .subnetId(subnetId)
                        .vpcSecurityGroupIds(
                            securityGroupId.applyValue(List::of)
                        )
                        .userData(userData)
                        .tags(Map.of(
                            "Name", instanceName,
                            "ManagedBy", "Pulumi",
                            "Fleet", name,
                            "OperatingSystem",
                                machineConfig.getOs().name(),
                            "MachineSize",
                                machineConfig.getSize().name()
                        ))
                        .build(),
                    com.pulumi.resources.CustomResourceOptions.builder()
                        .parent(this)
                        .build()
                );

                instances.add(instance);

                subnetIndex++;
            }
        }

        registerOutputs(Map.of(
                "instanceIds",
                Output.all(instances.stream()
                    .map(Instance::id)
                    .toList()),

                "publicIps",
                Output.all(instances.stream()
                    .map(Instance::publicIp)
                    .toList())
            ));
         }

    public List<Instance> getInstances() {
        return instances;
    }
}