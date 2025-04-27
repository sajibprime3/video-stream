package com.dark.videostreaming.dto;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PreviewDto {
    
    long id;

    String name;

    long size;

    String status;
    
}
