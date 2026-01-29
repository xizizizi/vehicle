package io.vehicle.vehicle_admin.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.vehicle.vehicle_admin.handler.EnumTypeHandler;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@TableName(value = "areas", autoResultMap = true)
public class Area {

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id; // 改为小写 id

    private String name;

    @TableField(value = "type", typeHandler = EnumTypeHandler.class)
    private AreaType type; // 使用 ENUM 类型

    @TableField(value = "unit", typeHandler = EnumTypeHandler.class)
    private Unit unit; // 使用 ENUM 类型

    @TableField(value = "base_value")
    private BigDecimal baseValue;

    @TableField(value = "created_at")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
    public static final String[] CARD_AREAS = {
            "商业区", "住宅区", "工业区", "自然区域", "特殊区域"
    };


    // Getter/Setter
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public AreaType getType() { return type; }
    public void setType(AreaType type) { this.type = type; }

    public Unit getUnit() { return unit; }
    public void setUnit(Unit unit) { this.unit = unit; }

    public BigDecimal getBaseValue() { return baseValue; }
    public void setBaseValue(BigDecimal baseValue) { this.baseValue = baseValue; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    // AreaType 枚举类
    public enum AreaType {
        BRIDGE("桥梁"),
        TUNNEL("涵洞隧道"),
        ROAD("交通干道"),
        COMMERCIAL("商业区"),
        RESIDENTIAL("住宅区"),
        INDUSTRIAL("工业区"),
        NATURAL("自然区域"),
        SPECIAL("特殊区域");

        private final String chineseName;

        AreaType(String chineseName) {
            this.chineseName = chineseName;
        }

        public String getChineseName() {
            return chineseName;
        }
    }

    // Unit 枚举类
    public enum Unit {
        SQUARE_KM("平方公里"),
        SEAT("座"),
        PIECE("个"),
        ROAD("条"),
        HECTARE("公顷");

        private final String chineseName;

        Unit(String chineseName) {
            this.chineseName = chineseName;
        }

        public String getChineseName() {
            return chineseName;
        }
    }
}
