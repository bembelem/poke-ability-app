# PokeAbilityApp

## ФИО, группа
Ведёхин Никита Васильевич, группа Б9123-09.03.03(4)

## API
**PokéAPI** (https://pokeapi.co/) 
Используется эндпоинт `/api/v2/ability` - список способностей с описанием эффектов, поколением и покемонами.

## Что храним в Room
**Таблица:** `favourites` - хранит `id` способности  
**Сценарий:** Избранное - пользователь свайпает способность, она сохраняется в БД

## Как проверить
1. Открыть приложение - список способностей
2. Свайпнуть любую способность влево или вправо - появится иконка ❤️
3. Перейти на вкладку **Favourites** - способность там
4. Закрыть приложение полностью
5. Открыть снова - вкладка **Favourites** - способность осталась

## Скриншоты
| Loading | Error | List | Favourites | Detail |
|---------|-------|------|------------|--------|
| <img width="576" height="1280" alt="image" src="https://github.com/user-attachments/assets/a466efbd-b56f-43f4-a93d-55f28cbe3592" /> | <img width="576" height="1280" alt="image" src="https://github.com/user-attachments/assets/f916ca2e-2fd8-4574-b153-e0711fc86515" /> | <img width="576" height="1280" alt="image" src="https://github.com/user-attachments/assets/2cdddc95-e13a-4952-8171-7bdc1d2780ae" /> | <img width="576" height="1280" alt="image" src="https://github.com/user-attachments/assets/c0c72ab4-e620-4c0b-92fc-8b6c5662874a" /> | <img width="576" height="1280" alt="image" src="https://github.com/user-attachments/assets/55cd40f7-ffc3-42fb-bea4-39bb7e833778" /> |
