# MainMviActivity 详细流程说明

## 📋 目录
1. [整体架构](#整体架构)
2. [核心组件](#核心组件)
3. [完整数据流转](#完整数据流转)
4. [具体场景示例](#具体场景示例)
5. [代码执行流程](#代码执行流程)

---

## 🏗️ 整体架构

### MVI 架构模式
```
View (UI) → Intent (用户操作) → ViewModel (业务逻辑) → State (状态) → View (UI更新)
```

### 单向数据流
- **单向性**：数据只能单向流动，确保状态可预测
- **不可变性**：State 是不可变的，每次更新都创建新对象
- **集中管理**：所有状态集中在一个 State 对象中

---

## 🧩 核心组件

### 1. MainViewState（状态）
```kotlin
data class MainViewState(
    val selectedTab: Int = 0,                    // 当前选中的 Tab
    val linkScreenData: Pair<String, String>? = null  // LinkScreen 数据
) : ViewState
```

**特点**：
- 使用 `data class` 确保不可变性
- 所有 UI 相关的状态都集中在这里
- 通过 `copy()` 方法创建新状态

### 2. MainIntent（用户意图）
```kotlin
sealed class MainIntent : ViewIntent {
    data class SelectTab(val tabIndex: Int) : MainIntent()      // 切换 Tab
    data class OpenArticle(val article: Article) : MainIntent()  // 打开文章
    object CloseLinkScreen : MainIntent()                        // 关闭 LinkScreen
}
```

**特点**：
- 使用 `sealed class` 确保类型安全
- 每个用户操作对应一个 Intent
- Intent 包含执行操作所需的所有数据

### 3. MainMviViewModel（状态管理）
```kotlin
class MainMviViewModel : MviViewModel<MainIntent, MainViewState>() {
    override fun initialState(): MainViewState {
        return MainViewState.initial()  // 初始状态
    }
    
    override fun reduce(currentState: MainViewState, intent: MainIntent): MainViewState {
        // Reducer：根据当前状态和 Intent 生成新状态
        return when (intent) {
            is MainIntent.SelectTab -> {
                currentState.copy(selectedTab = intent.tabIndex)
            }
            is MainIntent.OpenArticle -> {
                currentState.copy(linkScreenData = Pair(intent.article.title, intent.article.link))
            }
            is MainIntent.CloseLinkScreen -> {
                currentState.copy(linkScreenData = null)
            }
        }
    }
}
```

**核心方法**：
- `initialState()`: 返回初始状态
- `reduce()`: Reducer 函数，根据当前状态和 Intent 生成新状态
- `dispatchIntent()`: 发送 Intent（由基类提供）

### 4. MainMviActivity（UI 层）
```kotlin
class MainMviActivity : BaseComposeMviActivity<MainMviViewModel, MainIntent, MainViewState>() {
    @Composable
    override fun Render(state: MainViewState, dispatch: (MainIntent) -> Unit) {
        MainContent(
            selectedTab = state.selectedTab,
            onTabSelected = { tabIndex ->
                dispatch(MainIntent.SelectTab(tabIndex))  // 发送 Intent
            },
            linkScreenData = state.linkScreenData,
            onCloseLinkScreen = {
                dispatch(MainIntent.CloseLinkScreen)  // 发送 Intent
            },
            onArticleClick = { article ->
                dispatch(MainIntent.OpenArticle(article))  // 发送 Intent
            }
        )
    }
}
```

---

## 🔄 完整数据流转

### 流程图
```
┌─────────────────────────────────────────────────────────────────┐
│                      MainMviActivity                            │
│                                                                  │
│  ┌──────────────┐         ┌──────────────┐                      │
│  │   UI 组件     │         │   Render()   │                      │
│  │ (MainContent)│────────▶│              │                      │
│  └──────┬───────┘         └──────┬───────┘                      │
│         │                        │                               │
│         │ 用户操作               │ 接收 State                    │
│         │ (点击 Tab/文章)         │                               │
│         ▼                        │                               │
│  ┌──────────────┐               │                               │
│  │ dispatch()    │───────────────┘                               │
│  └──────┬───────┘                                                │
│         │                                                         │
│         │ dispatchIntent(MainIntent.SelectTab(1))                 │
└─────────┼─────────────────────────────────────────────────────────┘
          │
          ▼
┌─────────────────────────────────────────────────────────────────┐
│                  MviViewModel (基类)                            │
│                                                                  │
│  ┌──────────────────────────────────────────────────────────┐ │
│  │  dispatchIntent(intent: I)                                │ │
│  │    ↓                                                       │ │
│  │  _intent.emit(intent)  // 发送到 Intent 流                │ │
│  └──────────────────────────────────────────────────────────┘ │
│         │                                                       │
│         │ Intent 流处理                                         │
│         ▼                                                       │
│  ┌──────────────────────────────────────────────────────────┐ │
│  │  processIntent(intent: I)                                 │ │
│  │    ↓                                                       │ │
│  │  val currentState = _state.value                         │ │
│  │  val newState = reduce(currentState, intent)             │ │
│  │  _state.value = newState  // 更新状态                     │ │
│  └──────────────────────────────────────────────────────────┘ │
└─────────┼───────────────────────────────────────────────────────┘
          │
          ▼
┌─────────────────────────────────────────────────────────────────┐
│              MainMviViewModel (子类)                            │
│                                                                  │
│  ┌──────────────────────────────────────────────────────────┐ │
│  │  reduce(currentState: MainViewState,                      │ │
│  │         intent: MainIntent): MainViewState                │ │
│  │                                                            │ │
│  │  when (intent) {                                          │ │
│  │    is MainIntent.SelectTab ->                             │ │
│  │      currentState.copy(selectedTab = intent.tabIndex)    │ │
│  │    is MainIntent.OpenArticle ->                           │ │
│  │      currentState.copy(linkScreenData = ...)             │ │
│  │    is MainIntent.CloseLinkScreen ->                       │ │
│  │      currentState.copy(linkScreenData = null)            │ │
│  │  }                                                         │ │
│  └──────────────────────────────────────────────────────────┘ │
│         │                                                       │
│         │ 返回新状态                                             │
│         ▼                                                       │
│  ┌──────────────────────────────────────────────────────────┐ │
│  │  _state.value = newState                                  │ │
│  │  // StateFlow 自动通知所有观察者                           │ │
│  └──────────────────────────────────────────────────────────┘ │
└─────────┼───────────────────────────────────────────────────────┘
          │
          │ StateFlow 更新
          ▼
┌─────────────────────────────────────────────────────────────────┐
│                  BaseComposeMviActivity                         │
│                                                                  │
│  ┌──────────────────────────────────────────────────────────┐ │
│  │  val state by viewModel.state.collectAsState()           │ │
│  │  // 自动收集 StateFlow 的变化                              │ │
│  └──────────────────────────────────────────────────────────┘ │
│         │                                                       │
│         │ 状态变化触发重组                                       │
│         ▼                                                       │
│  ┌──────────────────────────────────────────────────────────┐ │
│  │  Render(state = state) { intent ->                        │ │
│  │    viewModel.dispatchIntent(intent)                      │ │
│  │  }                                                         │ │
│  └──────────────────────────────────────────────────────────┘ │
└─────────┼───────────────────────────────────────────────────────┘
          │
          │ 重新渲染 UI
          ▼
┌─────────────────────────────────────────────────────────────────┐
│                      MainMviActivity                            │
│                                                                  │
│  ┌──────────────────────────────────────────────────────────┐ │
│  │  Render(state: MainViewState,                            │ │
│  │         dispatch: (MainIntent) -> Unit)                  │ │
│  │                                                            │ │
│  │  MainContent(                                            │ │
│  │    selectedTab = state.selectedTab,  // 使用新状态        │ │
│  │    linkScreenData = state.linkScreenData,                 │ │
│  │    onTabSelected = { dispatch(MainIntent.SelectTab(it)) } │ │
│  │  )                                                         │ │
│  └──────────────────────────────────────────────────────────┘ │
└─────────────────────────────────────────────────────────────────┘
```

---

## 📝 具体场景示例

### 场景 1：用户点击 Tab（切换到"知识体系"）

#### 步骤 1：用户操作
```kotlin
// MainContent.kt
BottomNavigation(
    selectedTab = currentSelectedTab,
    onTabSelected = { tabIndex ->
        handleTabSelected(tabIndex)  // tabIndex = 1
    }
)
```

#### 步骤 2：发送 Intent
```kotlin
// MainMviActivity.kt - Render() 方法
MainContent(
    onTabSelected = { tabIndex ->
        dispatch(MainIntent.SelectTab(tabIndex))  // dispatch 是传入的 lambda
    }
)

// 实际调用
viewModel.dispatchIntent(MainIntent.SelectTab(1))
```

#### 步骤 3：ViewModel 接收 Intent
```kotlin
// MviViewModel.kt
fun dispatchIntent(intent: I) {
    viewModelScope.launch {
        _intent.emit(intent)  // 发送到 Intent 流
    }
}
```

#### 步骤 4：处理 Intent
```kotlin
// MviViewModel.kt - init 块中
viewModelScope.launch {
    _intent
        .onEach { intent ->
            processIntent(intent)  // 处理每个 Intent
        }
        .collect()
}

// processIntent 方法
protected open fun processIntent(intent: I) {
    val currentState = _state.value  // 获取当前状态
    val newState = reduce(currentState, intent)  // 生成新状态
    _state.value = newState  // 更新状态
}
```

#### 步骤 5：Reducer 生成新状态
```kotlin
// MainMviViewModel.kt
override fun reduce(currentState: MainViewState, intent: MainIntent): MainViewState {
    return when (intent) {
        is MainIntent.SelectTab -> {
            // 当前状态：MainViewState(selectedTab = 0, linkScreenData = null)
            // 新状态：MainViewState(selectedTab = 1, linkScreenData = null)
            currentState.copy(selectedTab = intent.tabIndex)
        }
        // ...
    }
}
```

#### 步骤 6：StateFlow 通知 UI
```kotlin
// MviViewModel.kt
private val _state = MutableStateFlow<S>(initialState())
val state: StateFlow<S> = _state.asStateFlow()

// 当 _state.value 更新时，StateFlow 自动通知所有观察者
```

#### 步骤 7：UI 自动更新
```kotlin
// BaseComposeMviActivity.kt
val state by viewModel.state.collectAsState()  // 自动收集状态变化

// 当 state 变化时，Compose 自动触发重组
Render(state = state) { intent ->
    viewModel.dispatchIntent(intent)
}
```

#### 步骤 8：重新渲染 UI
```kotlin
// MainMviActivity.kt
@Composable
override fun Render(state: MainViewState, dispatch: (MainIntent) -> Unit) {
    MainContent(
        selectedTab = state.selectedTab,  // 现在是 1，显示"知识体系"
        // ...
    )
}
```

**状态变化**：
```
初始状态：MainViewState(selectedTab = 0, linkScreenData = null)
    ↓
用户点击 Tab 1
    ↓
发送 Intent：MainIntent.SelectTab(1)
    ↓
Reducer 处理：currentState.copy(selectedTab = 1)
    ↓
新状态：MainViewState(selectedTab = 1, linkScreenData = null)
    ↓
UI 更新：显示"知识体系"页面
```

---

### 场景 2：用户点击文章（打开 LinkScreen）

#### 步骤 1：用户操作
```kotlin
// HomeScreen.kt
ArticleList(
    onItemClick = { article ->
        onArticleClick(article)  // 用户点击文章
    }
)
```

#### 步骤 2：发送 Intent
```kotlin
// MainMviActivity.kt
MainContent(
    onArticleClick = { article ->
        dispatch(MainIntent.OpenArticle(article))
    }
)
```

#### 步骤 3-4：ViewModel 处理（同场景 1）

#### 步骤 5：Reducer 生成新状态
```kotlin
// MainMviViewModel.kt
override fun reduce(currentState: MainViewState, intent: MainIntent): MainViewState {
    return when (intent) {
        is MainIntent.OpenArticle -> {
            // 当前状态：MainViewState(selectedTab = 0, linkScreenData = null)
            // 新状态：MainViewState(selectedTab = 0, linkScreenData = Pair("文章标题", "https://..."))
            currentState.copy(
                linkScreenData = Pair(intent.article.title, intent.article.link)
            )
        }
        // ...
    }
}
```

#### 步骤 6-8：UI 更新（同场景 1）

**状态变化**：
```
当前状态：MainViewState(selectedTab = 0, linkScreenData = null)
    ↓
用户点击文章
    ↓
发送 Intent：MainIntent.OpenArticle(article)
    ↓
Reducer 处理：currentState.copy(linkScreenData = Pair(...))
    ↓
新状态：MainViewState(selectedTab = 0, linkScreenData = Pair("标题", "链接"))
    ↓
UI 更新：显示 LinkScreen，隐藏主界面
```

---

### 场景 3：用户关闭 LinkScreen

#### 步骤 1：用户操作
```kotlin
// LinkScreen.kt
LinkScreen(
    onBackClick = { 
        // 用户点击返回按钮
    }
)
```

#### 步骤 2：发送 Intent
```kotlin
// MainMviActivity.kt
MainContent(
    linkScreenData = state.linkScreenData,
    onCloseLinkScreen = {
        dispatch(MainIntent.CloseLinkScreen)
    }
)
```

#### 步骤 5：Reducer 生成新状态
```kotlin
// MainMviViewModel.kt
override fun reduce(currentState: MainViewState, intent: MainIntent): MainViewState {
    return when (intent) {
        is MainIntent.CloseLinkScreen -> {
            // 当前状态：MainViewState(selectedTab = 0, linkScreenData = Pair(...))
            // 新状态：MainViewState(selectedTab = 0, linkScreenData = null)
            currentState.copy(linkScreenData = null)
        }
        // ...
    }
}
```

**状态变化**：
```
当前状态：MainViewState(selectedTab = 0, linkScreenData = Pair("标题", "链接"))
    ↓
用户点击返回
    ↓
发送 Intent：MainIntent.CloseLinkScreen
    ↓
Reducer 处理：currentState.copy(linkScreenData = null)
    ↓
新状态：MainViewState(selectedTab = 0, linkScreenData = null)
    ↓
UI 更新：隐藏 LinkScreen，显示主界面
```

---

## 🔍 代码执行流程

### 初始化流程

```kotlin
// 1. Activity 创建
MainMviActivity.onCreate()
    ↓
// 2. 创建 ViewModel
viewModel = ViewModelProvider(this)[getVmClazz(this)]
    ↓
// 3. ViewModel 初始化
MainMviViewModel.init
    ↓
// 4. 初始化状态
_state = MutableStateFlow(initialState())
    ↓
// 5. 启动 Intent 流处理
viewModelScope.launch {
    _intent.onEach { processIntent(it) }.collect()
}
    ↓
// 6. 设置 Compose 内容
setContent {
    val state by viewModel.state.collectAsState()  // 收集状态
    Render(state = state) { intent ->
        viewModel.dispatchIntent(intent)  // 发送 Intent
    }
}
```

### Intent 处理流程

```kotlin
// 1. UI 层发送 Intent
dispatch(MainIntent.SelectTab(1))
    ↓
// 2. BaseComposeMviActivity 调用
viewModel.dispatchIntent(MainIntent.SelectTab(1))
    ↓
// 3. MviViewModel 发送到流
_intent.emit(MainIntent.SelectTab(1))
    ↓
// 4. Intent 流处理（在 init 块中启动的协程）
_intent.onEach { intent ->
    processIntent(intent)  // MainIntent.SelectTab(1)
}
    ↓
// 5. processIntent 方法
val currentState = _state.value  // 获取当前状态
val newState = reduce(currentState, intent)  // 调用子类的 reduce
_state.value = newState  // 更新状态
    ↓
// 6. MainMviViewModel.reduce()
when (intent) {
    is MainIntent.SelectTab -> {
        currentState.copy(selectedTab = intent.tabIndex)
    }
}
    ↓
// 7. StateFlow 通知观察者
_state.value = newState  // StateFlow 自动通知
    ↓
// 8. Compose 重组
val state by viewModel.state.collectAsState()  // 检测到变化
Render(state = state) { ... }  // 重新渲染
```

---

## 🎯 关键点总结

### 1. **单向数据流**
- View → Intent → ViewModel → State → View
- 数据只能单向流动，确保可预测性

### 2. **不可变状态**
- State 使用 `data class`，通过 `copy()` 创建新对象
- 每次更新都创建新状态，不修改原状态

### 3. **Reducer 模式**
- `reduce(currentState, intent)` 是纯函数
- 根据当前状态和 Intent 生成新状态
- 不产生副作用

### 4. **响应式更新**
- 使用 `StateFlow` 管理状态
- Compose 通过 `collectAsState()` 自动收集变化
- 状态变化自动触发 UI 重组

### 5. **类型安全**
- 使用 `sealed class` 定义 Intent
- 编译时检查，避免遗漏处理

---

## 📚 相关文件

- `MainMviActivity.kt` - UI 层，处理用户交互
- `MainMviViewModel.kt` - 状态管理，实现 Reducer
- `MainIntent.kt` - 用户意图定义
- `MainViewState.kt` - 状态定义
- `BaseComposeMviActivity.kt` - MVI Activity 基类
- `MviViewModel.kt` - MVI ViewModel 基类

