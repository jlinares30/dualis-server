package com.dualis.api.modules.settlement.controller;

import com.dualis.api.modules.settlement.application.usecase.ManageSplitRuleUseCase;
import com.dualis.api.modules.settlement.dto.request.CalculateSplitRequest;
import com.dualis.api.modules.settlement.dto.request.CreateSplitRuleRequest;
import com.dualis.api.modules.settlement.dto.request.UpdateSplitRuleRequest;
import com.dualis.api.modules.settlement.dto.response.SplitCalculationResult;
import com.dualis.api.modules.settlement.dto.response.SplitRuleResponse;
import com.dualis.api.shared.dto.response.ErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/split-rules")
@RequiredArgsConstructor
@Tag(name = "Split Engine", description = "Couple shared expenses split rules and settlement calculation endpoints")
public class SplitRuleController {

    private final ManageSplitRuleUseCase splitRuleUseCase;

    @PostMapping
    @Operation(summary = "Create a split rule", description = "Creates a new couple expense split rule (EQUAL, PROPORTIONAL, CUSTOM_PERCENTAGE, FIXED_AMOUNT)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Split rule created successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = SplitRuleResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid payload parameters",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<SplitRuleResponse> createSplitRule(@Valid @RequestBody CreateSplitRuleRequest request) {
        SplitRuleResponse response = splitRuleUseCase.createSplitRule(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @Operation(summary = "List split rules by workspace", description = "Retrieves all split rules configured for a given workspace")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Split rules retrieved successfully"),
            @ApiResponse(responseCode = "400", description = "Missing or invalid workspaceId parameter",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<List<SplitRuleResponse>> getSplitRulesByWorkspace(
            @Parameter(description = "Workspace UUID", required = true, example = "550e8400-e29b-41d4-a716-446655440000")
            @RequestParam UUID workspaceId) {
        List<SplitRuleResponse> rules = splitRuleUseCase.getSplitRulesByWorkspace(workspaceId);
        return ResponseEntity.ok(rules);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get split rule details", description = "Retrieves details of a split rule by its unique ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Split rule details retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Split rule not found",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<SplitRuleResponse> getSplitRuleById(
            @Parameter(description = "Split rule UUID", required = true) @PathVariable UUID id) {
        SplitRuleResponse response = splitRuleUseCase.getSplitRuleById(id);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update split rule", description = "Updates configured values of an existing split rule")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Split rule updated successfully"),
            @ApiResponse(responseCode = "404", description = "Split rule not found",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<SplitRuleResponse> updateSplitRule(
            @Parameter(description = "Split rule UUID", required = true) @PathVariable UUID id,
            @Valid @RequestBody UpdateSplitRuleRequest request) {
        SplitRuleResponse response = splitRuleUseCase.updateSplitRule(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete split rule", description = "Removes a split rule")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Split rule deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Split rule not found")
    })
    public ResponseEntity<Void> deleteSplitRule(
            @Parameter(description = "Split rule UUID", required = true) @PathVariable UUID id) {
        splitRuleUseCase.deleteSplitRule(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/calculate")
    @Operation(summary = "Calculate expense split breakdown", description = "Calculates exact amounts and settlement summary for partner A and partner B given an expense amount")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Split calculated successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = SplitCalculationResult.class))),
            @ApiResponse(responseCode = "400", description = "Invalid calculate payload",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<SplitCalculationResult> calculateSplit(@Valid @RequestBody CalculateSplitRequest request) {
        SplitCalculationResult result = splitRuleUseCase.calculateSplit(request);
        return ResponseEntity.ok(result);
    }
}
