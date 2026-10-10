# Nature in a Bottle（瓶中自然）

[Time in a Bottle](https://github.com/RealMangorage/time-in-a-bottle) 的附属模组，
NeoForge 21.1.238 / Minecraft 1.21.1。

## 它做什么

时间之瓶右击方块时会给方块挂一个时间加速实体，让它反复跑随机刻。已经长熟的作物不再随机刻，
所以对着熟作物右击只会失败。本模组补上这一段：

- 手持时间之瓶右击**已成熟**的作物 → 立刻按战利品表收一份产物、乘上倍率直接掉出来，
  并把作物退回最小年龄让它重新生长；
- 未成熟的作物完全不受影响，照旧走时间之瓶的加速流程；
- 消耗的瓶内时间沿用时间之瓶自己的算法：`倍率 / 2 × (TICKS_CONST × EACH_USE_DURATION)`，
  倍率越高越贵（默认 8 倍 = 2400 tick）。

## 什么算“作物”

- 所有 `extends CropBlock` 的方块：小麦、胡萝卜、马铃薯、甜菜、火把花、瓶子草，
  以及模组里继承 `CropBlock` 的作物；
- 数据包标签 `nature_in_a_bottle:harvestable_crops` 里额外声明的方块，
  默认是 `minecraft:nether_wart`、`minecraft:cocoa`、`minecraft:sweet_berry_bush`。

西瓜/南瓜茎虽然带 `age`，但不是这里说的作物，不在处理范围内。瓶子草是双高作物，
点上半也按下半算。

## 配置

`config/nature_in_a_bottle-common.toml`：

| 配置 | 默认 | 说明 |
| --- | --- | --- |
| `harvest_multiplier` | 8 | 产物倍率（1–128，上限跟随时间之瓶的 `max_rate_multi`） |
| `reset_crop` | true | 收获后把作物退回最小年龄；关掉就是原地反复收 |
| `drop_as_items` | true | 产物掉在地上；关掉则直接进背包 |
| `consume_stored_time` | true | 消耗瓶内时间；创造模式玩家永远不消耗 |
| `play_sound` | true | 收获成功时播放时间之瓶的音符音效 |

## 开发

| 场景 | 命令 |
| --- | --- |
| 编译验证 | `.\gradlew.bat compileJava` |
| 完整构建 | `.\gradlew.bat build` |
| 双客户端 + 专用服务器 | `runClient`（Alice）、`runClient2`（Bob）、`runServer` |
| 提交前检查 | `git diff --check`、`git status --short` |

依赖 Time in a Bottle 6.5.4（CurseForge 文件 7555622），通过 cursmaven 解析；
版本和文件号在 `gradle.properties` 里。
