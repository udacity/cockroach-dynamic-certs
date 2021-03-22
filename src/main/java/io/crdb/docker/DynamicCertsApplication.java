package io.crdb.docker;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.env.Environment;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

@SpringBootApplication
public class DynamicCertsApplication implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DynamicCertsApplication.class);

    private static final String CLIENT_USERNAME = "CLIENT_USERNAME";
    private static final String NODE_ALTERNATIVE_NAMES = "NODE_ALTERNATIVE_NAMES";

    private static final String COCKROACH_CERTS_DIR = "/.cockroach-certs";
    private static final String COCKROACH_KEY = "/.cockroach-key/ca.key";

    public static void main(String[] args) {
        SpringApplication.run(DynamicCertsApplication.class, args);
    }

    private final Environment env;

    public DynamicCertsApplication(Environment env) {
        this.env = env;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {

        final List<String> nodeAlternativeNames = Arrays.asList(env.getRequiredProperty(NODE_ALTERNATIVE_NAMES).trim().split("\\s+"));
        final String clientUsername = env.getProperty(CLIENT_USERNAME, "root");

        log.info("{} is [{}]", NODE_ALTERNATIVE_NAMES, nodeAlternativeNames);
        log.info("{} is [{}]", CLIENT_USERNAME, clientUsername);

        List<String> createCACommands = new ArrayList<>();
        createCACommands.add("/cockroach");
        createCACommands.add("cert");
        createCACommands.add("create-ca");
        createCACommands.add("--certs-dir");
        createCACommands.add(COCKROACH_CERTS_DIR);
        createCACommands.add("--ca-key");
        createCACommands.add(COCKROACH_KEY);

        ProcessBuilder createCA = new ProcessBuilder(createCACommands);
        handleProcess(createCA);

        List<String> createClientCommands = new ArrayList<>();
        createClientCommands.add("/cockroach");
        createClientCommands.add("cert");
        createClientCommands.add("create-client");
        createClientCommands.add(clientUsername);
        createClientCommands.add("--certs-dir");
        createClientCommands.add(COCKROACH_CERTS_DIR);
        createClientCommands.add("--ca-key");
        createClientCommands.add(COCKROACH_KEY);
        createClientCommands.add("--also-generate-pkcs8-key");

        ProcessBuilder createClient = new ProcessBuilder(createClientCommands);
        handleProcess(createClient);


        List<String> createNodeCommands = new ArrayList<>();
        createNodeCommands.add("/cockroach");
        createNodeCommands.add("cert");
        createNodeCommands.add("create-node");
        createNodeCommands.addAll(nodeAlternativeNames);
        createNodeCommands.add("--certs-dir");
        createNodeCommands.add(COCKROACH_CERTS_DIR);
        createNodeCommands.add("--ca-key");
        createNodeCommands.add(COCKROACH_KEY);

        ProcessBuilder createNode = new ProcessBuilder(createNodeCommands);
        handleProcess(createNode);
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
