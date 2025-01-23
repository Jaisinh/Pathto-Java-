# pathto

A fast command-line utility to search for files and directories across your entire system. Performs case-insensitive searches, handles filenames with spaces, and automatically skips system directories for better performance.

## Installation

1. Clone this repository
```bash
git clone https://github.com/Jaisinh/pathto
```

## Usage
Basic usage:
```bash
javac OptimizedFileSearchUtility.java
```
```bash
java OptimizedFileSearchUtility <FileName>
```


## Examples

```bash
$ java OptimizedFileSearchUtility document
Searching for files or folders named 'document'...


Found the following matches:
Directory: /Users/username/Documents
File: /Users/username/Documents/document.pdf
```

## Features
- searches all the files and prints every single occurance of the name
- Searches entire system recursively
- Case-insensitive matching
- Handles filenames with spaces
- Shows whether matches are files or directories
- Fast performance through optimized directory traversal

## Requirements

- Unix-like operating system (Linux, macOS)


## License

MIT License 
