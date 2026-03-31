---
name: android-naming-conventions
description: Android 开发命名规范，涵盖类、方法、变量、资源文件的命名规则。当用户需要创建新类、布局文件、资源时使用。
---

# Android 命名规范

## 类命名

| 类型 | 规则 | 示例 |
|------|------|------|
| Activity | 以 Activity 结尾 | LoginActivity, MainActivity |
| Fragment | 以 Fragment 结尾 | UserFragment, SettingsFragment |
| Adapter | 以 Adapter 结尾 | UserAdapter, ProductAdapter |
| ViewModel | 以 ViewModel 结尾 | LoginViewModel, HomeViewModel |
| Repository | 以 Repository 结尾 | UserRepository, ProductRepository |
| Service | 以 Service 结尾 | UploadService, DownloadService |
| BroadcastReceiver | 以 Receiver 结尾 | NetworkReceiver, BootReceiver |
| 工具类 | 以 Utils 或 Helper 结尾 | FileUtils, DateHelper |
| 接口 | I 开头或 able/er 结尾 | ILoginListener, Clickable |
| 抽象类 | Base 开头 | BaseActivity, BaseViewModel |

## 变量命名

| 类型 | 规则 | 示例 |
|------|------|------|
| 成员变量 | camelCase | userName, isLoggedIn |
| 常量 | UPPER_SNAKE_CASE | MAX_RETRY_COUNT, DEFAULT_TIMEOUT |
| 静态变量 | s 前缀 + camelCase | sInstance, sDefaultConfig |
| 布尔值 | is/has/can 开头 | isLoading, hasError, canExecute |
| 集合 | 复数形式或 list/map 后缀 | users, userList, userMap |

## 方法命名

| 类型 | 规则 | 示例 |
|------|------|------|
| 获取方法 | get + 字段名 | getUserName(), getUserId() |
| 设置方法 | set + 字段名 | setUserName(), setUserId() |
| 布尔判断 | is/has/can + 动词 | isVisible(), hasData(), canDelete() |
| 回调方法 | on + 事件名 | onClick(), onSuccess(), onError() |
| 初始化 | init + 对象名 | initViews(), initData() |
| 加载数据 | load + 数据名 | loadUsers(), loadProducts() |

## XML 资源命名

| 资源类型 | 规则 | 示例 |
|----------|------|------|
| 布局文件 | 组件名_页面名 | activity_login.xml, fragment_user.xml |
| ID | 控件类型_含义 | btn_login, tv_title, et_username |
| 字符串 | 模块_功能_描述 | login_title, error_network_timeout |
| 颜色 | 颜色名_用途 | primary_color, text_dark, bg_white |
| 尺寸 | 用途_大小 | text_size_large, padding_normal |
| 图片 | 模块_功能_状态 | ic_login_eye, bg_button_pressed |

## 控件 ID 缩写对照表

| 控件 | 缩写 | 示例 |
|------|------|------|
| Button | btn | btn_submit |
| TextView | tv | tv_title |
| EditText | et | et_username |
| ImageView | iv | iv_avatar |
| RecyclerView | rv | rv_list |
| LinearLayout | ll | ll_container |
| RelativeLayout | rl | rl_parent |
| ConstraintLayout | cl | cl_root |
| ScrollView | sv | sv_content |
| ProgressBar | pb | pb_loading |

## 包命名

| 包名 | 内容 |
|------|------|
| com.公司名.项目名.ui | Activity, Fragment |
| com.公司名.项目名.adapter | RecyclerView 适配器 |
| com.公司名.项目名.viewmodel | ViewModel 类 |
| com.公司名.项目名.repository | Repository 类 |
| com.公司名.项目名.network | Retrofit 接口、网络模型 |
| com.公司名.项目名.database | Room 数据库、Dao |
| com.公司名.项目名.model | 数据模型 |
| com.公司名.项目名.utils | 工具类 |
| com.公司名.项目名.widget | 自定义 View |