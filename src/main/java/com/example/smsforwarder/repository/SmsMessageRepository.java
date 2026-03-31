package com.example.smsforwarder.repository;

import com.example.smsforwarder.entity.SmsMessage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SmsMessageRepository extends JpaRepository<SmsMessage, Long> {
    Page<SmsMessage> findAllByOrderByCreatedAtDesc(Pageable pageable);
}
