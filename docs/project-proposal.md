# 项目提案

## 项目名称
智驾伴侣——智能车主服务平台

## 目标用户
面向**车主**（唯一业务用户），按付费套餐分为轻量版、标准版、豪华版三个层级。

## 优先实现的业务场景
**车辆告警闭环**：车辆传感器上报数据 → 消息队列 → 告警规则服务分级 → 通知服务推送 → 豪华版额外触发 AI 分析。

## 两个核心模型

### 模型 1：车辆（Vehicle）
| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long | 车辆唯一标识 |
| plateNumber | String | 车牌号 |
| brand | String | 品牌 |
| model | String | 型号 |
| ownerId | Long | 车主 ID |
| tier | String | 套餐等级（light/common/full） |

### 模型 2：告警（Alert）
| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long | 告警唯一标识 |
| vehicleId | Long | 关联车辆 ID |
| alertType | String | 告警类型 |
| level | Integer | 告警等级（1/2/3） |
| occurredAt | DateTime | 发生时间 |
| status | String | 状态（pending/handled） |
