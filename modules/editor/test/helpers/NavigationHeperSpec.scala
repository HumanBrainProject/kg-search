
/*
*   Copyright (c) 2018, EPFL/Human Brain Project PCO
*
*   Licensed under the Apache License, Version 2.0 (the "License");
*   you may not use this file except in compliance with the License.
*   You may obtain a copy of the License at
*
*       http://www.apache.org/licenses/LICENSE-2.0
*
*   Unless required by applicable law or agreed to in writing, software
*   distributed under the License is distributed on an "AS IS" BASIS,
*   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
*   See the License for the specific language governing permissions and
*   limitations under the License.
*/

package editor.helpers

import common.models.NexusPath
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite

class NavigationHeperSpec  extends PlaySpec with GuiceOneAppPerSuite{

  "NavigationHelper#generateBackLink" should{
    "return a correctly formatted back link" in {
      val path = NexusPath("minds", "core", "dataset", "v0.0.4")
      val expected = "minds/core/dataset"
      assert(expected == NavigationHelper.generateBackLink(path, "reconciled"))
    }
    "return an empty string if the path is not valid" in {
      val path = NexusPath("this", "doesnot", "exists", "v0.0.4")
      val expected = ""
      assert(expected == NavigationHelper.generateBackLink(path, "reconciled"))
    }
    "return a path with an original organization" in {
      val path = NexusPath("mindsreconciled", "core", "dataset", "v0.0.4")
      val expected = "minds/core/dataset"
      assert(expected == NavigationHelper.generateBackLink(path, "reconciled"))
    }
  }

}
