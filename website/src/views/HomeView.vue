<script setup lang="ts">
import AISection from '@/components/AISection.vue';
import HeroSection from '@/components/HeroSection.vue';
</script>

<template>
    <div class="home-wrapper">
        <HeroSection></HeroSection>
        <AISection></AISection>
    </div>
</template>

<style scoped>
.home-wrapper {
    width: 100svw;
    height: 100svh;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: flex-start;
    overflow-y: scroll;
}
</style>
