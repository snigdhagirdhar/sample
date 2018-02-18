package messagededupe;

import com.google.common.base.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

import java.util.function.Supplier;

public class DeDuplicationService {
    private int timeToLive;
    private ReceivedMessageRepository receivedMessageRepository;
    private Supplier<Long> dateTimeSupplier;
    private final Logger log = LoggerFactory.getLogger(DeDuplicationService.class);

    public DeDuplicationService(ReceivedMessageRepository receivedMessageRepository, int timeToLiveDays) {
        this(receivedMessageRepository, System::currentTimeMillis);
        this.timeToLive = timeToLiveDays;
    }

    public DeDuplicationService(ReceivedMessageRepository receivedMessageRepository, Supplier<Long> dateTimeSupplier) {
        this.receivedMessageRepository = receivedMessageRepository;
        this.dateTimeSupplier = dateTimeSupplier;
    }

    @Transactional
    public void deDupe(String message, String source) {
        if (Strings.isNullOrEmpty(message)) {
            return;
        }
        log.debug("Received request for message: {} and source: {}", message, source);
        if (exists(message, source)) {
            throw new DuplicateMessageReceivedException(message, source);
        }
        save(message, source);
        log.debug("Successfully saved the received message: {} and source: {}", message, source);
    }

    @Transactional
    public void purge() {
        receivedMessageRepository.purge();
    }

    private boolean exists(String message, String source) {
        return receivedMessageRepository.exists(ReceivedMessage.createKey(message, source));
    }

    private void save(String message, String source) {
        ReceivedMessage receivedMessage = new ReceivedMessage(message, source, getCurrentTime(), timeToLive);
        receivedMessageRepository.save(receivedMessage);
    }

    private Long getCurrentTime() {
        return dateTimeSupplier.get();
    }

}
