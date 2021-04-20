[![Build Status](https://www.travis-ci.com/RomanRusanov/chat_rest_api.svg?branch=master)](https://www.travis-ci.com/RomanRusanov/chat_rest_api)
[![Codacy Badge](https://app.codacy.com/project/badge/Grade/47d17ab425bc42b7b572924affed5771)](https://www.codacy.com/gh/RomanRusanov/chat_rest_api/dashboard?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=RomanRusanov/chat_rest_api&amp;utm_campaign=Badge_Grade)

# чат на REST Api через Spring Boot
Модели Person, Role, Room, Message.
Приложение реализовывает чат c комнатами.

### 1. Регистрация пользователя

http://localhost:8080/users/sign-up

![image](ScrenShoots/Screenshot_1.png)

### 2. Аутентификация пользователя получение jwt token

http://localhost:8080/login

![image](ScrenShoots/Screenshot_2.png)

### 3. Добавление новой роли

http://localhost:8080/role/

![image](ScrenShoots/Screenshot_3.png)

### 4. Добавление нового пользователя

http://localhost:8080/person/

![image](ScrenShoots/Screenshot_4.png)

### 5. Добавление новой комнаты

http://localhost:8080/room/

![image](ScrenShoots/Screenshot_5.png)

### 6. Добавление сообщения

http://localhost:8080/message/

![image](ScrenShoots/Screenshot_6.png)

### 7. Обновление данных пользователя

http://localhost:8080/person/

![image](ScrenShoots/Screenshot_7.png)

Response json
```
{
    "id": 6,
    "username": "user3 tester",
    "password": "123",
    "messages": [
        {
        "id": 1,
        "description": "Message from user(use3) into room(new room)",
        "personId": 6,
        "roomId": 3
        }
    ],
    "roles": [
        {
            "id": 2,
            "authority": "ROLE_NEWROLE"
        },
        {
            "id": 3,
            "authority": "ROLE_TESTER"
        }
    ],
    "rooms": [
        {
            "id": 8,
            "name": "only for testers",
            "messages": []
        }
    ]
}
```

### 8. Удаление пользователя и всех сообщений которые создал

http://localhost:8080/person/{id}

![image](ScrenShoots/Screenshot_8.png)

### 9. Удаление сообщения

http://localhost:8080/message/{id}

![image](ScrenShoots/Screenshot_9.png)

### 10. Удаление роли

http://localhost:8080/role/{id}

![image](ScrenShoots/Screenshot_9.png)