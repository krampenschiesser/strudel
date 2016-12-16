/**
 * Copyright [2016] [Christian Loehnert]
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.ks.strudel.fileupload;

import com.google.common.net.MediaType;
import de.ks.strudel.Strudel;

import java.nio.file.Files;
import java.nio.file.Path;

public class Fileupload {
  public static void main(final String[] args) {
    Strudel strudel = Strudel.create();
    strudel.classpathLocation("/WEB-INF/fileupload/", "/form");
    strudel.get("/", (request, response) -> response.redirect("/form/form.html"));
    strudel.post("/post", (request, response) -> {
      Path path = request.formDataFile("file");
      if (path == null) {
        return "No file given";
      } else {
        response.contentType(MediaType.ANY_IMAGE_TYPE.type());
        return Files.readAllBytes(path);
      }
    });
    strudel.start();
  }
}
