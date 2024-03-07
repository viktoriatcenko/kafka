package ru.demo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Abs {
    private static final Logger logger = LoggerFactory.getLogger(Abs.class);

    public static void main(String[] args) {
        var os = (com.sun.management.OperatingSystemMXBean)
                java.lang.management.ManagementFactory.getOperatingSystemMXBean();

        logger.info("pid:{}", ProcessHandle.current().pid());
        logger.info("availableProcessors:{}", Runtime.getRuntime().availableProcessors());
        logger.info("TotalMemorySize, mb:{}", os.getTotalMemorySize() / 1024 / 1024);
        logger.info("maxMemory, mb:{}", Runtime.getRuntime().maxMemory() / 1024 / 1024);
        logger.info("freeMemory, mb:{}", Runtime.getRuntime().freeMemory() / 1024 / 1024);

        SpringApplication.run(Abs.class, args);
    }
}
