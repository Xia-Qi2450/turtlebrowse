<script setup lang="ts">
import '@m3e/web/list';
import '@m3e/web/card';

function download(url: string) {
    window.open(
        `https://github.com/ingStudiosOfficial/turtlebrowse/releases/latest/download/turtlebrowse_${url}`,
    );
}
</script>

<template>
    <div class="download-wrapper">
        <h1 class="download-title">Download Turtlebrowse (Latest)</h1>
        <p>Download Turtlebrowse for your respective operating system and CPU architecture.</p>
        <m3e-card>
            <m3e-action-list slot="content" variant="segmented">
                <m3e-list-action @click="download('debian_amd64.deb')"
                    >Debian/Ubuntu Linux amd64</m3e-list-action
                >
                <m3e-list-action @click="download('debian_arm64.deb')"
                    >Debian/Ubuntu Linux arm64</m3e-list-action
                >
                <m3e-list-action @click="download('fedora_amd64.rpm')"
                    >Fedora Linux amd64</m3e-list-action
                >
                <m3e-list-action @click="download('fedora_arm64.rpm')"
                    >Fedora Linux arm64</m3e-list-action
                >
                <m3e-list-action @click="download('windows_arm64.deb')"
                    >Windows 10/11 amd64</m3e-list-action
                >
            </m3e-action-list>
        </m3e-card>

        <p>
            Can't find your operating system? Use the
            <a
                href="https://github.com/ingStudiosOfficial/turtlebrowse/releases/latest/download/turtlebrowse_jar.jar"
                >JAR</a
            >
            if you have JDK 25 installed on your system instead.
        </p>
        <p>macOS amd64/arm64 and Windows arm64 builds coming soon.</p>
    </div>
</template>

<style scoped>
.download-wrapper {
    width: 100svw;
    height: 100svh;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: flex-start;
    overflow-y: scroll;
    padding: 20px;
    box-sizing: border-box;
}

.download-title {
    font-size: 2.5rem;
    font-weight: 600;
    margin: 0;
    text-align: center;
}
</style>
