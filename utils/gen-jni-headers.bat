@echo off

set CLASSPATH=%ANDROID_SDK_ROOT%\platforms\android-23\android.jar;%~dp0header-deps\commons-io-2.8.0.jar;.

call kotlinc -cp "%CLASSPATH%" %~dp0..\src\main\java\com\oasisfeng\nevo\decorators\wechat\EmojiJNI.kt

call javah -jni -cp "%CLASSPATH%" -o EmojiJNI.h com.oasisfeng.nevo.decorators.wechat.EmojiJNI

del /f /s /q %~dp0com
del /f /s /q %~dp0META-INF
rmdir /s /q %~dp0com
rmdir /s /q %~dp0META-INF
