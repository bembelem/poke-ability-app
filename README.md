# PokeAbilityApp

**VariantCode: POKE-ABILITY-MOD_B6_SWIPE_ACTIONS**

Android-приложение на Jetpack Compose, отображающее список способностей покемонов (abilities) из PokéAPI v2.

---

## Стек

- Kotlin + Jetpack Compose + Coroutines
- Retrofit + Gson
- Hilt (DI)
- Navigation Compose
- Architecture: DTO → UI-model + Repository + ViewModel

---

## Используемые endpoints

| Назначение | Endpoint | Пример |
|---|---|---|
| Список способностей | `GET /ability` | `https://pokeapi.co/api/v2/ability?limit=20&offset=0` |
| Детали способности | `GET /ability/{id}` | `https://pokeapi.co/api/v2/ability/1/` |

Base URL: `https://pokeapi.co/api/v2/`

---

## Модификатор MOD_B6_SWIPE_ACTIONS

Свайп по элементу списка влево или вправо добавляет / убирает способность из избранного.

- Свайп вправо — добавить в избранное (зелёный фон, иконка ♡)
- Свайп влево — убрать из избранного (красный фон, иконка ♥)
- Элемент не удаляется из списка, возвращается на место
- Избранные отмечены иконкой ♥ в правой части карточки
- Состояние хранится в ViewModel в рамках сессии

Реализовано через `SwipeToDismissBox` из Material3.

---

## Экраны

**Список (List screen)**
- Отображает 20 способностей (первая страница)
- Состояния: Loading / Content / Error с кнопкой Retry
- Свайп для добавления/удаления из избранного

**Детали (Detail screen)**
- Открывается по клику на элемент списка
- Загружает детали отдельным запросом по id
- Отображает: название, поколение, короткое и полное описание, флейвор-текст, список покемонов с этой способностью
- Состояния: Loading / Content / Error с кнопкой Retry

---

## Архитектура

```
data/
├── dto/          — DTO классы (AbilityListDto, AbilityDto)
├── AbilityMapper — маппинг DTO → UI-модель
├── service/      — Retrofit интерфейс (PokeApiService)
└── repository/   — AbilityRepositoryImpl

domain/
├── model/        — UI-модели (AbilityItem, AbilityDetail)
└── repository/   — интерфейс AbilityRepository

di/               — Hilt модули (NetworkModule, RepositoryModule)

ui/
├── list/         — AbilityListScreen + AbilityListViewModel
├── detail/       — AbilityDetailScreen + AbilityDetailViewModel
├── common/       — UiState, ErrorState
└── NavGraph      — навигация
```

---

## Скриншоты

| Список | Свайп                      | Детали                       |
|--|----------------------------|------------------------------|
|![details](https://github.com/user-attachments/assets/e2c69e37-c09b-422b-985c-30bf552d1119) | ![list](https://github.com/user-attachments/assets/64c49171-2f71-4b43-a8fd-e2f2d04a3631) | ![swipe](https://github.com/user-attachments/assets/be78fb1a-566f-4c70-aac0-950ca862f2d7) |



