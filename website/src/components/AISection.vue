<script setup lang="ts"></script>

<template>
    <div class="ai-section-wrapper">
        <div class="section-title-container">
            <h2 class="section-title">Powered by the</h2>
            <h2 class="section-title">latest <span class="gemma-emph">Gemma 4</span> models</h2>
        </div>
        <img class="ai-sidebar-img" src="/ai_sidebar.png" loading="lazy" />
    </div>
</template>

<style scoped>
.ai-section-wrapper {
    width: 100%;
    min-height: 100svh;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: flex-start;
    padding: 20px;
    box-sizing: border-box;
    gap: 30px;
}

.gemma-emph {
    background: var(--md-sys-color-primary-container);
    background: linear-gradient(142deg, rgba(9, 107, 90, 1) 0%, rgba(189, 207, 71, 1) 100%);
    color: var(--md-sys-color-on-primary);
    padding: 10px;
    border-radius: 100px;
    font-size: 3rem;
    font-weight: 500;
}

.ai-sidebar-img {
    width: 30%;
    will-change: transform;
    animation: aiSidebarScale linear both;
    animation-timeline: view();
}

@keyframes aiSidebarScale {
    0% {
        transform: scale(0.25);
    }
    100% {
        transform: scale(1);
    }
}
</style>
