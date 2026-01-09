# Домен Text (Текст)

Домен для работы с текстовыми данными в криптографическом приложении.

## Структура

### Entity (Сущности)
- **Text** - основная сущность, представляющая текстовые данные
  - `content: String` - содержимое текста
  - `encoding: TextEncoding` - кодировка текста (UTF8, ASCII, BASE64)
  - Свойства: `length`, `isEmpty`, `isNotEmpty`

- **EncryptionKey** - ключ шифрования
  - `value: ByteArray` - значение ключа
  - `algorithm: EncryptionAlgorithm` - алгоритм шифрования (AES_128, AES_192, AES_256)

- **EncryptedText** - зашифрованный текст
  - `encryptedData: ByteArray` - зашифрованные данные
  - `algorithm: EncryptionAlgorithm` - использованный алгоритм
  - `initializationVector: ByteArray?` - вектор инициализации (IV)

### Repository (Репозиторий)
- **TextRepository** - интерфейс для работы с текстовыми данными
  - `saveText()` - сохранение текста
  - `getText()` - получение текста по ID
  - `deleteText()` - удаление текста
  - `getAllTexts()` - получение всех текстов
  - `validateText()` - валидация текста

### Service (Доменные сервисы)
- **EncryptionService** - интерфейс для криптографических алгоритмов
  - `encrypt()` - шифрование данных
  - `decrypt()` - дешифрование данных
  - `generateKey()` - генерация ключа

- **AesEncryptionService** - реализация AES шифрования
  - Поддерживает AES-128, AES-192, AES-256
  - Использует режим GCM (Galois/Counter Mode)
  - Генерирует случайный IV для каждого шифрования

### Use Cases (Сценарии использования)

1. **ValidateTextUseCase** - валидация текста
   - Проверяет, что текст не пустой
   - Проверяет максимальную длину (1MB)
   - Проверяет соответствие кодировке

2. **PrepareTextForEncryptionUseCase** - подготовка текста к шифрованию
   - Нормализует текст (удаляет лишние пробелы)
   - Приводит кодировку к UTF8
   - Валидирует текст перед подготовкой

3. **ConvertTextEncodingUseCase** - конвертация между кодировками
   - UTF8 ↔ BASE64
   - UTF8 ↔ ASCII
   - ASCII ↔ UTF8

4. **SaveTextUseCase** - сохранение текста с валидацией

5. **GetTextUseCase** - получение текста по ID

6. **GetAllTextsUseCase** - получение всех текстов

7. **DeleteTextUseCase** - удаление текста

8. **EncryptTextUseCase** - шифрование текста
   - Подготавливает текст к шифрованию
   - Выполняет шифрование через EncryptionService
   - Возвращает EncryptedText

9. **DecryptTextUseCase** - дешифрование текста
   - Дешифрует EncryptedText через EncryptionService
   - Преобразует байты обратно в Text

10. **GenerateEncryptionKeyUseCase** - генерация ключа шифрования
    - Генерирует криптографически стойкий ключ для указанного алгоритма

## Пример использования

```kotlin
// Создание текста
val text = Text(
    content = "Привет, мир!",
    encoding = TextEncoding.UTF8
)

// Валидация
val validateUseCase = ValidateTextUseCase()
val validationResult = validateUseCase(text)

// Подготовка к шифрованию
val prepareUseCase = PrepareTextForEncryptionUseCase(validateUseCase)
val preparedText = prepareUseCase(text).getOrNull()

// Конвертация кодировки
val convertUseCase = ConvertTextEncodingUseCase()
val base64Text = convertUseCase(text, TextEncoding.BASE64).getOrNull()

// Шифрование текста
val aesService = AesEncryptionService()
val generateKeyUseCase = GenerateEncryptionKeyUseCase(aesService)
val key = generateKeyUseCase(EncryptionAlgorithm.AES_256).getOrNull()

val prepareUseCase = PrepareTextForEncryptionUseCase(validateUseCase)
val encryptUseCase = EncryptTextUseCase(aesService, prepareUseCase)
val encryptedText = key?.let { encryptUseCase(text, it).getOrNull() }

// Дешифрование текста
val decryptUseCase = DecryptTextUseCase(aesService)
val decryptedText = key?.let { decryptUseCase(encryptedText!!, it).getOrNull() }
```

## Принципы Clean Architecture

- **Domain слой не зависит от других слоев** - нет зависимостей от Android, фреймворков
- **Use Cases инкапсулируют бизнес-логику** - каждый use case решает одну задачу
- **Repository интерфейс в domain** - реализация будет в data слое
- **Entity - чистые данные** - без логики, только структура данных

## Примечания по безопасности

- **AES-GCM** - выбран режим GCM для обеспечения аутентификации данных
- **IV (Initialization Vector)** - генерируется случайно для каждого шифрования
- **Ключи** - должны храниться безопасно (Android Keystore, зашифрованное хранилище)
- **Валидация** - все входные данные валидируются перед обработкой

## Следующие шаги

1. Создать data слой с реализацией TextRepository
2. Создать presentation слой с ViewModel и UI
3. Добавить другие алгоритмы шифрования (RSA, ChaCha20, и т.д.)
4. Реализовать безопасное хранение ключей (Android Keystore)

