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
- `alerts.vehicle_id` 逻辑关联 `vehicles.id`（一对多关系：一辆车可以有多条告警记录）。