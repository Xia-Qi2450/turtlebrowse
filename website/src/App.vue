<script setup lang="ts">
import { RouterView } from 'vue-router';
</script>

<template>
    <RouterView></RouterView>
    <footer>
        <p class="copyright">Made with 💖 by (ing) Studios and Ethan Lee</p>
    </footer>
</template>

<style scoped>
footer {
    padding: 20px;
    background-color: var(--md-sys-color-surface-container);
    color: var(--md-sys-color-on-surface-container);
    box-sizing: border-box;
}

.copyright {
    margin: 0;
}
</style>
