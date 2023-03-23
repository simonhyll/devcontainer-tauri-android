<div align="center">
<h1><a href="https://github.com/simonhyll/devcontainer">New dev container here!</a></h1>
</div>

# Dev Container for Tauri+Android

This is by far the least painful way I've developed for Android thus far just in general. I'll never use Android on my host system ever again.

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

This sounds more dire than it really is. If the Tauri CLI just has an option for specifying the IP and port manually it's as simple as creating a port forward on the host machine. The frontend already gets exposed to the host through e.g. http://localhost:1420, with port forwarding we could get it to be e.g. http://192.168.1.123:6969 and that should allow the android device to function normally. For now the only solution is to run the emulator from within the dev container, which doesn't perform very well, but at least it works.

## Getting started

### Setting up Devcontainer

1. [Set up Devcontainer development](https://code.visualstudio.com/docs/devcontainers/tutorial)
2. Copy the `.devcontainer` folder from this repo to your own project and glance over the files once so I'm not doing something malicious to your project
3. (Optional): If you trust me you can remove the Dockerfile and instead put `ghcr.io/simonhyll/devcontainer-tauri-android` in the `{"image":""}` part of `devcontainer.json` and remove the `{"build:{}}"` section entirely. This makes you download my prebuilt version instead. If you don't go with this option you'll be building it yourself and that can take quite a bit of time. Note that this option means you can't customize the preinstalled SDK for your developers, they'll need to use the sdkmanager to update it every time they launch their setup if you don't use the same SDK version nr as I've built into it. If you just want something that I've verified works, go with my image. If you want something that you can customize, build it yourself. Just note that if every developer builds it every time you're not just wasting precious energy from the world, you're effectively giving every single developer you have a 5-15 minute break every time there's an update to the container. If you're a team, consider making a prebuilt version, just steal the workflow from this repo and you should be good to go with that

And that's it really! If you just use `Ctrl + Shift + P` and `>Dev Containers: Open folder in Container...` you'll be up and running.

Now you can either run the emulator inside the dev container, or run the emulator on your host and port forward adb.

If you run the emulator inside the dev container everything is just plug and play, just launch it then run `cargo tauri android dev` and you'll see your app running on Android. There's performance issues, but you don't have to set up your host at all.

If you require better performance you'll have to check the ADB section below for further instructions on that. It's also currently the only solution I have for running on a physical device until I figure out how to forward the physical device (most likely a mix of mounting something in /dev and ensuring WSL has it).

### Option 1: ADB port forwarding

> NOTE!!! The frontend won't show up even if the app gets installed since the device can't connect to the containers IP. This is a known issue and will get fixed once the Tauri CLI gets patched to support setting the IP manually. Once that patch is out we can use port forwarding on the host side to expose the frontend running in the container to the device. Until
>
> Until this issue gets resolved I recommend using Option 2: Running the emulator inside the dev container

This approach relies on port forwarding from the host system into the dev container. The benefit of this approach is that you get maximum performance, since the emulator/physical device is used, the container just handles building the APK that gets installed.

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
HOST_IP=192.168.HOST.IP # REPLACE WITH YOUR HOSTS IP, I might figure out later how to do this dynamically
# Start socat port forwarding
socat TCP-LISTEN:5037,reuseaddr,fork TCP:${HOST_IP}:5037
# Run adb devices to ensure everything works as intended
adb devices
```

If you can now see your device inside the devcontainer you're good to go!

### Option 2: Running the emulator inside the devcontainer

You're gonna need to make sure you have `/dev/kvm` on your host system, on Windows you'll need to be using WSL to have that. This is related to making sure the container is capable of running the virtualization at a kernel level required for emulation.

Note that launching the emulator may appear black for a while. This is normal and is entirely related to the fact that it doesn't perform very well when ran inside the container. The fix for this is to instead opt for installing ADB and the emulator on your host system (you don't need the entire SDK and Android Studio for just those, just the cmdline-tools to install them), and use the port forwarding solution instead.

All you have to do is open a terminal inside the container and run the following command:

```bash
emulator -avd dev -no-audio -no-snapshot-load -gpu swiftshader_indirect -qemu -m 2048 -netdev user,id=mynet0,hostfwd=tcp::5555-:5555
```

### Building and running

Start by trying to build the project. Retry the command 2-3 times before giving up!

```bash
# Initiate the project. Note that I has to use the JS version to init along with the fix for issue nr 1 in order for it to build
pnpm tauri android init
# Use the Cargo version for running/building since I've added one to the container based on the next branch which contains some commits that fix some issues
cargo tauri android build
# Run on a connected device
cargo tauri android dev
```

### Running `tauri dev`

#### Windows

Running something graphical from the dev container requires an X11 server to be running. On Windows we can accomplish this with the `vcxsrv` package.

1. Run `choco install vcxsrv`
2. Run Xlaunch from the start menu, making sure you ✔️ the "Disable access control" option
3. Inside the dev container, run `export DISPLAY=192.168.HOST.IP:0.0`
4. Run `tauri dev` inside the dev container

That should be it!

#### Mac

I don't own a Mac so not sure what the steps here are, nor am I able to even attempt it. Sponsor me with the value of a Mac and I'll definitely buy one and look into it! :D

#### Linux

Haven't tested yet but it shouldn't be any more difficult than setting the DISPLAY value properly if you're on a system with an existing X11 server running (in my experience, most of them).

## Performance

### Use WSL2 on Windows!!!

If you're on Windows you should clone your project into the WSL filesystem. Just to be clear here, that does NOT mean just opening VSCode in WSL, you have to actually move the project into the WSL filesystem into e.g. `~/Projects/your-app`.

The reason for this is disk I/O related. If you keep the files on your Windows filesystem you're going to have absolutely horrible performance.

### Allocate sufficient RAM (I use 16GB)

I had limited WSL to only use 4GB for a while, and that made VSCode constantly crash when I ran something heavy. The solution was simply to increase the RAM that WSL is allowed to use. This isn't related btw to building the container, it's related to compiling Tauri which can take a bit too much RAM.

You can probably also just limit the nr of threads running at the same time in case you don't have 16GB RAM to spare, but it'll severely reduce your performance.

### I've added mold to speed up compilation a bit

Check the Dockerfile for more info on `mold`. It's basically just another linker that massively speeds up compilation times for your projects. If you don't want it simply remove it from the Dockerfile. It doesn't affect the Android build (at least not currently, I miiiight look into compiling it for Android, but most likely won't).

The reason I bring it up is to just make you aware of the fact that it's there. If you run `tauri build` you may actually compile it faster than on your host system!

Note that the quality of the build binary is somewhat lessened by `mold`. I only have second hand information on this, but apparently it can have a slightly larger size and lower performance. In my experience it's not any more unstable ay all, just a little bit less optimized. Perfect for development, but if you're going to build for production (which you shouldn't do in a **dev** container), don't use it.

## Todo:

1. Ensure the Tauri CLI gets the necessary patch for selecting an IP manually so that devices on the host system can show the frontend
2. Ensure the Kotlin version issue gets resolved in Tauri so that doesn't have to be applied manually
3. Optimize the Dockerfile a bit
4. Host a prebuilt Docker container on ghcr.io
5. Figure out why `git` isn't working for me correctly inside the dev container even if it should just magically work according to Microsoft
