package com.jobseeker.repo;

import com.jobseeker.model.Application;
import com.jobseeker.model.ApplicationStatus;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ApplicationRepository extends JpaRepository<Application, Long> {

    List<Application> findAllByOrderByAppliedOnDescIdDesc();

    long countByStatus(ApplicationStatus status);

    boolean existsByJobId(Long jobId);
}
