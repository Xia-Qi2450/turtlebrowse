<script setup lang="ts">
import AISection from '@/components/AISection.vue';
import HeroSection from '@/components/HeroSection.vue';
import OpenSourceSection from '@/components/OpenSourceSection.vue';
import ThemeSection from '@/components/ThemeSection.vue';
</script>

<template>
    <div class="home-wrapper">
        <HeroSection></HeroSection>
        <AISection></AISection>
        <ThemeSection></ThemeSection>
        <OpenSourceSection></OpenSourceSection>
    </div>
</template>

<style scoped>
.home-wrapper {
    width: 100svw;
    height: auto;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: flex-start;
    padding: 20px;
    box-sizing: border-box;
}
</style>
