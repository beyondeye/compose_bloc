in order to run web samples, launch a command prompt in this directory
and write 

```../../gradlew jsBrowserRun```
Instead of manually compiling and executing a Kotlin/JS project every time you want to see the changes you made, you can use the continuous compilation mode:
```../../gradlew jsBrowserRun --continuous```
or run the same gradle task from IDE (it is called jsBrowserRun, under the group: kotlin browser)