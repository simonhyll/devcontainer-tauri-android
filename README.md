# Devcontainer for Tauri Android

## Current issues, READ THIS!!

### 1. You need to add the following to `gen/android/APP/build.gradle.kts`

This is due to Kotlin not being able to detect the correct JVM target to use. [More info here](https://kotlinlang.org/docs/gradle-configure-project.html#gradle-java-toolchains-support)

I'll probably make a PR for this so it won't have to be added manually. But for now, after you've ran `tauri android init` you'll need to manually add these lines to avoid Kotlin using Java 8 as a target while Java decides to use Java 11.

```java
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

// https://kotlinlang.org/docs/gradle-configure-project.html#gradle-java-toolchains-support
tasks.withType(JavaCompile::class.java) {
    sourceCompatibility = "11"
    targetCompatibility = "11"
}

tasks.withType(KotlinCompile::class.java) {
    kotlinOptions {
        jvmTarget = "11"
    }
}
```

### 2. The frontend can't be accessed in the devcontainer due to networking

This sounds more dire than it really is. If the Tauri CLI just has an option for specifying the IP and port manually it's as simple as creating a port forward on the host machine. The frontend already gets exposed to the host through e.g. http://localhost:1420, with port forwarding we could get it to be e.g. http://192.168.1.123:6969 and that should allow the android device to function normally.

## Getting started

### Setting up Devcontainer

1. [Set up Devcontainer development](https://code.visualstudio.com/docs/devcontainers/tutorial)
2. Copy the `.devcontainer` folder from this repo to your own project and glance over the files once so I'm not doing something malicious to your project

That's it for the project setup really! If you just set up ADB as well (and any workarounds that are currently required) you should just have to open your project in the Dev Container, run `socat` in one terminal, then start developing in a second terminal!

### ADB

My current recommended solution is to set up port forwarding so that adb inside the devcontainer actually communicates with adb on the host side, meaning your devcontainer adb will support all devices whether physical or emulated without any complicated extra setup.

```bash
# Host machine
# First run adb devices just to verify that your device shows up, if it doesn't you'll need to keep at it until it does
adb devices
# If your device showed up, kill the adb server
adb kill-server
# Now start the server in a way that supports port forwarding
adb -a -P 5037 nodaemon server start
```

```bash
# Inside the devcontainer
# Get your host ip
HOST_IP=192.168.1.123 # REPLACE WITH YOUR HOSTS IP, I might figure out later how to do this dynamically
# Start socat port forwarding
socat TCP-LISTEN:5037,reuseaddr,fork TCP:${HOST_IP}:5037
# Run adb devices to ensure everything works as intended
adb devices
```

If you can now see your device inside the devcontainer you're good to go!

### Building

Start by trying to build the project. Retry the command 2-3 times before giving up!

```bash
# Initiate the project. Note that I has to use the JS version to init along with the fix for issue nr 1 in order for it to build
pnpm tauri android init
# Use the Cargo version for running/building since I've added one to the container based on the next branch which contains some commits that fix some issues
cargo tauri android build
```

### Developing

If the project got built successfully, congratulations! You're now up and running with a Tauri Android Dev Container and should be able to run `tauri android dev`. It will use the port forwarded adb to connect to your device and install the app.

```bash
# Initiate the project. Note that I has to use the JS version to init along with the fix for issue nr 1 in order for it to build
pnpm tauri android init
# Use the Cargo version for running/building since I've added one to the container based on the next branch which contains some commits that fix some issues
cargo tauri android dev
```

> NOTE! The frontend won't show up even if the app gets installed since the device can't connect to the containers IP

## Performance

### Use WSL on Windows!

If you're on Windows you should clone your project into the WSL filesystem. Just to be clear here, that does NOT mean just opening VSCode in WSL, you have to actually move the project into the WSL filesystem into e.g. `~/Projects/your-app`.

The reason for this is disk I/O related. If you keep the files on your Windows filesystem you're going to have absolutely horrible performance.

### Allocate sufficient RAM (I use 16GB)

I had limited WSL to only use 4GB for a while, and that made VSCode constantly crash when I ran something heavy. The solution was simply to increase the RAM that WSL is allowed to use. This isn't related btw to building the container, it's related to compiling Tauri which can take a bit too much RAM.

You can probably also just limit the nr of threads running at the same time in case you don't have 16GB RAM to spare, but it'll severely reduce your performance.

### I've added mold to speed up compilation a bit

Check the Dockerfile for more info on mold. It's basically just another linker that massively speeds up compilation times for your Rust projects. If you don't want it simply remove it from the Dockerfile. It doesn't affect the Android build (at least not currently, I miiiight look into compiling it for Android, but most likely won't).

The reason I bring it up is to just make you aware of the fact that it's there. If you run `tauri build` you may actually compile it faster than on your host system! And yes, the container is fully set up to be capable of running `tauri build`, it just can't run anything graphical so you can't run `tauri dev`, but you can run unit tests and build the app, primarily for testing purposes but potentially also for cross-compilation purposes.
