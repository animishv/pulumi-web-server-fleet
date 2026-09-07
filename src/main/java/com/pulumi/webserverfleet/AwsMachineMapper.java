package com.pulumi.webserverfleet;

import com.pulumi.core.Output;
import com.pulumi.aws.ec2.Ec2Functions;
import com.pulumi.aws.ec2.inputs.GetAmiArgs;
import com.pulumi.aws.ec2.inputs.GetAmiFilterArgs;

public class AwsMachineMapper {

    public static String getInstanceType(MachineSize size) {

        return switch (size) {
            case SMALL -> "t3.micro";
            case MEDIUM -> "t3.small";
            case LARGE -> "t3.medium";
        };
    }

    public static Output<String> getAmiId(OperatingSystem os) {

        var ami = switch (os) {

            case UBUNTU -> Ec2Functions.getAmi(
                GetAmiArgs.builder()
                    .mostRecent(true)
                    .owners("099720109477")
                    .filters(
                        GetAmiFilterArgs.builder()
                            .name("name")
                            .values("ubuntu/images/hvm-ssd-gp3/ubuntu-noble-24.04-amd64-server-*")
                            .build(),

                        GetAmiFilterArgs.builder()
                            .name("virtualization-type")
                            .values("hvm")
                            .build(),

                        GetAmiFilterArgs.builder()
                            .name("root-device-type")
                            .values("ebs")
                            .build()
                    )
                    .build()
            );

            case AMAZON_LINUX -> Ec2Functions.getAmi(
                GetAmiArgs.builder()
                    .mostRecent(true)
                    .owners("137112412989")
                    .filters(
                        GetAmiFilterArgs.builder()
                            .name("name")
                            .values("al2023-ami-*-x86_64")
                            .build(),

                        GetAmiFilterArgs.builder()
                            .name("virtualization-type")
                            .values("hvm")
                            .build(),

                        GetAmiFilterArgs.builder()
                            .name("root-device-type")
                            .values("ebs")
                            .build()
                    )
                    .build()
            );
        };

        return ami.applyValue(result -> result.imageId());
    }
}