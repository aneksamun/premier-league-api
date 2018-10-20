package models

import org.scalatest.prop.TableDrivenPropertyChecks
import org.scalatest.{Matchers, PropSpec}

class PageSpec extends PropSpec
  with TableDrivenPropertyChecks
    with Matchers {

  val firstPages = Table(
    "First pages",
    Page[String](
      offset = 1,
      limit = 1,
      total = 2,
      items = Seq("First", "Second")
    )
  )

  val lastPages = Table(
    "Last pages",
    Page[String](
      offset = 2,
      limit = 2,
      total = 4,
      items = Seq("First", "Second", "Third", "Fourth")
    ),
    Page[String](
      offset = 2,
      limit = 10,
      total = 4,
      items = Seq("First", "Second", "Third", "Fourth")
    ),
    Page[String](
      offset = 3,
      limit = 2,
      total = 5,
      items = Seq("First", "Second", "Third")
    )
  )

  property("Property `isFirst` equal to `true`") {
    forAll(firstPages) { _.isFirst should be (true) }
  }

  property("Property `isFirst` equal to `false`") {
    forAll(lastPages) { _.isFirst should be (false) }
  }

  property("Property `isLast` equal to `true`") {
    forAll(lastPages) { _.isLast should be (true) }
  }

  property("Property `isLast` equal to `false`") {
    forAll(firstPages) { _.isLast should be (false) }
  }
}
