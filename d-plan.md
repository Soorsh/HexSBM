# Agent Development Plan: HexSBM

**Attention Agent:** This is your primary operational file. Treat it as your "working memory" and "to-do list" for the HexSBM project.

Your core workflow is:
1.  **КОММУНИКАЦИЯ НА РУССКОМ:** Всегда общайтесь со мной на русском языке.
2.  **ЧИТАЙ МЕНЯ СНАЧАЛА:** В начале каждой сессии, после первого запроса, немедленно читай этот файл.
3.  **ОБНОВЛЯЙ МЕНЯ ВСЕГДА:** После каждого действия (изменение кода, анализ) ты ДОЛЖЕН обновить этот файл.
    *   Изменяй статус задачи в соответствии с её состоянием.
    *   Добавляй новые задачи по мере их обнаружения.
4.  **НЕ МЕНЯЙ СТРУКТУРУ:** Не изменяй структуру, инструкции или заголовки этого документа.
5.  **ПОДТВЕРЖДЕНИЕ СТАТУСА:** Ты можешь изменить статус на `Выполнено` только после моего прямого подтверждения.

**Статусы задач:**
*   `К выполнению`: Задача ожидает начала работы.
*   `В работе`: Ты сейчас активно работаешь над этой задачей.
*   `Ожидает одобрения`: Ты считаешь, что выполнил задачу и ждёшь моей проверки. В этом состоянии ты будешь спрашивать, всё ли работает корректно. Для проверки твоих изменений используй команду `./gradlew clean build`.
*   `Выполнено`: Я подтвердил, что задача полностью решена.

---

## Current Tasks (Ordered by Priority)

### Part 1: Release Preparation (High Priority)

*   **Task 1: Improve UI/UX**
    *   **Status:** `Выполнено`
    *   **Files:** `ConfigPanel.java`, `NumberField.java`, `CheckBoxField.java`, `CycleField.java`, `Button.java`, `SpellBookScreen.java`
    *   **Details:**
        *   Реализована функциональность кнопки 'Сохранить', автоматическое сохранение при закрытии удалено.
        *   Уменьшена ширина полей ввода. `Выполнено`
        *   Добавлена рамка/подсветка при редактировании. `Выполнено`
        *   Заменен ToggleField на визуальный переключатель. `Выполнено`
        *   Переименован переключатель "Без градиента" в "Градиент" и инвертирована его логика. `Выполнено`
        *   Улучшен внешний вид CycleField. `Выполнено`
        *   Перемещен блок "Сброс" в самый низ. `Выполнено`
        *   Изменен порядок секций: Поведение, Размеры и позиции, Цвет и внешний вид. `Выполнено`
        *   Добавлены визуальные разделители. `Выполнено`
        *   Сделаны кнопки визуально узнаваемыми. `Выполнено`
        *   Добавлен эффект наведения на все элементы (поля ввода, тумблеры). `Выполнено`
        *   Исправлено наложение заголовка группы 'Поведение' на заголовок панели. `Выполнено`
        *   Элементы ввода текста:
            *   Большое количество символов выходят за рамки элемента. `Выполнено`
            *   При вводе большого числа в RGB оно сбрасывается до 231, а не 255. `Выполнено`
            *   В RGB при скроле больше 255 значение сбрасывается до 0. `Выполнено`
        *   Конфиг панель:
            *   Элементы конфиг панели налазят на рамку (белая рамка в 1px). `Выполнено`
            *   CONTENT_HEIGHT сделан больше. `Выполнено`
        *   Если одно из колец закрывает кнопку открытия конфига (справа) - конфиг нельзя открыть. `Выполнено`
        *   Заменены ТУМБЛЕРЫ на чекбоксы. `Выполнено`
        *   Переименован класс `ToggleField` в `CheckBoxField` и обновлены все ссылки. `Выполнено`
        *   Заглушки "Сохранить параметры" теперь функциональны. `Выполнено`

*   **Task 2: Localization**
    *   **Status:** `Выполнено`
    *   **Files:** All files with `Text.literal("...")`, `lang/*.json`
    *   **Details:**
        *   Заменить все `Text.literal` на `Text.translatable`.
        *   Убедиться в наличии и полноте `en_us.json`, `ru_ru.json`, `zh_cn.json`.

