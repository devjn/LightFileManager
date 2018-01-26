# LightFileManager
Lightweight embeddable File Manager

## Example
The example located in :app module.

To see this library in action you can refer to [MereFileExplorer](https://github.com/devjn/MereFileExplorer) which is built on top of this project.

## Installation

If you are using Gradle and the JCenter Maven Repository, installing the library is as simple as
adding a new dependency statement.

```gradle
dependencies {
    compile 'com.github.devjn:file-manager:1.0.0-aplha3'
}
```
## Usage

First, FileManager needs to be initialized, the good place for this is Application class inside `onCreate`:

```java
FileManager.initialize(applicationContext);
```

Example of starting file manager for select content:

```java
FileManager.with(this).showHidden(false).setContentType("image/*")
        .startFileManager(path -> someCallback());
```
It's also possible to use file manager as dialog window:

```java
FileManager.with(this).showHidden(false).setContentType(FileManager.Filter.IMAGE)
        .showDialogFileManager(path -> Toast.makeText(context, "onResult: " + path, Toast.LENGTH_SHORT).show());
```

#### Config

To configure file manager use `FileManager.getInstance().getConfig()`


## License

This project is licensed under the Apache License 2.0 - see the [LICENSE.md](LICENSE.md) file for details
