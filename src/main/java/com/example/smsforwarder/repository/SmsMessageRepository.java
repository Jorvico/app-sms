package com.example.smsforwarder.repository;

import com.example.smsforwarder.entity.SmsMessage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

public interface SmsMessageRepository extends JpaRepository<SmsMessage, Long> {
    Page<SmsMessage> findAllByOrderByCreatedAtDesc(Pageable pageable);

    List<SmsMessage> findTop20ByEmailStatusInAndNextEmailRetryAtBeforeOrderByCreatedAtAsc(
            Collection<String> statuses,
            LocalDateTime now
    );
}
