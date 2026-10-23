package com.example.l2_rbpo.dto;

import com.example.l2_rbpo.entity.ApplicationStatus;

public record TeamApplicationPatchRequest(
        ApplicationStatus status
) {
}