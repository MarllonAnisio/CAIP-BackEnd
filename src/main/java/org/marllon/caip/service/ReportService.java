package org.marllon.caip.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.marllon.caip.dto.response.ReportResponse;
import org.marllon.caip.model.Report;
import org.marllon.caip.repository.ReportRepository;
import org.marllon.caip.repository.StatusStepRepository;
import org.marllon.caip.repository.UserRepository;
import org.marllon.caip.service.mapper.ReportMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class ReportService {

    private static final Logger log = LoggerFactory.getLogger(ReportService.class);
    private final ReportRepository reportRepository;
    private final ReportMapper reportMapper;
    private final StatusStepRepository statusStepRepository;
    private final UserRepository userRepository;


    @Transactional(readOnly = true)
    public ReportResponse findById(Long id) throws Exception {
        Report report = reportRepository.findById(id)
                .orElseThrow(() -> new Exception("Report not found with id: " + id));

        return reportMapper.toResponse(report);
    }

}