*   **Task 3: Code Cleanup**
    *   **Status:** `Выполнено`
    *   **Files:** Entire codebase
    *   **Details:**
        *   Удалить мёртвый код.
        *   Устранить дублирование.
        *   Упростить сложные методы.
        *   Привести стиль к единому виду.

---

### Part 2: Future Development (Lower Priority)

*   **Task 4: Implement New Features**
    *   **Status:** `К выполнению`
    *   **Details:**
        *   Режим подсветки секторов ("По заклинанию", "Всегда").
        *   Режим выбора по зажатию клавиши.

*   **Task 5: Bug Fixes and Small Improvements**
    *   **Status:** `К выполнению`
    *   **Details:**
        *   Исправить баг с прокруткой в NumberField.
        *   Реализовать копирование/вставку по ПКМ.
        *   Улучшить поведение CycleField и ToggleField.
        *   **Исправить работу альфа-канала (прозрачности).**

*   **Task 6: Performance and Architecture**
    *   **Status:** `К выполнению`
    *   **Details:**
        *   Оптимизировать перерисовку `ColorScheme`.
        *   Рассмотреть кэширование пигментов.

*   **Task 7: Refactor Button Click Detection**
    *   **Status:** `К выполнению`
    *   **Details:**
        *   Пересмотреть функционал определения клика по кнопкам для более надежной и гибкой реализации.

*   **Task 8: Refactor NumberField Architecture**
    *   **Status:** `К выполнению`
    *   **Details:**
        *   После выпуска бета-версии необходимо будет полностью пересмотреть архитектуру `NumberField` для более гибкой и надежной системы ограничений значений.


---

<details>
<summary>Project Details (Build Info, Dependencies, Structure)</summary>

## Build Properties

These properties are defined in `gradle.properties`.

| Property            | Value          |
| ------------------- | -------------- |
| `minecraft_version` | 1.20.1         |
| `yarn_mappings`     | 1.20.1+build.10|
| `loader_version`    | 0.18.2         |
| `loom_version`      | 1.6.12         |
| `mod_version`       | 0.1.1          |
| `hexcasting_version`| 0.11.3         |
| `fabric_version`    | 0.92.6+1.20.1  |

## Dependencies

Key dependencies as defined in `build.gradle`:

-   **Minecraft**: `com.mojang:minecraft:1.20.1`
-   **Yarn Mappings**: `net.fabricmc:yarn:1.20.1+build.10:v2`
-   **Fabric Loader**: `net.fabricmc:fabric-loader:0.18.2`
-   **Fabric API**: `net.fabricmc.fabric-api:fabric-api:0.92.6+1.20.1`
-   **Hex Casting**: `hexcasting-fabric-1.20.1-0.11.3.jar` (local dependency)

## Source Code Structure

Source code structure from the `src` directory:

```
src
├── client
│   └── java
│       └── com
│           └── hexsbm
│               ├── config
│               │   ├── ConfigManager.java
│               │   └── HexSBMConfig.java
│               ├── HexSBMClient.java
│               ├── keybinds
│               │   └── KeyBindManager.java
│               └── screen
│                   ├── nbt
│                   │   └── SpellbookNbtManager.java
│                   ├── pigment
│                   │   └── PigmentColorRegistry.java
│                   ├── SpellBookScreen.java
│                   └── ui
│                       ├── ColorScheme.java
│                       ├── ConfigPanel.java
│                       ├── elements
│                       │   ├── Button.java
│                       │   ├── CheckBoxField.java
│                       │   ├── ConfigControl.java
│                       │   ├── CycleField.java
│                       │   ├── DividerControl.java
│                       │   ├── LabelControl.java
│                       │   └── NumberField.java
│                       └── RadialRenderer.java
└── main
    ├── java
    │   └── com
    │       └── hexsbm
    │           ├── HexSBM.java
    │           └── network
    │               ├── ChangeSpellbookPagePacket.java
    │               ├── UpdateGroupIconPacket.java
    │               └── UpdatePageIconPacket.java
    └── resources
        ├── assets
        │   └── hexsbm
        │       ├── icon (1) (1).png:Zone.Identifier
        │       ├── icon2.png
        │       ├── icon.png
        │       └── lang
        │           ├── en_us.json
        │           ├── ru_ru.json
        │           └── zh_cn.json
        ├── fabric.mod.json
        └── hexsbm.mixins.json
```

</details>