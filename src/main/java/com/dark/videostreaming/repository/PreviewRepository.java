package com.dark.videostreaming.repository;

import com.dark.videostreaming.entity.Preview;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PreviewRepository extends JpaRepository<Preview, Long> {
}
