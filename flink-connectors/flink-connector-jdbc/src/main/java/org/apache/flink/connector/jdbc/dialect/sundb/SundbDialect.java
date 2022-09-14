/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.flink.connector.jdbc.dialect.sundb;

import org.apache.flink.annotation.Internal;
import org.apache.flink.connector.jdbc.converter.JdbcRowConverter;
import org.apache.flink.connector.jdbc.dialect.AbstractDialect;
import org.apache.flink.connector.jdbc.internal.converter.SundbRowConverter;
import org.apache.flink.table.types.logical.LogicalTypeRoot;
import org.apache.flink.table.types.logical.RowType;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/** JDBC dialect for Sundb. */
@Internal
public class SundbDialect extends AbstractDialect {
    @Override
    public String dialectName() {
        return "SunDB";
    }

    @Override
    public Optional<Range> timestampPrecisionRange() {
        return Optional.of(Range.of(0, 7));
    }

    @Override
    public Optional<Range> decimalPrecisionRange() {
        return Optional.of(Range.of(0, 38));
    }

    @Override
    public Optional<String> defaultDriverName() {
        return Optional.of("csii.sundb.jdbc.SundbDriver");
    }

    @Override
    public String quoteIdentifier(String identifier) {
        return identifier;
    }

    /**
     * Sundb upsert query use DUPLICATE KEY UPDATE.
     *
     * <p>NOTE: It requires Sundb's primary key to be consistent with pkFields.
     *
     * <p>We don't use REPLACE INTO, if there are other fields, we can keep their previous values.
     */
    @Override
    public Optional<String> getUpsertStatement(
            String tableName, String[] fieldNames, String[] uniqueKeyFields) {
        String updateClause =
                Arrays.stream(fieldNames)
                        .map(f -> quoteIdentifier(f) + "=VALUES(" + quoteIdentifier(f) + ")")
                        .collect(Collectors.joining(", "));
        return Optional.of(
                getInsertIntoStatement(tableName, fieldNames)
                        + " ON DUPLICATE KEY UPDATE "
                        + updateClause);
    }

    @Override
    public JdbcRowConverter getRowConverter(RowType rowType) {
        return new SundbRowConverter(rowType);
    }

    @Override
    public String getLimitClause(long limit) {
        return "LIMIT " + limit;
    }

    @Override
    public Set<LogicalTypeRoot> supportedTypes() {
        return EnumSet.of(
                LogicalTypeRoot.CHAR,
                LogicalTypeRoot.VARCHAR,
                LogicalTypeRoot.BOOLEAN,
                LogicalTypeRoot.BINARY,
                LogicalTypeRoot.VARBINARY,
                LogicalTypeRoot.DECIMAL,
                LogicalTypeRoot.TINYINT,
                LogicalTypeRoot.SMALLINT,
                LogicalTypeRoot.INTEGER,
                LogicalTypeRoot.BIGINT,
                LogicalTypeRoot.FLOAT,
                LogicalTypeRoot.DOUBLE,
                LogicalTypeRoot.DATE,
                LogicalTypeRoot.TIME_WITHOUT_TIME_ZONE,
                LogicalTypeRoot.TIMESTAMP_WITHOUT_TIME_ZONE,
                LogicalTypeRoot.TIMESTAMP_WITH_TIME_ZONE,
                LogicalTypeRoot.TIMESTAMP_WITH_LOCAL_TIME_ZONE);
    }
}
