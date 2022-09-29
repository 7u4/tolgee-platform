package io.tolgee.controllers

import io.tolgee.fixtures.andIsForbidden
import io.tolgee.fixtures.generateUniqueString
import io.tolgee.model.Language
import io.tolgee.model.enums.ApiScope
import io.tolgee.testing.annotations.ProjectApiKeyAuthTestMethod
import io.tolgee.testing.annotations.ProjectJWTAuthTestMethod
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.test.web.servlet.MvcResult
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import org.springframework.transaction.annotation.Transactional
import java.io.ByteArrayInputStream
import java.util.function.Consumer
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream

class ExportControllerTest : ProjectAuthControllerTest() {
  @Test
  @Transactional
  @ProjectJWTAuthTestMethod
  fun exportZipJson() {
    val base = dbPopulator.populate(generateUniqueString())
    commitTransaction()
    projectSupplier = { base.project }
    userAccount = base.userAccount
    val mvcResult = performProjectAuthGet("export/jsonZip")
      .andExpect(MockMvcResultMatchers.status().isOk).andDo { obj: MvcResult -> obj.getAsyncResult(60000) }.andReturn()
    mvcResult.response
    val fileSizes = parseZip(mvcResult.response.contentAsByteArray)
    project.languages.forEach(
      Consumer { l: Language ->
        val name = l.tag + ".json"
        Assertions.assertThat(fileSizes).containsKey(name)
      }
    )
  }

  @Test
  @Transactional
  @ProjectApiKeyAuthTestMethod
  fun exportZipJsonWithApiKey() {
    val base = dbPopulator.populate(generateUniqueString())
    commitTransaction()
    projectSupplier = { base.project }
    val mvcResult = performProjectAuthGet("export/jsonZip")
      .andExpect(MockMvcResultMatchers.status().isOk).andDo { obj: MvcResult -> obj.asyncResult }.andReturn()
    val fileSizes = parseZip(mvcResult.response.contentAsByteArray)
    project.languages.forEach(
      Consumer { l: Language ->
        val name = l.tag + ".json"
        Assertions.assertThat(fileSizes).containsKey(name)
      }
    )
  }

  @Test
  @ProjectApiKeyAuthTestMethod(scopes = [ApiScope.KEYS_EDIT])
  fun exportZipJsonApiKeyPermissionFail() {
    performProjectAuthGet("export/jsonZip").andIsForbidden
  }

  private fun parseZip(responseContent: ByteArray): Map<String, Long> {
    val byteArrayInputStream = ByteArrayInputStream(responseContent)
    val zipInputStream = ZipInputStream(byteArrayInputStream)
    val result = HashMap<String, Long>()
    var nextEntry: ZipEntry?
    while (zipInputStream.nextEntry.also {
      nextEntry = it
    } != null
    ) {
      result[nextEntry!!.name] = nextEntry!!.size
    }
    return result
  }
}
