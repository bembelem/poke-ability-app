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

| Список | Свайп и спиок избранных                      | Детали                       |
|--|----------------------------|------------------------------|
|<img width="576" height="1280" alt="image" src="https://github.com/user-attachments/assets/ddc254da-72ca-4893-9d0a-04aec02275fb" />|<img width="576" height="1280" alt="image" src="https://github.com/user-attachments/assets/05e04ebf-d38a-411e-a7df-3f29fcb35d40" />|<img width="576" height="1280" alt="image" src="https://github.com/user-attachments/assets/7e808fbc-99a1-49bb-a04c-0e42fa1f8d8c" />|









