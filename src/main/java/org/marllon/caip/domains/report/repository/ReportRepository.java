package org.marllon.caip.domains.report.repository;

import org.marllon.caip.domains.report.entity.Report;
import org.marllon.caip.domains.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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

    @Modifying
    @Query(value = "DELETE FROM tb_report WHERE id = :id", nativeQuery = true)
    void hardDeleteById(@Param("id") Long id);
}
