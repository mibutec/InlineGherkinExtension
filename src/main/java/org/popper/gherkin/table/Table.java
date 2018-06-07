/*
 * Copyright [2018] [Michael Bulla, michaelbulla@gmail.com]
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.popper.gherkin.table;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.platform.commons.util.StringUtils;

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
	private static final String errorMessage = "table needs to be formatter die following way: ||Header1|Header2|Header3||\n|value1|value2|value3|";
	
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
	
	public Stream<T> stream() {
		return rows.stream();
	}
	
	public static<T> Table<T> createTable(String step, Class<T> targetType, PojoMapper<T> mapper) {
		int indexOfTable = step.indexOf("||");
		if (indexOfTable < 0) {
			return null;
		}
		
		String tableString = step.substring(indexOfTable + 2);
		String[] split = tableString.split("\\|\\|");
		if (split.length != 2) {
			throw new IllegalStateException(errorMessage);
		}

		List<String> headers = parseHeader(split[0]);
		List<T> body = parseBody(split[1], headers, targetType, mapper);
		
		return new Table<T>(headers, body);
	}
	
	private static List<String> parseHeader(String header) {
		return Arrays.stream(header.split("\\|")).map(String::trim).collect(Collectors.toList());
	}
	
	private static<T> List<T> parseBody(String body, List<String> headers, Class<T> targetType, PojoMapper<T> mapper) {
		body = body.replaceAll("\\|+", "|");
		String[] split = body.split("\\|");
		
		List<String> values = Arrays.stream(split).map(String::trim).filter(StringUtils::isNotBlank).collect(Collectors.toList());
		
		if (values.size() % headers.size() != 0) {
			throw new IllegalStateException(errorMessage);
		}
		
		List<T> ret = new ArrayList<>();
		Map<String, String> actualMap = new HashMap<>();
		
		int cnt = 0;
		for (String value : values) {
			int index = cnt % headers.size();
			actualMap.put(headers.get(index), value);
			if (index == headers.size() - 1) {
				if (targetType != Map.class) {
					ret.add(mapper.mapToPojo(actualMap, targetType));
				} else {
					ret.add(targetType.cast(actualMap));
				}
				actualMap = new HashMap<>();
			}
			cnt++;
		}
		
		return ret;
	}
}
