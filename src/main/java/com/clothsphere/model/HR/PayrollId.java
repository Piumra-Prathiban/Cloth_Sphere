// PayrollId.java
package com.clothsphere.model.HR;

import java.io.Serializable;
import java.util.Objects;

public class PayrollId implements Serializable {
    private String employeeId;
    private String payrollMonth;

    public PayrollId() {}

    public PayrollId(String employeeId, String payrollMonth) {
        this.employeeId = employeeId;
        this.payrollMonth = payrollMonth;
    }

    public String getEmployeeId() { return employeeId; }
    public void setEmployeeId(String employeeId) { this.employeeId = employeeId; }

    public String getPayrollMonth() { return payrollMonth; }
    public void setPayrollMonth(String payrollMonth) { this.payrollMonth = payrollMonth; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PayrollId payrollId = (PayrollId) o;
        return Objects.equals(employeeId, payrollId.employeeId) &&
                Objects.equals(payrollMonth, payrollId.payrollMonth);
    }

    @Override
    public int hashCode() {
        return Objects.hash(employeeId, payrollMonth);
    }
}