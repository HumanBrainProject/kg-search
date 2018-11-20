package helpers

import models.instance.NexusInstanceReference
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import org.scalatest._
import Matchers._

class BookmarkHelperSpec extends PlaySpec with GuiceOneAppPerSuite{

  "bookmarkToAddAndDelete" should {
    "return the list of element to add correctly" in {
      val fromDB = List(
        NexusInstanceReference("a", "b", "c", "d", "1"),
        NexusInstanceReference("a", "b", "c", "d", "2")
      )

      val fromUser = List(
        NexusInstanceReference("a", "b", "c", "d", "1"),
        NexusInstanceReference("a", "b", "c", "d", "2"),
        NexusInstanceReference("a", "b", "c", "d", "3"),
        NexusInstanceReference("a", "b", "c", "d", "4"),
        NexusInstanceReference("a", "b", "c", "d", "5")
      )

      val expectedAdd = List(
        NexusInstanceReference("a", "b", "c", "d", "3"),
        NexusInstanceReference("a", "b", "c", "d", "4"),
        NexusInstanceReference("a", "b", "c", "d", "5")
      )

      val (toAdd, toDelete) = BookmarkHelper.bookmarksToAddAndDelete(fromDB, fromUser)

      toAdd should contain theSameElementsAs expectedAdd
      toDelete mustBe List()
    }
    "return the list of element to delete correctly" in {
      val fromDB = List(
        NexusInstanceReference("a", "b", "c", "d", "1"),
        NexusInstanceReference("a", "b", "c", "d", "2")
      )

      val fromUser = List()

      val expected = List(
        NexusInstanceReference("a", "b", "c", "d", "1"),
        NexusInstanceReference("a", "b", "c", "d", "2")
      )

      val (toAdd, toDelete) = BookmarkHelper.bookmarksToAddAndDelete(fromDB, fromUser)

      toAdd mustBe List()
      toDelete should contain theSameElementsAs expected
    }

    "return an empty list when no changes are made" in {
      val fromDB = List(
        NexusInstanceReference("a", "b", "c", "d", "1"),
        NexusInstanceReference("a", "b", "c", "d", "2")
      )

      val fromUser = List(
        NexusInstanceReference("a", "b", "c", "d", "1"),
        NexusInstanceReference("a", "b", "c", "d", "2")
      )

      val expected = List()

      val (toAdd, toDelete) = BookmarkHelper.bookmarksToAddAndDelete(fromDB, fromUser)

      toAdd mustBe expected
      toDelete mustBe expected
    }

    "return a correct delete list if the user sends an empty list" in {
      val fromDB = List(
        NexusInstanceReference("a", "b", "c", "d", "1"),
        NexusInstanceReference("a", "b", "c", "d", "2")
      )

      val fromUser = List()

      val expected = List(
        NexusInstanceReference("a", "b", "c", "d", "1"),
        NexusInstanceReference("a", "b", "c", "d", "2")
      )

      val (toAdd, toDelete) = BookmarkHelper.bookmarksToAddAndDelete(fromDB, fromUser)

      toAdd mustBe List()
      toDelete should contain theSameElementsAs expected
    }
  }

}
