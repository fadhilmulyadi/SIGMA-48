package com.sigma48.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * Model untuk identitas samaran (cover).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CoverIdentity {
    private String coverName;
    private String coverPassport;
    private String coverRole;
}
