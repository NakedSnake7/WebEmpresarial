package com.webempresarial.store.repository;

import com.webempresarial.store.dto.lead.PipelineStageStatsDTO;  
import com.webempresarial.store.entity.Lead; 
import com.webempresarial.store.model.LeadStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface LeadRepository extends JpaRepository<Lead, Long> {

    List<Lead> findByStoreIdOrderByCreatedAtDesc(Long storeId);

    List<Lead> findByStoreIdAndStatus(Long storeId, LeadStatus status);

    long countByStoreIdAndCreatedAtBetween(
        Long storeId,
        LocalDateTime start,
        LocalDateTime end
    );
    
    @Query("""
    	    SELECT COUNT(l)
    	    FROM Lead l
    	    WHERE l.store.id = :storeId
    	    AND l.status = com.webempresarial.store.model.LeadStatus.CALL_BOOKED
    	""")
    	long countBookedCalls(Long storeId);

    	@Query("""
    	    SELECT COUNT(l)
    	    FROM Lead l
    	    WHERE l.store.id = :storeId
    	    AND l.status = com.webempresarial.store.model.LeadStatus.PROPOSAL_SENT
    	""")
    	long countSentProposals(Long storeId);

    	@Query("""
    	    SELECT COUNT(l)
    	    FROM Lead l
    	    WHERE l.store.id = :storeId
    	    AND l.createdAt BETWEEN :start AND :end
    	""")
    	long countLeadsBetween(
    	        Long storeId,
    	        LocalDateTime start,
    	        LocalDateTime end
    	);

    	@Query("""
    	    SELECT COUNT(l)
    	    FROM Lead l
    	    WHERE l.store.id = :storeId
    	    AND l.temperature = com.webempresarial.store.model.LeadTemperature.HOT
    	""")
    	long countHotLeads(Long storeId);

    	@Query("""
    	    SELECT COALESCE(SUM(l.projectedValue), 0)
    	    FROM Lead l
    	    WHERE l.store.id = :storeId
    	    AND l.status NOT IN (
    	        com.webempresarial.store.model.LeadStatus.CLOSED,
    	        com.webempresarial.store.model.LeadStatus.LOST
    	    )
    	""")
    	BigDecimal getPipelineValue(Long storeId);

    	@Query("""
    	    SELECT COUNT(l)
    	    FROM Lead l
    	    WHERE l.store.id = :storeId
    	    AND l.status = com.webempresarial.store.model.LeadStatus.CLOSED
    	""")
    	long countClosedLeads(Long storeId);

    	@Query("""
    	    SELECT COUNT(l)
    	    FROM Lead l
    	    WHERE l.store.id = :storeId
    	""")
    	long countAllLeads(Long storeId);

    	@Query("""
    	    SELECT l.source, COUNT(l)
    	    FROM Lead l
    	    WHERE l.store.id = :storeId
    	    GROUP BY l.source
    	    ORDER BY COUNT(l) DESC
    	""")
    	List<Object[]> getLeadsBySourceRaw(Long storeId);

    @Query("""
        SELECT l.status, COUNT(l)
        FROM Lead l
        WHERE l.store.id = :storeId
        GROUP BY l.status
    """)
    List<Object[]> countByStatus(Long storeId);
    
    @Query("""
    	    SELECT l.source, COUNT(l)
    	    FROM Lead l
    	    WHERE l.store.id = :storeId
    	    GROUP BY l.source
    	    ORDER BY COUNT(l) DESC
    	""")
    	List<Object[]> getLeadsBySource(Long storeId);
    	
    	@Query("""
    		    SELECT SUM(p.amount * p.closeProbability / 100)
    		    FROM Proposal p
    		    WHERE p.lead.store.id = :storeId
    		    AND p.status IN ('SENT', 'VIEWED')
    		""")
    		BigDecimal getRevenueForecast(Long storeId);
    	
    	@Query("""
    		    SELECT l
    		    FROM Lead l
    		    WHERE l.store.id = :storeId
    		    AND l.status IN (
    		        com.webempresarial.store.model.LeadStatus.NEW,
    		        com.webempresarial.store.model.LeadStatus.CONTACTED,
    		        com.webempresarial.store.model.LeadStatus.FOLLOW_UP,
    		        com.webempresarial.store.model.LeadStatus.PROPOSAL_SENT
    		    )
    		    AND l.nextFollowUpAt IS NOT NULL
    		    AND l.nextFollowUpAt <= :now
    		""")
    		List<Lead> findLeadsNeedingFollowUp(
    		        Long storeId,
    		        LocalDateTime now
    		);
    	
        @Query("""
        	    SELECT new com.webempresarial.store.dto.lead.PipelineStageStatsDTO(
        	        l.status,
        	        COUNT(l),
        	        COALESCE(SUM(l.projectedValue), 0)
        	    )
        	    FROM Lead l
        	    WHERE l.store.id = :storeId
        	    GROUP BY l.status
        	""")
        	List<PipelineStageStatsDTO> getPipelineStats(Long storeId);



        		
        		Optional<Lead> findByIdAndStoreId(Long id, Long storeId);
        		
        		
        		@Query("""
        			    SELECT COUNT(l)
        			    FROM Lead l
        			""")
        			long countAllPlatformLeads();

        			@Query("""
        			    SELECT COALESCE(SUM(l.projectedValue), 0)
        			    FROM Lead l
        			    WHERE l.status NOT IN (
        			        com.webempresarial.store.model.LeadStatus.CLOSED,
        			        com.webempresarial.store.model.LeadStatus.LOST
        			    )
        			""")
        			BigDecimal getGlobalPipelineValue();
        		
        		
        
}