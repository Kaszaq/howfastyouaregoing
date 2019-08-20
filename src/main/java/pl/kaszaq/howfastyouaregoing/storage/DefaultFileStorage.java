/*
 * Copyright 2017 Michał Kasza <kaszaq@gmail.com>.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package pl.kaszaq.howfastyouaregoing.storage;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import org.apache.commons.io.FileUtils;

public class DefaultFileStorage implements FileStorage {

    @Override
    public String loadFile(File file) throws IOException {
        return FileUtils.readFileToString(file, "UTF-8");
    }

    @Override
    public void storeFile(File file, String data) throws IOException {
        FileUtils.writeStringToFile(file, data, "UTF-8");
    }

}
