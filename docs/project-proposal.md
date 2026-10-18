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

## 10.18-实现：业务建模与持久化
- **核心业务流程**：车主录入车辆 -> 车辆触发告警 -> 系统记录告警状态。
- **业务实体与关系**：
  - `Vehicle`（车辆实体）
  - `Alert`（告警实体）
  - 关系：`Alert` 通过 `vehicleId` 关联 `Vehicle`，为一对多关系。
- **业务规则**：在 `AlertService` 中校验，告警等级（level）必须是 1、2 或 3，否则抛出 `IllegalArgumentException` 拒绝保存。