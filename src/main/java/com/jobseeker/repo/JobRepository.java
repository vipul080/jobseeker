package com.jobseeker.repo;

import com.jobseeker.model.Job;
import com.jobseeker.model.JobType;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface JobRepository extends JpaRepository<Job, Long> {

    @Query("""
            select j from Job j
            where (:q = '' or lower(j.title) like lower(concat('%', :q, '%'))
                           or lower(j.company) like lower(concat('%', :q, '%'))
                           or lower(j.description) like lower(concat('%', :q, '%')))
              and (:location = '' or lower(j.location) like lower(concat('%', :location, '%')))
              and (:type is null or j.type = :type)
              and (:remoteOnly = false or j.remote = true)
            order by j.postedOn desc, j.id desc
            """)
    List<Job> search(@Param("q") String q,
                     @Param("location") String location,
                     @Param("type") JobType type,
                     @Param("remoteOnly") boolean remoteOnly);

    List<Job> findBySavedTrueOrderByPostedOnDesc();
}
