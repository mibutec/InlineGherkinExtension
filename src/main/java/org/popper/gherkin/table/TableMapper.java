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

import org.popper.gherkin.customizer.Customizer;

/**
 * Configuration how to map a gherkin table to
 *
 * @author Michael
 *
 */
public class TableMapper<T> {
    private static final PojoMapper<?> DefaultPojoMapper = new DefaultPojoMapper<>();

    private static final PojoFactory<?> DefaultPojoFactory = new DefaultPojoFactory<>();

    private Class<T> targetType;

    private PojoMapper<T> pojoMapper;
    
    private PojoFactory<T> pojoFactory;

    private final Map<String, String> nameOverrides = new HashMap<>();

    @SuppressWarnings("unchecked")
    public TableMapper(Class<T> targetType) {
        this.targetType = targetType;
        pojoMapper = (PojoMapper<T>) DefaultPojoMapper;
        pojoFactory = (PojoFactory<T>) DefaultPojoFactory;
    }

    public Class<T> getTargetType() {
        return targetType;
    }

    public String removeTable(String step) {
        if (step.contains("||")) {
            return step.substring(0, step.indexOf('|'));
        } else {
            return step;
        }

    }

    public TableMapper<T> withTargetType(Class<T> targetType) {
        this.targetType = targetType;
        return this;
    }

    public PojoMapper<T> getPojoMapper() {
        return pojoMapper;
    }

    public TableMapper<T> withPojoMapper(PojoMapper<T> pojoMapper) {
        this.pojoMapper = pojoMapper;
        return this;
    }

    public TableMapper<T> mapHeader(String source, String target) {
        nameOverrides.put(source, target);
        return this;
    }
    
    public PojoFactory<T> getPojoFactory() {
		return pojoFactory;
	}

	public TableMapper<T> withPojoFactory(PojoFactory<T> pojoFactory) {
		this.pojoFactory = pojoFactory;
		return this;
	}

	@SuppressWarnings({"unchecked", "rawtypes"})
    public Table<T> createTable(String step) {
        Table<Map<String, String>> mapTable = createMapTable(step);

        if (targetType != Map.class) {
            List<T> convertedRows = mapTable.getRows().stream().map(m -> pojoMapper.mapToPojo(m, pojoFactory.createPojo(targetType, m)))
                    .collect(Collectors.toList());
            return new Table<>(mapTable.getHeaders(), convertedRows);
        } else {
            return new Table(mapTable.getHeaders(), mapTable.getRows());
        }
    }

    public Table<Map<String, String>> createMapTable(String step) {
        step = step.replace("\n", "");
        int indexOfTable = step.indexOf("||");
        if (indexOfTable < 0) {
            return null;
        }

        String tableString = step.substring(step.indexOf('|') + 1);
        String[] rows = tableString.split("\\|\\|");
        if (rows.length < 2) {
            throw createError(tableString);
        }

        List<String> headers = parseHeader(rows[0]);

        List<Map<String, String>> body = new ArrayList<>();
        for (int i = 1; i < rows.length; i++) {
            body.add(parseBody(rows[i], headers, getTargetType(), getPojoMapper(), tableString));
        }

        return new Table<>(headers, body);
    }

    private IllegalStateException createError(String tableString) {
        String exceptionText = "Table needs to be formatter die following way:\n|Header1|Header2|Header3|\n|value1|value2|value3|\n, but was\n"
                + tableString;
        return new IllegalStateException(exceptionText);
    }

    protected List<String> parseHeader(String header) {
        return Arrays.stream(header.split("\\|")).map(String::trim).collect(Collectors.toList());
    }

    protected Map<String, String> parseBody(String bodyRow, List<String> headers, Class<T> targetType,
            PojoMapper<T> mapper, String tableString) {
        String[] split = bodyRow.split("\\|");
        List<String> values = Arrays.stream(split).map(String::trim).collect(Collectors.toList());

        if (values.size() != headers.size()) {
            throw createError(tableString);
        }

        Map<String, String> actualMap = new HashMap<>();

        int index = 0;
        for (String value : values) {
            actualMap.put(getFieldName(headers.get(index++)), value);
        }

        return actualMap;
    }

    protected String getFieldName(String name) {
        return nameOverrides.getOrDefault(name, name);
    }
}
