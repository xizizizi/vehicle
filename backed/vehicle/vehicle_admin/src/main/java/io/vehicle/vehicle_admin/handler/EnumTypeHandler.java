package io.vehicle.vehicle_admin.handler;

import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedTypes;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@MappedTypes({
        io.vehicle.vehicle_admin.entity.Uav.UavStatus.class,
        io.vehicle.vehicle_admin.entity.Order.OrderStatus.class,
        io.vehicle.vehicle_admin.entity.CruiseMission.MissionType.class,
        io.vehicle.vehicle_admin.entity.CruiseMission.MissionStatus.class
})
public class EnumTypeHandler<E extends Enum<E>> extends BaseTypeHandler<E> {

    private Class<E> type;

    public EnumTypeHandler() {
    }

    public EnumTypeHandler(Class<E> type) {
        this.type = type;
    }

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, E parameter, JdbcType jdbcType) throws SQLException {
        if (parameter != null) {
            ps.setString(i, parameter.name());
        } else {
            ps.setNull(i, java.sql.Types.VARCHAR);
        }
    }

    @Override
    public E getNullableResult(ResultSet rs, String columnName) throws SQLException {
        String value = rs.getString(columnName);
        return convertToEnum(value);
    }

    @Override
    public E getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        String value = rs.getString(columnIndex);
        return convertToEnum(value);
    }

    @Override
    public E getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        String value = cs.getString(columnIndex);
        return convertToEnum(value);
    }

    private E convertToEnum(String value) {
        if (value == null || value.trim().isEmpty() || type == null) {
            return null;
        }

        try {
            return Enum.valueOf(type, value.toUpperCase());
        } catch (IllegalArgumentException e) {
            // 尝试匹配枚举名称（忽略大小写）
            for (E enumConstant : type.getEnumConstants()) {
                if (enumConstant.name().equalsIgnoreCase(value)) {
                    return enumConstant;
                }
            }
            System.err.println("WARN: Cannot convert '" + value + "' to enum type: " + type.getName());
            return null;
        }
    }
}