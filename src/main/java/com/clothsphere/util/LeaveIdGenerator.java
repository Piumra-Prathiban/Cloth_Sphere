package com.clothsphere.util;

import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.id.IdentifierGenerator;
import java.io.Serializable;
import java.util.stream.Stream;

public class LeaveIdGenerator implements IdentifierGenerator {

    @Override
    public Serializable generate(SharedSessionContractImplementor session, Object object) {
        String query = "SELECT l.leaveId FROM Leave l ORDER BY l.leaveId DESC";

        try (Stream<String> stream = session.createQuery(query, String.class).stream()) {
            String lastId = stream.findFirst().orElse("lev0");

            // Extract the numeric part and increment
            String numericPart = lastId.replaceAll("lev", "");
            int nextNumber = Integer.parseInt(numericPart) + 1;

            return "lev" + nextNumber;
        } catch (Exception e) {
            // If parsing fails, start from lev1
            return "lev1";
        }
    }
}