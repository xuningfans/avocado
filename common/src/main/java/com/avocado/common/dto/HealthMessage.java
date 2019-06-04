package com.avocado.common.dto;

import lombok.*;

/**
 * HealthMessge class
 *
 * @author xuning
 * @date 2019-05-06 15:08
 */
@Data
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class HealthMessage {

    private Long usableSpace;

    private Integer workerPort;

    private Integer connectionCount;
}
