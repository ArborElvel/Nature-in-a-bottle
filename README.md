# Nature in a Bottle（瓶中自然）

[Time in a Bottle](https://github.com/RealMangorage/time-in-a-bottle) 的附属模组，
NeoForge 21.1.238 / Minecraft 1.21.1。

## 它做什么

**机制与时间之瓶相同，功能不同**：时间之瓶右击方块是叠加速倍率、让方块一直跑随机刻；
瓶中自然是叠倍率、然后按倍率直接收成作物。

- 手持**瓶中自然**（`niab:nature_in_a_bottle`）右击作物 → 给这块作物叠一层倍率：
  **加速倍率**跟时间之瓶同口径（点一下 **x1**，之后每点一次翻一倍，上限 `2^(max_rate_multi-1)`，
  默认 128，8 下到顶）；**收成倍数 = 加速倍率 × 2**（第一下 2×、顶格 256×）——
  时间之瓶的"1 倍"本身就是加速，而收成这边的"1 倍"按字面就是普通收成，不翻倍不算"加上倍数"；
- 每点一次都跟时间之瓶一样在作物上挂/顶起一个 `TimeAcceleratorEntity`：实体的倍率就是上面那个
  加速倍率，寿命就是收成倒计时（默认 3 秒）。作物在这段时间里是真在长，实体上的倍率/倒计时
  就是收成倒计时，到点实体自己消失、正好是收成的那一刻；
- 成熟作物本来留不住加速实体（时间之瓶的实体会检查方块能不能随机刻，不能就自杀），
  所以这里用一个小 mixin 在收成窗口里把它留住——窗口之外一律走时间之瓶原逻辑；
- 每次右击都按时间之瓶的公式扣瓶内时间：`加速倍率 / 2 × (TICKS_CONST × EACH_USE_DURATION)`，
  创造模式不扣。按上面那串倍率算下来是 600 / 600 / 1200 / 2400 / 4800 / 9600 / 19200 / 38400 tick；
- 停手 **3 秒**（`harvest_delay_ticks = 60`）之后，按当时的收成倍数**直接掉落**作物产物，
  并把作物退回最小年龄让它重新生长；
- 瓶内时间在背包里每秒自行积累 20 tick。**同一时间只有一瓶在攒**：带着时间之瓶时由它攒
  （时间之瓶自己的规则），没带时才轮到瓶中自然，不会两瓶同时涨。

倍率记在**作物（方块坐标）**上：同一块作物连点就翻倍，点另一块作物是另起一份。
没熟透的作物也能点（照样加速、照样叠倍率），只是到点收成时还没熟就什么都不掉——
免得拿幼苗刷种子。成熟度判定在收成那一刻做。

## 配方

| 输入 | 输出 |
| --- | --- |
| 时间之瓶 + 骨粉 | 瓶中自然 |
| 瓶中自然 + 红石粉 | 时间之瓶 |

两条都是工作台无序合成，**瓶内时间（`stored_time`）原样继承**：转化前后是同一瓶东西，
只是换了功能（已存时间、累计时间都跟着走）。

配方类型是自定义的 `niab:bottle_conversion`，但它挂在原版 crafting 类型下，
JEI/EMI 会当成普通合成配方显示。之所以不用原版 `crafting_shapeless`：原版 shapeless 的产物
是固定堆叠，带不上数据组件；而 `ItemCraftedEvent` 在 shift 连点时拿到的是产物拷贝，改了没用。

## 什么算“作物”

- 所有 `extends CropBlock` 的方块：小麦、胡萝卜、马铃薯、甜菜、火把花、瓶子草，
  以及模组里继承 `CropBlock` 的作物；
- 数据包标签 `niab:harvestable_crops` 里额外声明的方块，
  默认是 `minecraft:nether_wart`、`minecraft:cocoa`、`minecraft:sweet_berry_bush`。

西瓜/南瓜茎虽然带 `age`，但不是这里说的作物，不在处理范围内。瓶子草是双高作物，
点上半也按下半算。

## 配置

`config/niab-common.toml`：

| 配置 | 默认 | 说明 |
| --- | --- | --- |
| `harvest_delay_ticks` | 60 | 停手多久收成；每点一次重新计时 |
| `reset_crop` | true | 收成后把作物退回最小年龄；关掉就是原地反复收 |
| `consume_stored_time` | true | 叠倍率消耗瓶内时间；创造模式玩家永远不消耗 |
| `accumulate_stored_time` | true | 在背包里每秒积攒 20 tick 瓶内时间 |
| `play_sound` | true | 倍率每上一层播放时间之瓶的音符音效 |

## 开发

| 场景 | 命令 |
| --- | --- |
| 编译验证 | `.\gradlew.bat compileJava` |
| 完整构建 | `.\gradlew.bat build` |
| 双客户端 + 专用服务器 | `runClient`（Alice）、`runClient2`（Bob）、`runServer` |
| 提交前检查 | `git diff --check`、`git status --short` |

依赖 Time in a Bottle 6.5.4（CurseForge 文件 7555622），通过 cursmaven 解析；
版本和文件号在 `gradle.properties` 里。物品贴图是自带的 16×160 动画
（`textures/item/nature_in_a_bottle.png` + 同名 `.mcmeta`）。
