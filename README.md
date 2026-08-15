# sharazan-http

**Sharazan** — модульный бэкенд-фреймворк на Kotlin, вдохновлённый архитектурой Ktor (declare-then-start композиция через Koin, без Spring-магии).

**http** — coroutine-based HTTP-фреймворк: hand-rolled Netty-транспорт под капотом, http4k-типы (`Request`/`Response`/`Method`/`Uri`) и Jackson-сериализация, роутинг и request/response `pipeline` через `core`.

## Стек

- Netty
- http4k-core, http4k-format-jackson
- kotlinx.coroutines
- core, logging (sharazan)

## Maven-координаты

```kotlin
implementation("com.github.37hulk37:sharazan-http:1.0.0")
```
