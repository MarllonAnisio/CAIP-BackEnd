package org.marllon.caip.repository;

import org.marllon.caip.model.Report;
import org.marllon.caip.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
@Repository
public interface ReportRepository extends JpaRepository<Report, Long> {
    // Busca reports criados por um usuário específico
    List<Report> findAllByAudit_CreatedBy(User createdBy);

    // Apenas ativos do usuário
    List<Report> findAllByAudit_CreatedByAndIsClosedFalse(User createdBy);

    // Ativos (globais)
    List<Report> findAllByIsClosedIsFalse();

    // Finalizados (globais)
    List<Report> findAllByIsClosedIsTrue();

}
