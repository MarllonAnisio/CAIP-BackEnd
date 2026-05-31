package org.marllon.caip.domains.report.entity.constants;

import lombok.Getter;

@Getter
public enum TypeReport {

    LOST ("LOST"),
    FOUND ("FOUND"),
    COLLECTED ("COLLECTED"),
    RETURNED ("RETURNED"),
    COMPLETED ("COMPLETED");

    private String value;

    TypeReport(String value) {
        this.value = value;
    }
}
