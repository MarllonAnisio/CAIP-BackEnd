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

    /**
     * Busca todos os relatórios criados por um usuário específico.
     * @param user O usuário criador.
     * @return Uma lista de relatórios.
     */
    @Query("SELECT r FROM Report r WHERE r.audit.createdBy = :user")
    List<Report> findAllByCreator(@Param("user") User user);

    /**
     * Busca todos os relatórios ativos (não fechados) criados por um usuário específico.
     * @param user O usuário criador.
     * @return Uma lista de relatórios ativos.
     */
    @Query("SELECT r FROM Report r WHERE r.audit.createdBy = :user AND r.isClosed = false")
    List<Report> findActiveReportsByCreator(@Param("user") User user);

    /**
     * Busca todos os relatórios que estão ativos (não fechados) no sistema.
     * @return Uma lista de relatórios ativos.
     */
    List<Report> findAllByIsClosedFalse();

    /**
     * Busca todos os relatórios que estão finalizados (fechados) no sistema.
     * @return Uma lista de relatórios fechados.
     */
    List<Report> findAllByIsClosedTrue();

    /**
     * Realiza a exclusão física (hard delete) de um relatório.
     * ATENÇÃO: Este método bypassa o soft-delete. Use com cuidado.
     * @param id O ID do relatório a ser deletado permanentemente.
     */
    @Modifying
    @Query(value = "DELETE FROM tb_report WHERE id = :id", nativeQuery = true)
    void hardDeleteById(@Param("id") Long id);
}
