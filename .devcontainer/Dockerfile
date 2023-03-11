ARG VARIANT="jammy"

ARG ANDROID_SDK_TOOLS_VERSION="8512546"
ARG ANDROID_PLATFORM_VERSION="31"
ARG ANDROID_BUILD_TOOLS_VERSION="33.0.0"
ARG NDK_VERSION="25.0.8775105"

ARG USERNAME=vscode
ARG USER_UID=1000
ARG USER_GID=$USER_UID
ARG DEPENDENCIES="build-essential curl libappindicator3-dev libgtk-3-dev librsvg2-dev libssl-dev libwebkit2gtk-4.1-dev wget"
ARG EXTRA_DEPENDENCIES=""

FROM mcr.microsoft.com/vscode/devcontainers/base:0-${VARIANT} AS android_sdk
WORKDIR /android_sdk

ARG ANDROID_SDK_TOOLS_VERSION
ARG ANDROID_PLATFORM_VERSION
ARG ANDROID_BUILD_TOOLS_VERSION
ARG NDK_VERSION

ENV ANDROID_HOME="/android-sdk"
ENV ANDROID_SDK_ROOT="$ANDROID_HOME"
ENV NDK_HOME="${ANDROID_HOME}/ndk/${NDK_VERSION}"
ENV PATH=${PATH}:/android_sdk/cmdline-tools/latest/bin
ENV JAVA_HOME=/usr/lib/jvm/java-11-openjdk-amd64

RUN sudo apt update \
    && sudo apt install -y --no-install-recommends curl default-jdk

RUN curl -C - --output android-sdk-tools.zip "https://dl.google.com/android/repository/commandlinetools-linux-${ANDROID_SDK_TOOLS_VERSION}_latest.zip" \
    && mkdir -p /android_sdk/cmdline-tools/latest/ \
    && unzip -q android-sdk-tools.zip -d /android_sdk/cmdline-tools/latest/ \
    && mv /android_sdk/cmdline-tools/latest/cmdline-tools/* /android_sdk/cmdline-tools/latest \
    && rm -r /android_sdk/cmdline-tools/latest/cmdline-tools \
    && rm android-sdk-tools.zip \
    && yes | sdkmanager --licenses \
    && touch $HOME/.android/repositories.cfg \
    && sdkmanager "platform-tools" \
    && sdkmanager "emulator" \
    && sdkmanager "platforms;android-${ANDROID_PLATFORM_VERSION}" "build-tools;$ANDROID_BUILD_TOOLS_VERSION" \
    && sdkmanager "ndk;${NDK_VERSION}" \
    && sdkmanager "cmdline-tools;latest"

ARG VARIANT
FROM mcr.microsoft.com/vscode/devcontainers/base:0-${VARIANT}

ARG ANDROID_SDK_TOOLS_VERSION
ARG ANDROID_PLATFORM_VERSION
ARG ANDROID_BUILD_TOOLS_VERSION
ARG NDK_VERSION

ARG USERNAME
ARG USER_UID
ARG USER_GID
ARG DEPENDENCIES
ARG EXTRA_DEPENDENCIES

#Locale
ENV LANG C.UTF-8

ENV ANDROID_HOME="/home/${USERNAME}/android-sdk"
ENV ANDROID_SDK_ROOT="$ANDROID_HOME"
ENV NDK_HOME="${ANDROID_HOME}/ndk/${NDK_VERSION}"
ENV PATH=${PATH}:${ANDROID_HOME}/cmdline-tools/latest/bin:${ANDROID_HOME}/platform-tools:${ANDROID_HOME}/emulator
ENV JAVA_HOME=/usr/lib/jvm/java-11-openjdk-amd64

RUN export DEBIAN_FRONTEND=noninteractive \
    && apt update \
    && apt upgrade -y \
    && apt install -y --no-install-recommends sudo default-jdk \
    wget curl git xz-utils zip unzip --no-install-recommends \
    && apt install -y $DEPENDENCIES $EXTRA_DEPENDENCIES

RUN apt autoremove -y \
    && apt clean -y \
    && rm -rf /var/lib/apt/lists/*

RUN echo $USERNAME ALL=\(root\) NOPASSWD:ALL >/etc/sudoers.d/$USERNAME \
    && chmod 0440 /etc/sudoers.d/$USERNAME

USER $USERNAME
RUN curl https://sh.rustup.rs -sSf | \
    sh -s -- --default-toolchain stable -y
RUN echo "source /home/${USERNAME}/.cargo/env" >>/home/${USERNAME}/.bashrc
ENV PATH="/home/${USERNAME}/.cargo/bin:$PATH"
RUN rustup update && rustup target add aarch64-linux-android armv7-linux-androideabi i686-linux-android x86_64-linux-android

COPY --from=android_sdk --chown=${USERNAME}:${USERNAME} /android_sdk /home/${USERNAME}/android_sdk

USER $USERNAME
