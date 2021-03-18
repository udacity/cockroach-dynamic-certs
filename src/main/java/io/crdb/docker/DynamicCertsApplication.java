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

@SpringBootApplication
public class DynamicCertsApplication implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DynamicCertsApplication.class);

    private static final String NODES = "NODES";
    private static final String CLIENT_USERNAME = "CLIENT_USERNAME";
    private static final String NODE_ALIAS = "NODE_ALIAS";

    private static final String COCKROACH_CERTS_DIR = "/.cockroach-certs";
    private static final String COCKROACH_KEY = "/.cockroach-key/ca.key";

    public static void main(String[] args) {
        SpringApplication.run(DynamicCertsApplication.class, args);
    }

    @Autowired
    private Environment env;

    @Override
    public void run(ApplicationArguments args) throws Exception {

        final List<String> nodes = Arrays.asList(env.getRequiredProperty(NODES).trim().split("\\s+"));
        final String clientUsername = env.getProperty(CLIENT_USERNAME, "root");
        final String nodeAlias = env.getProperty(NODE_ALIAS);

        log.info("{} is [{}]", NODES, nodes.toString());
        log.info("{} is [{}]", CLIENT_USERNAME, clientUsername);
        log.info("{} is [{}]", NODE_ALIAS, nodeAlias);

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

        for (String node : nodes) {
            List<String> createNodeCommands = new ArrayList<>();
            createNodeCommands.add("/cockroach");
            createNodeCommands.add("cert");
            createNodeCommands.add("create-node");
            createNodeCommands.add(node);
            createNodeCommands.add("localhost");
            if (StringUtils.hasText(nodeAlias)) {
                createNodeCommands.add(nodeAlias);
            }
            createNodeCommands.add("--certs-dir");
            createNodeCommands.add(COCKROACH_CERTS_DIR);
            createNodeCommands.add("--ca-key");
            createNodeCommands.add(COCKROACH_KEY);

            ProcessBuilder createNode = new ProcessBuilder(createNodeCommands);
            handleProcess(createNode);
        }

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
