/*
 * Copyright © 2018 Michael Bulla (michaelbulla@gmail.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.popper.gherkin.table;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

/**
 * Representation of a Table used in Gherkin syntax. Example:
 * Some step:
 * || Header 1 | Header 2 | Header 3 |
 * | val 1     | val 2    | val 3    |
 * | val 4     | val 5    | val 6    |
 *
 * The Table may be mapped to a List<Map> structure or List<SomePojo>
 *
 * @author Michael
 */
public class Table<T> {
    private List<String> headers;

    private List<T> rows = new ArrayList<>();

    public Table(List<String> headers, List<T> rows) {
        this.headers = headers;
        this.rows = rows;
    }

    public List<String> getHeaders() {
        return headers;
    }

    public int size() {
        return rows.size();
    }

    public T getRow(int row) {
        return rows.get(row);
    }

    public List<T> getRows() {
        return rows;
    }

    public Stream<T> stream() {
        return rows.stream();
    }
}
