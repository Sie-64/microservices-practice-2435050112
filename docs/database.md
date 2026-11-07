# 数据库设计说明

## 1. 车辆表 (vehicles)
| 字段名 | 类型 | 约束 | 说明 |
| :--- | :--- | :--- | :--- |
| id | BIGINT | 主键, 自增 | 车辆唯一标识 |
| plate_number | VARCHAR(255) | 非空, 唯一 | 车牌号 |
| brand | VARCHAR(255) | 无 | 品牌 |
| tier | VARCHAR(255) | 非空 | 套餐等级(light/common/full) |

## 2. 告警表 (alerts)
| 字段名 | 类型 | 约束 | 说明 |
| :--- | :--- | :--- | :--- |
| id | BIGINT | 主键, 自增 | 告警唯一标识 |
| vehicle_id | BIGINT | 非空 | 关联的车辆ID |
| alert_type | VARCHAR(255) | 非空 | 告警类型(如漏电、老化) |
| level | INT | 非空 | 告警等级(1/2/3) |
| status | VARCHAR(255) | 无 | 状态(pending/handled) |

## 关系说明
- `alerts.vehicle_id` 逻辑关联 `vehicles.id`（一对多关系）。

## 3. 驾驶事件表 (driving_events)
| 字段名 | 类型 | 约束 | 说明 |
| :--- | :--- | :--- | :--- |
| id | BIGINT | 主键, 自增 | 驾驶事件唯一标识 |
| vehicle_id | BIGINT | 非空 | 逻辑关联车辆ID |
| occurred_at | DATETIME | 非空 | 行程发生时间 |
| distance_km | DECIMAL(12,3) | 非空 | 行程里程 |
| fuel_liters | DECIMAL(12,3) | 非空 | 燃油消耗 |
| energy_kwh | DECIMAL(12,3) | 非空 | 电能消耗 |
| harsh_acceleration_count | INT | 非空 | 急加速次数 |
| harsh_braking_count | INT | 非空 | 急刹车次数 |
| max_speed_kph | DECIMAL(8,2) | 非空 | 最高速度 |
| created_at | DATETIME | 非空 | 记录创建时间 |

## 4. 碳排报告表 (carbon_reports)
| 字段名 | 类型 | 约束 | 说明 |
| :--- | :--- | :--- | :--- |
| id | BIGINT | 主键, 自增 | 碳排报告唯一标识 |
| vehicle_id | BIGINT | 非空 | 逻辑关联车辆ID |
| report_month | VARCHAR(7) | 非空 | 报告月份（YYYY-MM） |
| total_distance_km | DECIMAL(12,3) | 非空 | 月度总里程 |
| fuel_liters | DECIMAL(12,3) | 非空 | 月度燃油消耗 |
| energy_kwh | DECIMAL(12,3) | 非空 | 月度电能消耗 |
| co2_kg | DECIMAL(12,3) | 非空 | 月度CO₂排放 |
| calculated_at | DATETIME | 非空 | 报告生成时间 |

`vehicle_id + report_month` 唯一，重复生成同一月份报告时更新原有报告。

## 跨库关系说明
- `driving_events.vehicle_id` 逻辑关联 `vehicles.id`。
- `carbon_reports.vehicle_id` 逻辑关联 `vehicles.id`。
- 碳排报告的数据来源通过 HTTP 调用驾驶行为服务获取，不直接查询
  `driving_db`，也不在数据库中建立跨库外键。
