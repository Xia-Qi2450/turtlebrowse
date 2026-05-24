<script setup lang="ts">
import '@m3e/web/button';
import '@m3e/web/icon';
</script>

<template>
    <div class="hero-wrapper">
        <img class="browser-logo" src="/logo_full_trans.png" loading="lazy" />
        <div class="hero-title-container">
            <h1 class="hero-title">The browser</h1>
            <h1 class="hero-title">built to be <span class="browser-feat">agentic</span></h1>
        </div>
        <m3e-button variant="filled" size="medium">
            <m3e-icon slot="icon" name="download"></m3e-icon>
            Download Turtlebrowse
        </m3e-button>
        <img class="newtab-image" src="/newtab.png" loading="lazy" />
    </div>
</template>

<style scoped>
.hero-wrapper {
    width: 100%;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: flex-start;
    padding: 20px;
    box-sizing: border-box;
    gap: 30px;
}

.hero-title-container {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    width: 100%;
    box-sizing: border-box;
}

.hero-title {
    font-size: 4.5rem;
    font-weight: 600;
    margin: 0;
    text-align: center;
}

.browser-logo {
    aspect-ratio: 1 / 1;
    max-width: 90%;
    width: 150px;
    height: 150px;
}

.browser-feat {
    background-color: var(--md-sys-color-primary-container);
    color: var(--md-sys-color-on-primary-container);
    padding: 10px;
    border-radius: 100px;
    font-size: 4rem;
    font-weight: 500;
}

.newtab-image {
    width: 50%;
    will-change: transform;
    animation: imageScale linear both;
    animation-timeline: view();
}

@keyframes imageScale {
    0% {
        transform: scale(0.25);
    }
    100% {
        transform: scale(1);
    }
}
</style>
