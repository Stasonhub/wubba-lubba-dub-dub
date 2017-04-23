package service.provider;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ProviderScheduler implements AutoCloseable {

    private Logger logger = LoggerFactory.getLogger(ProviderScheduler.class);
    private AdvertImportService advertImportService;
    private ScheduledExecutorService scheduledExecutorService;

    public ProviderScheduler(AdvertImportService advertImportService) {
        this.advertImportService = advertImportService;
        this.scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
    }

    public void schedule(long initial, long interval) {
        scheduledExecutorService.scheduleWithFixedDelay(
                () -> {
                    try {
                        advertImportService.runImport();
                    } catch (Exception e) {
                        logger.error("Import process failed", e);
                    }
                }, initial, interval, TimeUnit.MILLISECONDS);
    }

    @Override
    public void close() throws Exception {
        scheduledExecutorService.shutdown();
        if (!scheduledExecutorService.awaitTermination(5, TimeUnit.MINUTES)) {
            logger.warn("Failed to stop scheduled task in 5 minutes. Terminating.");
            scheduledExecutorService.shutdownNow();
        }
    }
}