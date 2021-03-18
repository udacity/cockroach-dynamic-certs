package io.crdb.docker;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.env.Environment;

import java.io.IOException;

@SpringBootApplication
public class DynamicCertsApplication implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DynamicCertsApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(DynamicCertsApplication.class, args);
    }

    @Autowired
    private Environment env;

    @Override
    public void run(ApplicationArguments args) throws Exception {


    }

    private void handleProcess(ProcessBuilder builder) throws IOException, InterruptedException {

        builder.inheritIO();

        String command = builder.command().toString();

        log.debug("starting command... {}", command);

        Process process = builder.start();
        int exitCode = process.waitFor();

        if (exitCode != 0) {
            throw new RuntimeException(String.format("the following command exited ABNORMALLY with code [%d]: %s", exitCode, command));
        } else {
            log.debug("command exited SUCCESSFULLY with code [{}]", exitCode);
        }

    }
}
