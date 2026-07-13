package com.kipu.kafka.transforms;

import org.apache.kafka.common.config.ConfigDef;
import org.apache.kafka.connect.connector.ConnectRecord;
import org.apache.kafka.connect.data.Struct;
import org.apache.kafka.connect.transforms.Transformation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * Routes records to a dynamic topic name based on payload fields.
 * Format: schema.table
 */
public class RouteToSchemaTransform<R extends ConnectRecord<R>> implements Transformation<R> {
    private static final Logger log = LoggerFactory.getLogger(RouteToSchemaTransform.class);

    public static final String DB_FIELD_CONFIG = "db.field";
    public static final String TABLE_FIELD_CONFIG = "table.field";

    public static final ConfigDef CONFIG_DEF = new ConfigDef()
            .define(DB_FIELD_CONFIG, ConfigDef.Type.STRING, "__db", ConfigDef.Importance.HIGH, "Field name for the schema")
            .define(TABLE_FIELD_CONFIG, ConfigDef.Type.STRING, "__table", ConfigDef.Importance.HIGH, "Field name for the table");

    private String dbField;
    private String tableField;

    @Override
    public void configure(Map<String, ?> props) {
        Object dbVal = props.get(DB_FIELD_CONFIG);
        this.dbField = (dbVal != null) ? dbVal.toString() : "__db";
        
        Object tableVal = props.get(TABLE_FIELD_CONFIG);
        this.tableField = (tableVal != null) ? tableVal.toString() : "__table";
        
        log.info("[RouteToSchema] Initialized with db.field={} and table.field={}", dbField, tableField);
    }

    @Override
    public R apply(R record) {
        if (record.value() == null) return record;

        String db = null;
        String table = null;

        if (record.value() instanceof Struct) {
            Struct struct = (Struct) record.value();
            db = getFieldValue(struct, dbField);
            table = getFieldValue(struct, tableField);
        } else if (record.value() instanceof Map) {
            Map<?, ?> map = (Map<?, ?>) record.value();
            db = getMapValue(map, dbField);
            table = getMapValue(map, tableField);
        }

        if (isValidIdentifier(db) && isValidIdentifier(table)) {
            // Concatenate schema and table cleanly. 
            // The JDBC Sink Connector with "quote.sql.identifiers": "always" will handle the quoting.
            String newTopic = db + "." + table;
            
            return record.newRecord(
                    newTopic,
                    record.kafkaPartition(),
                    record.keySchema(),
                    record.key(),
                    record.valueSchema(),
                    record.value(),
                    record.timestamp(),
                    record.headers()
            );
        }

        return record; // Fallback to original topic if fields are missing or invalid
    }

    private String getFieldValue(Struct struct, String fieldName) {
        try {
            if (struct.schema().field(fieldName) != null) {
                Object val = struct.get(fieldName);
                return (val != null) ? val.toString().trim() : null;
            }
        } catch (Exception e) {
            log.debug("[RouteToSchema] Error accessing field {}", fieldName, e);
        }
        return null;
    }

    private String getMapValue(Map<?, ?> map, String fieldName) {
        Object val = map.get(fieldName);
        return (val != null) ? val.toString().trim() : null;
    }

    private boolean isValidIdentifier(String identifier) {
        return identifier != null && !identifier.isEmpty();
    }

    @Override
    public ConfigDef config() { return CONFIG_DEF; }

    @Override
    public void close() {}
}