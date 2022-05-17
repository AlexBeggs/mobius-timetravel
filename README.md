# Mobius Time Travel - Visualize and debug state changes

[![build](https://github.com/AlexBeggs/mobius-timetravel/actions/workflows/build.yaml/badge.svg)](https://github.com/AlexBeggs/mobius-timetravel/actions/workflows/build.yaml)

## Setup

- Add `mobius-timetravel` dependency
- Add `mobius-timetravel-<server-type>` e.g. `mobius-timetravel-server-http` dependency
`Compose Update<M,E,F>` with `TimeTravelUpdate<M,E,F>`

```kotlin
val timeTravelUpdate: TimeTravelUpdate<NumberModel, NumberEvent, NumberEffect> = TimeTravelUpdate.from(update)
```


- Compose `MobiusLoop.Controller<M,E,F>` with `TimeTravelController<M,E,F>`
```kotlin
val timeTravelController =
   TimeTravelController(
       MobiusAndroid.controller(createLoop(effectHandlers, timeTravelUpdate), defaultModel),
       timeTravelUpdate,
       TimeTravelServer.timeTravelServer
   )
```

- Connect `TimeTravelController` and `TimeTravelUpdate`
```kotlin
TimeTravelController.connect(timeTravelController, timeTravelUpdate)
```

- Execute `adb forward tcp:8080 tcp:8080` (required for HTTP server) on the command line for the emulator/physical device

## Download

```groovy
repositories {
  mavenCentral()
}
dependencies {
  implementation 'dev.alexbeggs.mobius-timetravel:timetravel-core:0.1.0'
  implementation 'dev.alexbeggs.mobius-timetravel:timetravel-server-http:0.1.0'
}
```

<details>
<summary>Snapshots of the development version are available in Sonatype's snapshots repository.</summary>
<p>

```groovy
repositories {
  maven {
    url 'https://oss.sonatype.org/content/repositories/snapshots/'
  }
}
dependencies {
  implementation 'dev.alexbeggs.mobius-timetravel:timetravel-core:0.1.0-SNAPSHOT'
  implementation 'dev.alexbeggs.mobius-timetravel:timetravel-server-http:0.1.0-SNAPSHOT'
}
```

</p>
</details>


# License

    Copyright 2022 Alex Beggs

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.