package org.marllon.caip.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.marllon.caip.repository.ReportRepository;
import org.marllon.caip.repository.StatusStepRepository;
import org.marllon.caip.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class ReportService {

    private static final Logger log = LoggerFactory.getLogger(ReportService.class);
    private final ReportRepository reportRepository;
    private final ObjectMapper objectMapper;
    private final StatusStepRepository statusStepRepository;
    private final UserRepository userRepository;



}
