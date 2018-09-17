package org.codefeedr.plugins.github.generate

import org.joda.time.{DateTime, DateTimeZone}
import org.scalatest.FlatSpec

class UserGeneratorSpec extends FlatSpec {
  implicit val eventTime: DateTime = DateTime.now(DateTimeZone.UTC)

  "UserGenerator" should "Generate the same element with the same seed" in {
    assert(new UserGenerator(10).generate() == new UserGenerator(10).generate())
  }

  it should "Generate different elements with different seeds" in {
    assert(new UserGenerator(10).generate() != new UserGenerator(11).generate())
  }
}
