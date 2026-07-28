package com.dualis.api.dto.request;

import com.dualis.api.domain.model.AccountStatus;
import com.dualis.api.domain.model.AccountType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Request payload for updating an existing account")
public class UpdateAccountRequest {

    @Size(max = 100, message = "Account name cannot exceed 100 characters")
    @Schema(description = "Updated name of the account", example = "Joint Savings BCP")
    private String name;

    @Schema(description = "Updated type of account", example = "SAVINGS")
    private AccountType type;

    @Schema(description = "Updated account status (ACTIVE, INACTIVE, ARCHIVED)", example = "ACTIVE")
    private AccountStatus status;

    @Size(max = 255, message = "Description cannot exceed 255 characters")
    @Schema(description = "Updated notes or account description", example = "Emergency fund savings account")
    private String description;

    @Schema(description = "Flag indicating whether this account is included in overall liquidity totals", example = "true")
    private Boolean isIncludedInTotal;
}
