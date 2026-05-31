package io.vehicle.vehicle_admin.handler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.vehicle.vehicle_admin.entity.Meta;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Objects;

public class MetaTypeHandler extends BaseTypeHandler<Meta> {
    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, Meta parameter, JdbcType jdbcType) throws SQLException {
        ObjectMapper om = new ObjectMapper();
        String json = null;
        try {
            json = om.writeValueAsString(parameter);
            if (Objects.isNull(json)) {
                ps.setString(i, null);
            }
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    public Meta getNullableResult(ResultSet rs, String columnName) throws SQLException {
        ObjectMapper om = new ObjectMapper();
        String json = rs.getString(columnName);
        Meta meta = null;
        try {
            if(Objects.isNull(json)){
                meta=om.readValue(json,Meta.class);
            }
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        return meta;
    }

    @Override
    public Meta getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        ObjectMapper om = new ObjectMapper();
        String json = rs.getString(columnIndex);
        Meta meta = null;
        try {
            if(Objects.isNull(json)){
                meta=om.readValue(json,Meta.class);
            }
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        return meta;
    }

    @Override
    public Meta getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        ObjectMapper om = new ObjectMapper();
        String json = cs.getString(columnIndex);
        Meta meta = null;
        try {
            if(Objects.isNull(json)){
                meta=om.readValue(json,Meta.class);
            }
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        return meta;
    }
}

