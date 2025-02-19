package io.tolgee.api.v2.controllers

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import io.tolgee.dtos.MtCreditBalanceDto
import io.tolgee.hateoas.machineTranslation.CreditBalanceModel
import io.tolgee.security.apiKeyAuth.AccessWithApiKey
import io.tolgee.security.project_auth.AccessWithAnyProjectPermission
import io.tolgee.security.project_auth.ProjectHolder
import io.tolgee.service.machineTranslation.MtCreditBucketService
import io.tolgee.service.organization.OrganizationRoleService
import io.tolgee.service.organization.OrganizationService
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@CrossOrigin(origins = ["*"])
@RequestMapping(value = ["/v2"])
@Tag(name = "Machine translation credits")
@Suppress("SpringJavaInjectionPointsAutowiringInspection", "MVCPathVariableInspection")
class MtCreditsController(
  private val projectHolder: ProjectHolder,
  private val mtCreditBucketService: MtCreditBucketService,
  private val organizationRoleService: OrganizationRoleService,
  private val organizationService: OrganizationService
) {
  @GetMapping("/projects/{projectId}/machine-translation-credit-balance")
  @Operation(summary = "Returns machine translation credit balance for specified project")
  @AccessWithApiKey
  @AccessWithAnyProjectPermission
  fun getProjectCredits(@PathVariable projectId: Long): CreditBalanceModel {
    return mtCreditBucketService.getCreditBalances(projectHolder.projectEntity).model
  }

  @GetMapping("/organizations/{organizationId}/machine-translation-credit-balance")
  @Operation(summary = "Returns machine translation credit balance for organization")
  fun getOrganizationCredits(@PathVariable organizationId: Long): CreditBalanceModel {
    organizationRoleService.checkUserIsMemberOrOwner(organizationId)
    val organization = organizationService.get(organizationId)
    return mtCreditBucketService.getCreditBalances(organization).model
  }

  private val MtCreditBalanceDto.model
    get() = CreditBalanceModel(
      creditBalance = this.creditBalance / 100,
      bucketSize = this.bucketSize / 100,
      extraCreditBalance = this.extraCreditBalance / 100
    )
}
