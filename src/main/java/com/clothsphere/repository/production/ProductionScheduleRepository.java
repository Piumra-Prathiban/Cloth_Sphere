package com.clothsphere.repository.production;

import com.clothsphere.model.production.ProductionSchedule;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductionScheduleRepository extends JpaRepository<ProductionSchedule, Long> {

    default List<ProductionSchedule> findAllOrdered() {
        return findAll(Sort.by(Sort.Direction.ASC, "startDate"));
    }
}
