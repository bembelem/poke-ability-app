# PokeAbilityApp

## ФИО, группа
Ведёхин Никита Васильевич, группа Б9123-09.03.03(4)

## API
**PokéAPI** (https://pokeapi.co/)
Используется эндпоинт `/api/v2/ability` - список способностей с описанием эффектов, поколением и покемонами.

## Flow (ДЗ 6)

### Источники данных (5 независимых)
- `queryFlow` — строка поиска (`MutableStateFlow`)
- `filterFlow` — выбранный фильтр (`MutableStateFlow`)
- `favouritesFlow` — избранное из Room (`Flow` из DAO)
- `refreshRequests` — ручной refresh (`MutableSharedFlow`)
- `_allAbilities` — начальный список способностей из API

### Реактивная логика
1) Поиск по мере ввода:
```
queryFlow -> debounce(400) -> distinctUntilChanged -> flatMapLatest(search) -> scan -> searchState
```
Поиск работает по **точному названию** способности. Так как эндпоинт `/api/v2/ability/{name}` принимает только точное имя.

Финальный UI собирается из всех источников:
```
queryFlow + filterFlow + favouritesFlow + searchState + allAbilities -> combine -> uiState
```

### Операторы
- `combine` — объединение 5 потоков в единый `uiState`
- `flatMapLatest` — отмена устаревших поисковых запросов при быстром вводе
- `debounce(400)` — задержка перед поиском, чтобы не спамить API
- `distinctUntilChanged` — игнорирование одинаковых запросов
- `scan` — накопление состояния поиска (Loading → Success/Error)
- `map` — преобразование списка избранного в `Set<Int>`
- `onStart` — инициализация refresh при старте

### Поведение
- Избранное из Room обновляется автоматически — свайп сразу отражается в UI без ручной перезагрузки
- При быстром вводе старые запросы к API отменяются автоматически через `flatMapLatest`
- Refresh через `SharedFlow` переиспользует тот же pipeline поиска

## Скриншоты
| Loading | Error | List | Favourites | Detail |
|---------|-------|------|------------|--------|
| <img width="576" height="1280" alt="image" src="https://github.com/user-attachments/assets/a466efbd-b56f-43f4-a93d-55f28cbe3592" /> | <img width="576" height="1280" alt="image" src="https://github.com/user-attachments/assets/f916ca2e-2fd8-4574-b153-e0711fc86515" /> | <img width="576" height="1280" alt="image" src="https://github.com/user-attachments/assets/2cdddc95-e13a-4952-8171-7bdc1d2780ae" /> | <img width="576" height="1280" alt="image" src="https://github.com/user-attachments/assets/c0c72ab4-e620-4c0b-92fc-8b6c5662874a" /> | <img width="576" height="1280" alt="image" src="https://github.com/user-attachments/assets/55cd40f7-ffc3-42fb-bea4-39bb7e833778" /> |