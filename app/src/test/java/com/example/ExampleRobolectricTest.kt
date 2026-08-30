package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Place Name Generator", appName)
  }

  @Test
  fun `test name generator engine generates non empty batch`() {
    val names = NameGeneratorEngine.generateBatch(NameStyle.NORMAL, 4)
    assertEquals(4, names.size)
    names.forEach { name ->
      assertTrue(name.isNotEmpty())
    }
  }

  @Test
  fun `test short name length constraint`() {
    repeat(10) {
      val shortName = NameGeneratorEngine.generateName(NameStyle.SHORT)
      assertTrue(shortName.length <= 6)
    }
  }
}

