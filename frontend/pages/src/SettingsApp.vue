<script setup lang="ts">
import '@m3e/web/card';
import '@m3e/web/list';
import '@m3e/web/avatar';
import '@m3e/web/icon';
</script>

<template>
	<div class="settings-wrapper">
		<h1 class="settings-header">Settings</h1>
		<m3e-card>
			<m3e-action-list slot="content" variant="segmented">
				<m3e-list-action>
					<m3e-avatar slot="leading">
						<m3e-icon optical-size="30" weight="600" name="shield"></m3e-icon>
					</m3e-avatar>
					<span class="settings-text">Privacy & security</span>
				</m3e-list-action>
				<m3e-list-action>
					<m3e-avatar slot="leading">
						<m3e-icon optical-size="30" weight="600" name="search"></m3e-icon>
					</m3e-avatar>
					<span class="settings-text">Search engines</span>
				</m3e-list-action>
			</m3e-action-list>
		</m3e-card>
	</div>
</template>

<style scoped>
.settings-header {
	margin: 16px 0;
	font-size: 2.5rem;
}

.settings-wrapper {
	display: flex;
	flex-direction: row;
	width: 100dvw;
	height: 100dvh;
	box-sizing: border-box;
	padding: 16px;
	display: flex;
	flex-direction: column;
	align-items: center;
}

.settings-text {
	font-size: 1.7rem;
}

m3e-avatar {
	--m3e-avatar-size: 50px;
}
</style>
