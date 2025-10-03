package com.dark.videostreaming.repository;

import com.dark.videostreaming.entity.Thumbnail;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ThumbnailRepository extends JpaRepository<Thumbnail, Long> {
}
