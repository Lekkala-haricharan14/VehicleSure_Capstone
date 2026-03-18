package org.thehartford.willowshield.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.thehartford.willowshield.entity.VehicleDocument;

@Repository
public interface VehicleDocumentRepository extends JpaRepository<VehicleDocument, Integer> {
}
