import { fileURLToPath, URL } from 'node:url'

import { defineConfig } from 'vite'

// https://vite.dev/config/
export default defineConfig({
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url))
    },
  },
  base: './',
  build: {
	outDir: '../../../app/src/main/resources/dino',
	emptyOutDir: true,
	rollupOptions: {
		input: {
			dino: fileURLToPath(new URL('./dino.html', import.meta.url)),
		}
	},
  },
  server: {
	port: 3003,
  }
})
