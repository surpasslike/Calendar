# Calendar

一个 Android 日历应用，可以管理日程和习惯打卡，支持重复日程和到时提醒。

## 应用截图

| 日历主页 | 日程编辑 | 通知提醒 |
|:---:|:---:|:---:|
| ![日历主页](https://raw.githubusercontent.com/surpasslike/Calendar/main/docs/images/07-screenshot-home.png) | ![日程编辑](https://raw.githubusercontent.com/surpasslike/Calendar/main/docs/images/08-screenshot-schedule.png) | ![通知提醒](https://raw.githubusercontent.com/surpasslike/Calendar/main/docs/images/09-screenshot-notification.png) |

<!-- 截图说明：将截图放到 docs/images/ 目录下，文件名对应上面的链接即可显示 -->

## 功能

- 月视图日历，点击日期查看当天日程
- 日程的增删改查，支持设置开始/结束时间
- 重复日程（每天/每周/每月/每年）
- 日程提醒通知（提前 5/15/30/60 分钟）
- 优先级标记（重要/次重要/日常）
- 全天事件
- 设备重启后自动恢复提醒

## 技术栈

- Kotlin
- MVVM + Repository 架构
- Room 数据库
- Kotlin Coroutines + Flow
- AlarmManager + NotificationManager（日程提醒）
- ViewBinding
- [CalendarView](https://github.com/huanghaibin-dev/CalendarView)（开源日历控件）
- [AndroidUtilCode](https://github.com/Blankj/AndroidUtilCode)（工具类库）

## 项目结构

```
com.surpasslike.calendar/
├── base/                   # 基类（BaseFragment）
├── data/
│   ├── dao/                # Room DAO
│   ├── database/           # Room Database
│   └── entity/             # 数据实体
├── reminder/               # 提醒模块
│   ├── ReminderScheduler   # 闹钟调度
│   ├── ReminderReceiver    # 闹钟触发 → 发通知
│   ├── BootReceiver        # 开机重注册闹钟
│   └── NotificationHelper  # 通知权限处理
├── repository/             # 数据仓库层
├── utils/                  # 工具类（RepeatRule 等）
├── view/
│   ├── adapter/            # RecyclerView 适配器
│   ├── dialog/             # 对话框
│   └── fragment/           # 页面（日历、日程编辑）
├── viewmodel/              # ViewModel
├── MainActivity.kt
└── MyApplication.kt
```

## 环境要求

- Android Studio Ladybug 或更高版本
- minSdk 34 (Android 14)
- targetSdk 36
- JDK 11

## 构建运行

1. 克隆项目
```bash
git clone https://github.com/surpasslike/Calendar.git
```

2. 用 Android Studio 打开项目

3. 连接设备或启动模拟器，点击 Run

## 设计文档

更详细的设计文档见博客：

- [项目概述与需求分析](https://surpasslike.github.io/calendar-01-requirements/)
- [系统架构设计](https://surpasslike.github.io/calendar-02-architecture/)
- [日程提醒通知功能](https://surpasslike.github.io/calendar-03-reminder/)
