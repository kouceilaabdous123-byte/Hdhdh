package com.example

import android.app.Application
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.DiagnosticTest
import com.example.data.model.TestStatus
import com.example.ui.viewmodel.DiagViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

  @Test
  fun testAppNameResource() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("DiagPhone", appName)
  }

  @Test
  fun testDiagnosticViewModelSummaryCalculation() {
    val app = ApplicationProvider.getApplicationContext<Application>()
    val viewModel = DiagViewModel(app)
    val initialSummary = viewModel.getSummary()
    assertEquals(DiagnosticTest.entries.size, initialSummary.total)
    assertEquals(0, initialSummary.passed)

    viewModel.setTestResult(DiagnosticTest.FLASHLIGHT, TestStatus.PASSED)
    viewModel.setTestResult(DiagnosticTest.BUTTONS, TestStatus.PASSED)
    val updatedSummary = viewModel.getSummary()
    assertEquals(2, updatedSummary.passed)
    assertTrue(updatedSummary.scorePercent > 0)
  }
}
