package org.cookpro.handler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedJdbcTypes;
import org.apache.ibatis.type.MappedTypes;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;

@MappedJdbcTypes(JdbcType.VARCHAR)
@MappedTypes(LinkedList.class)
public class LinkedListStringTypeHandler extends BaseTypeHandler<LinkedList<String>> {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, LinkedList<String> parameter, JdbcType jdbcType) throws SQLException {
        try {
            String json = objectMapper.writeValueAsString(parameter);
            ps.setString(i, json);
        } catch (JsonProcessingException e) {
            throw new SQLException("Error serializing LinkedList<String> to JSON", e);
        }
    }

    @Override
    public LinkedList<String> getNullableResult(ResultSet rs, String columnName) throws SQLException {
        return deserializeJson(rs.getString(columnName));
    }

    @Override
    public LinkedList<String> getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        return deserializeJson(rs.getString(columnIndex));
    }

    @Override
    public LinkedList<String> getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        return deserializeJson(cs.getString(columnIndex));
    }

    private LinkedList<String> deserializeJson(String json) throws SQLException {
        if (json == null || json.trim().isEmpty()) {
            return new LinkedList<>();
        }
        try {
            return objectMapper.readValue(json, objectMapper.getTypeFactory().constructCollectionType(LinkedList.class, String.class));
        } catch (Exception e) {
            throw new SQLException("Error deserializing JSON to LinkedList<String>", e);
        }
    }
}