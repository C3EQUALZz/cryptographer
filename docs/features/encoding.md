# Encoding Conversion

The Encoding Conversion feature allows users to convert text between different character encodings.

## Supported Encodings

- **UTF-8**: Unicode Transformation Format (8-bit)
- **ASCII**: American Standard Code for Information Interchange
- **BASE64**: Base64 encoding scheme

## Features

### Real-time Conversion

Text is converted to all supported encodings simultaneously, allowing easy comparison.

### Input

- Enter text in any encoding
- Text is automatically detected and converted

### Output

Results are displayed for all encodings:
- **UTF-8**: Original or converted UTF-8 representation
- **ASCII**: ASCII representation (if valid)
- **BASE64**: Base64 encoded representation

## Usage

### Converting Text

```kotlin
// In ViewModel
fun convertEncoding(text: String) {
    viewModelScope.launch {
        convertCommand.handle(
            ConvertTextEncodingCommand(
                text = text,
                sourceEncoding = TextEncoding.UTF8,
                targetEncodings = listOf(
                    TextEncoding.UTF8,
                    TextEncoding.ASCII,
                    TextEncoding.BASE64
                )
            )
        ).fold(
            onSuccess = { results ->
                // Update UI with conversion results
            },
            onFailure = { error ->
                // Handle error
            }
        )
    }
}
```

## Conversion Rules

### UTF-8 to ASCII

- Only ASCII characters (0-127) can be converted
- Non-ASCII characters result in an error or placeholder

### UTF-8 to BASE64

- All UTF-8 text can be converted to BASE64
- BASE64 is URL-safe encoded

### ASCII to UTF-8

- ASCII is a subset of UTF-8
- Direct conversion is possible

### BASE64 to UTF-8

- BASE64 text is decoded to UTF-8
- Invalid BASE64 results in an error

## UI Components

- **EncodingInputCard**: Text input field
- **EncodingResultsSection**: Display conversion results
- **EncodingResultCard**: Individual encoding result with copy button

## Use Cases

1. **Text Encoding Verification**: Check how text appears in different encodings
2. **Data Transmission**: Convert text for safe transmission
3. **Encoding Debugging**: Debug encoding issues
4. **Educational**: Learn about different encodings

## Learn More

- [Architecture - Domain Layer](architecture/layers/domain.md) - Encoding conversion logic
- [Architecture - Application Layer](architecture/layers/application.md) - Command handlers

