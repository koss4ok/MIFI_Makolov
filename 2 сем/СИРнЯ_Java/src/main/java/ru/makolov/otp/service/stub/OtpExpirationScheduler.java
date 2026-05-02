package ru.makolov.otp.service.stub;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.makolov.otp.dao.OtpCodeDao;

public class OtpExpirationScheduler {
    private static final Logger log = LoggerFactory.getLogger(OtpExpirationScheduler.class);
    private final OtpCodeDao otpCodeDao;
    private final ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();

    public OtpExpirationScheduler(OtpCodeDao otpCodeDao) {
        this.otpCodeDao = otpCodeDao;
    }

    public void start() {
        executorService.scheduleAtFixedRate(this::expire, 30, 30, TimeUnit.SECONDS);
    }

    private void expire() {
        int updated = otpCodeDao.expireActiveCodes();
        if (updated > 0) {
            log.info("Marked {} OTP records as EXPIRED", updated);
        }
    }
}
