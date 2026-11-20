# AO3 纯净阅读器 (ArchiveReader for Android)

专为 Android 移动端打造的 **AO3 (Archive of Our Own) 纯净同人小说阅读器**。

彻底告别原生网页字体拥挤、排版细小、翻页卡顿与成人内容反复弹窗验证的困扰！支持一键导入解析、书架与离线缓存、滑动与无动画瞬切翻页模式、全自由背景与字体定制，以及全套界面主题变色。

已预设 **GitHub Actions CI/CD 自动构建工作流**，只要上传到 GitHub 即可全自动打包生成可直接安装运行的 Android APK。

---

## ✨ 核心特色功能

### 1. 📖 双阅读模式自由切换
* **无动画瞬切翻页模式 (零延迟、零晃眼)**：
  * 点击屏幕右侧（或音量下键）直接切换下一页；
  * 点击屏幕左侧（或音量上键）切换上一页；
  * **完全取消滑动/淡入淡出动画**，像墨水屏电纸书一样瞬时切页，长时间阅读眼睛不酸痛。
* **平滑垂直滑动模式**：
  * 流畅竖屏连续滚动，支持章节快速导航与阅读进度记录。

### 2. 🎨 自由背景与配色定制
* **7 大经典预设护眼主题**：
  * 纯白明亮 (`#FFFFFF`)
  * 暖阳羊皮纸护眼 (`#F7EED8`)
  * 清新豆沙绿 (`#E8F5E9`)
  * 极简水墨灰 (`#F0F2F5`)
  * 石墨暗灰 (`#232529`)
  * 极致纯黑省电 OLED (`#000000`)
  * 静谧深蓝夜读 (`#0D1B2A`)
* **100% 自由调色板**：支持 RGB 滑块与 Hex 十六进制色码，自由设定专属背景色与文字色。

### 3. 🔤 字体、字号与间距深度调节
* **字体风格**：支持系统默认、经典衬线体（宋体风格）、现代黑体、等宽字体。
* **字号无级调节**：12sp 到 36sp，支持 A- / A+ 快速微调。
* **行间距与段间距**：1.2 倍至 2.4 倍行距自由拉动，首行自动缩进。
* **屏幕常亮保护**：阅读时自动保持屏幕唤醒。

### 4. 🌈 调节整个界面的颜色 (App-Wide Theme)
* 不仅是阅读页面，包括顶部导航栏、底部菜单、按钮、开关、标签高亮等**整个应用的主题基调色**均可一键调节。
* 提供经典 AO3 绯红、樱粉魅影、水晶绛紫、皇家邃蓝、翡翠竹青、落日琥珀等预设，也可自定义任意主色。

### 5. 🌐 美化 AO3 探索与一键解析
* **内置 AO3 浏览器**：注入移动端美化 CSS，优化排版与标签显示。
* **自动绕过成人内容警告**：预设 `view_adult=true` Cookie，无需繁琐点击确认。
* **✨ 浮动一键进入纯净阅读**：浏览到任何作品页面时，底部自动浮现一键纯净阅读胶囊按钮。
* **支持直接粘贴 URL 或 ID**：输入如 `https://archiveofourown.org/works/12345678` 或纯数字 `12345678` 即可秒级解析全文。
* **书架与离线缓存**：已加载的作品会自动保存在本地，无需网络也可随时离线畅读。

---

## 🚀 如何上传到 GitHub 并自动打包下载 APK

项目根目录下已配置好 `.github/workflows/build-apk.yml`。按照以下步骤，无需在电脑上配置 Java 和 Android SDK，GitHub 会全自动帮您编译好 APK：

### 第一步：创建 GitHub 仓库
1. 登录您的 [GitHub](https://github.com/) 账号；
2. 点击右上角的 `+` -> **New repository**；
3. 填写仓库名称（例如 `ao3-reader-android`），设为 **Public** 或 **Private** 均可，点击 **Create repository**。

### 第二步：将本项目代码推送到 GitHub
在本项目目录中打开终端（Git Bash 或 PowerShell），运行以下命令：

```bash
# 进入项目所在文件夹
cd C:\Users\Administrator\.gemini\antigravity\scratch\ao3-reader-android

# 初始化 git 仓库
git init

# 添加所有文件
git add .

# 提交初始版本
git commit -m "feat: Initial commit for AO3 Reader Android"

# 关联您的 GitHub 仓库（将 YOUR_USERNAME 和 YOUR_REPO 换成您的实际地址）
git branch -M main
git remote add origin https://github.com/YOUR_USERNAME/YOUR_REPO.git

# 推送代码到 GitHub
git push -u origin main
```

### 第三步：在 GitHub 上下载打包好的 APK
1. 打开您在 GitHub 上的仓库页面；
2. 点击顶部的 **Actions** 标签页；
3. 您会看到名为 **"Build AO3 Reader APK"** 的工作流正在自动运行（通常耗时约 2-3 分钟）；
4. 运行完成后（显示绿色的对勾 ✔），点击进入该次运行记录；
5. 在页面最底部的 **Artifacts** 区域，直接点击下载 **`AO3Reader-Debug-APK`** 或 **`AO3Reader-Release-APK`** 压缩包；
6. 解压下载的 zip 包，即可得到 `.apk` 安装文件，发送至安卓手机安装即可直接使用！

> **提示**：构建的 Release APK 已自动配置 Android Debug Keystore 签名，无需自行生成签名证书，在任何安卓手机上均可直接安装运行。

---

## 📱 手机安装与使用说明

1. 将生成的 `.apk` 文件传输至手机（通过微信、QQ、网盘或 USB 数据线）；
2. 点击安装（若系统提示“未知来源应用”，允许本次安装即可）；
3. 打开应用：
   * **快捷阅读**：在首页直接粘贴 AO3 网址或 ID，点击【立即解析并阅读】；
   * **探索浏览**：点击底栏【AO3 探索】，像普通浏览器一样找文，找到后点击右下角浮现的【✨ 一键进入纯净阅读】；
   * **调节翻页模式**：在阅读界面轻触屏幕中央调出菜单，点击【排版与颜色】或底部的模式切换按钮，可在【平滑滑动】和【无动画翻页】之间自由切换；
   * **调节界面与背景颜色**：在阅读菜单可选择 7 种背景或自定义背景色；在主页右上角调色盘图标中可调节整个 App 的主题色。

---

## 🛠️ 项目技术栈
* **架构语言**：Kotlin + Jetpack Compose + Material 3
* **HTML 解析引擎**：Jsoup 1.17.2 (深度提取标题、作者、分级、标签、简介及章节正文)
* **网络与 Cookie 管理**：OkHttp 4.12.0 (持久化维持 AO3 会话与成人内容授权)
* **本地存储**：Gson + SharedPreferences/JSON File (离线缓存已加载作品)
* **CI/CD**：GitHub Actions (`ubuntu-latest` + JDK 17 + Gradle 8.7)
