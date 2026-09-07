package com.pulumi.webserverfleet;

public class UserDataBuilder {

    public static String build(OperatingSystem os) {

        return switch (os) {

            case UBUNTU -> """
                #!/bin/bash

                apt-get update -y
                apt-get install -y nginx

                systemctl enable nginx
                systemctl start nginx

                echo '<html>
                <head><title>Pulumi Web Server Fleet</title></head>
                <body>
                <h1>Hello from Ubuntu!</h1>
                <p>Provisioned by Pulumi WebServerFleet.</p>
                </body>
                </html>' > /var/www/html/index.html
                """;

            case AMAZON_LINUX -> """
                #!/bin/bash

                dnf install -y nginx

                systemctl enable nginx
                systemctl start nginx

                echo '<html>
                <head><title>Pulumi Web Server Fleet</title></head>
                <body>
                <h1>Hello from Amazon Linux!</h1>
                <p>Provisioned by Pulumi WebServerFleet.</p>
                </body>
                </html>' > /usr/share/nginx/html/index.html
                """;
        };
    }
}